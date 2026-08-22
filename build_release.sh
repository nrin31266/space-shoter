#!/usr/bin/env bash
set -Eeuo pipefail

# ============================================================================
#  build_release.sh — Đóng gói APK Space Shooter theo version
#
#  Cách dùng:
#      ./build_release.sh
#
#  Script sẽ hỏi:
#      - Version (dạng x.y.z, ví dụ 1.2.0)
#      - versionCode (mặc định tự sinh: major*10000 + minor*100 + patch)
#      - Loại APK: 1 = Release (đã ký debug keystore) | 2 = Debug
#
#  APK sau khi build được copy vào thư mục: releases/
# ============================================================================

# --- Xác định thư mục gốc project -------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -x "$(pwd)/gradlew" ]]; then
  PROJECT_ROOT="$(pwd)"
elif [[ -x "$SCRIPT_DIR/gradlew" ]]; then
  PROJECT_ROOT="$SCRIPT_DIR"
else
  echo "❌ Hãy chạy script tại thư mục project có ./gradlew."
  echo "   Ví dụ: cd /duong-dan/space-shooter && ./build_release.sh"
  exit 1
fi

# --- Cấu hình SDK ------------------------------------------------------------
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_ROOT" || ! -d "$SDK_ROOT" ]]; then
  if [[ -f "$PROJECT_ROOT/local.properties" ]]; then
    PROP_SDK="$(grep -E '^sdk\.dir=' "$PROJECT_ROOT/local.properties" | cut -d'=' -f2- | tr -d '\r')"
    [[ -d "$PROP_SDK" ]] && SDK_ROOT="$PROP_SDK"
  fi
fi
[[ -n "$SDK_ROOT" && -d "$SDK_ROOT" ]] || SDK_ROOT="$HOME/Android/Sdk"
[[ -d "$SDK_ROOT" ]] || SDK_ROOT="/home/nrin31266/Android/Sdk"

[[ -d "$SDK_ROOT" ]] || {
  echo "❌ Không tìm thấy Android SDK tại: $SDK_ROOT"
  echo "   Hãy set biến môi trường ANDROID_SDK_ROOT hoặc kiểm tra local.properties."
  exit 1
}

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"

RELEASES_DIR="$PROJECT_ROOT/releases"
mkdir -p "$RELEASES_DIR"

# --- Tìm công cụ trong build-tools (chọn bản mới nhất) ----------------------
BUILD_TOOLS="$(ls -1 "$SDK_ROOT/build-tools" 2>/dev/null | sort -V | tail -1 || true)"
[[ -n "$BUILD_TOOLS" ]] || {
  echo "❌ Không tìm thấy build-tools trong $SDK_ROOT/build-tools"
  exit 1
}
ZIPALIGN="$SDK_ROOT/build-tools/$BUILD_TOOLS/zipalign"
APKSIGNER="$SDK_ROOT/build-tools/$BUILD_TOOLS/apksigner"
AAPT="$SDK_ROOT/build-tools/$BUILD_TOOLS/aapt"
DEBUG_KEYSTORE="$HOME/.android/debug.keystore"

# Đảm bảo debug.keystore tồn tại nếu chưa có
if [[ ! -f "$DEBUG_KEYSTORE" ]] && command -v keytool >/dev/null 2>&1; then
  echo "ℹ️  Đang tạo debug.keystore mặc định..."
  mkdir -p "$HOME/.android"
  keytool -genkey -v -keystore "$DEBUG_KEYSTORE" -storepass android -alias androiddebugkey \
    -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1 || true
fi

# --- Lấy version hiện tại trong android/build.gradle làm mặc định ------------
CURRENT_VERSION="$(grep -E 'versionName' "$PROJECT_ROOT/android/build.gradle" | sed -E 's/.*"([^"]+)"[^"]*$/\1/' | head -1)"
CURRENT_VERSION="${CURRENT_VERSION:-1.0}"

echo "================================================================"
echo " 🚀 Đóng gói APK Space Shooter"
echo "================================================================"
echo "Project root : $PROJECT_ROOT"
echo "SDK root     : $SDK_ROOT (build-tools $BUILD_TOOLS)"

# --- Nhập version ------------------------------------------------------------
while true; do
  read -rp "📦 Nhập version (x.y.z, ví dụ 1.2.0) [$CURRENT_VERSION]: " VERSION
  VERSION="${VERSION:-$CURRENT_VERSION}"
  if [[ "$VERSION" =~ ^[0-9]+(\.[0-9]+){1,2}$ ]]; then
    break
  fi
  echo "⚠️  Version không hợp lệ (chỉ gồm số và dấu chấm, tối đa 3 phần). Thử lại."
done

# Chuẩn hoá về dạng x.y.z
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
MAJOR="${MAJOR:-0}"; MINOR="${MINOR:-0}"; PATCH="${PATCH:-0}"

# --- Nhập versionCode (mặc định tự sinh) -------------------------------------
AUTO_CODE=$((MAJOR * 10000 + MINOR * 100 + PATCH))
read -rp "🔢 Nhập versionCode [$AUTO_CODE]: " VERSION_CODE
VERSION_CODE="${VERSION_CODE:-$AUTO_CODE}"
[[ "$VERSION_CODE" =~ ^[0-9]+$ ]] || {
  echo "❌ versionCode phải là số nguyên dương."
  exit 1
}

# --- Chọn loại APK -----------------------------------------------------------
echo
echo "Chọn loại APK:"
echo "  1) Release — minify + ký bằng debug keystore (khuyên dùng)"
echo "  2) Debug   — APK debug thông thường"
read -rp "Chọn [1]: " BUILD_CHOICE
BUILD_CHOICE="${BUILD_CHOICE:-1}"
case "$BUILD_CHOICE" in
  1) BUILD_TYPE="release" ;;
  2) BUILD_TYPE="debug" ;;
  *) echo "❌ Lựa chọn không hợp lệ."; exit 1 ;;
esac

OUT_NAME="space-shooter-v${VERSION}-${BUILD_TYPE}.apk"
OUT_APK="$RELEASES_DIR/$OUT_NAME"

echo
echo "--------------------------------------------------------------"
echo " Tóm tắt:"
echo "   Version    : $VERSION"
echo "   VersionCode: $VERSION_CODE"
echo "   Build type : $BUILD_TYPE"
echo "   Output     : $OUT_APK"
echo "--------------------------------------------------------------"
read -rp "Bắt đầu build? (y/N): " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "Đã huỷ."; exit 0; }

# --- Build bằng Gradle (truyền version qua -P, không sửa file) ---------------
echo
echo "=== Build APK $BUILD_TYPE v$VERSION (versionCode $VERSION_CODE) ==="
cd "$PROJECT_ROOT"
./gradlew ":android:assemble$(tr '[:lower:]' '[:upper:]' <<< "${BUILD_TYPE:0:1}")${BUILD_TYPE:1}" \
  -PversionName="$VERSION" -PversionCode="$VERSION_CODE"

# --- Xác định APK thô do Gradle sinh ra --------------------------------------
if [[ "$BUILD_TYPE" == "release" ]]; then
  UNSIGNED_APK="$(ls -1 "$PROJECT_ROOT"/android/build/outputs/apk/release/android-release*.apk 2>/dev/null | head -1 || true)"
  [[ -n "$UNSIGNED_APK" ]] || {
    echo "❌ Không tìm thấy APK release sau khi build."
    exit 1
  }

  # --- Ký APK release bằng debug keystore (zipalign → apksigner) -------------
  if [[ -f "$DEBUG_KEYSTORE" ]]; then
    ALIGNED_APK="$(mktemp --suffix=.apk)"
    echo
    echo "=== Ký APK bằng debug keystore ==="
    "$ZIPALIGN" -f -p 4 "$UNSIGNED_APK" "$ALIGNED_APK"
    "$APKSIGNER" sign \
      --ks "$DEBUG_KEYSTORE" \
      --ks-pass pass:android \
      --key-pass pass:android \
      --ks-key-alias androiddebugkey \
      --v1-signing-enabled true \
      --v2-signing-enabled true \
      --v3-signing-enabled true \
      --out "$OUT_APK" \
      "$ALIGNED_APK"
    rm -f "$ALIGNED_APK"

    if "$APKSIGNER" verify --print-certs "$OUT_APK" >/dev/null 2>&1; then
      echo "✅ Chữ ký APK hợp lệ (debug keystore)."
    else
      echo "⚠️  Không xác minh được chữ ký APK."
    fi
  else
    echo "⚠️  Không tìm thấy debug keystore ($DEBUG_KEYSTORE)."
    echo "   APK release sẽ KHÔNG được ký (không cài được trực tiếp)."
    cp "$UNSIGNED_APK" "$OUT_APK"
  fi
else
  DEBUG_APK="$(ls -1 "$PROJECT_ROOT"/android/build/outputs/apk/debug/android-debug.apk 2>/dev/null | head -1 || true)"
  [[ -n "$DEBUG_APK" ]] || {
    echo "❌ Không tìm thấy APK debug sau khi build."
    exit 1
  }
  cp "$DEBUG_APK" "$OUT_APK"
fi

# --- Xác minh & in thông tin APK cuối ----------------------------------------
echo
echo "=== Thông tin APK ==="
"$AAPT" dump badging "$OUT_APK" 2>/dev/null | grep -E "^(package|application-label):" || true
SIZE="$(du -h "$OUT_APK" | cut -f1)"
SHA256="$(sha256sum "$OUT_APK" | awk '{print $1}')"

echo
echo "✅ ĐÓNG GÓI THÀNH CÔNG"
echo "   File : $OUT_APK"
echo "   Size : $SIZE"
echo "   SHA256: $SHA256"
echo
echo "Cài lên thiết bị/máy ảo:"
echo "   adb install -r \"$OUT_APK\""
