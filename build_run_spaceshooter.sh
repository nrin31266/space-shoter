#!/usr/bin/env bash
set -Eeuo pipefail

# Xác định thư mục gốc project
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -x "$(pwd)/gradlew" ]]; then
  PROJECT_ROOT="$(pwd)"
elif [[ -x "$SCRIPT_DIR/gradlew" ]]; then
  PROJECT_ROOT="$SCRIPT_DIR"
else
  echo "❌ Hãy chạy script tại thư mục project có ./gradlew."
  exit 1
fi

# Cấu hình SDK
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$SDK_ROOT" || ! -d "$SDK_ROOT" ]]; then
  if [[ -f "$PROJECT_ROOT/local.properties" ]]; then
    PROP_SDK="$(grep -E '^sdk\.dir=' "$PROJECT_ROOT/local.properties" | cut -d'=' -f2- | tr -d '\r')"
    [[ -d "$PROP_SDK" ]] && SDK_ROOT="$PROP_SDK"
  fi
fi
[[ -n "$SDK_ROOT" && -d "$SDK_ROOT" ]] || SDK_ROOT="$HOME/Android/Sdk"
[[ -d "$SDK_ROOT" ]] || SDK_ROOT="/home/nrin31266/Android/Sdk"

ADB="$SDK_ROOT/platform-tools/adb"
PACKAGE="com.alexei.spaceshooter"
ACTIVITY="com.alexei.spaceshooter.AndroidLauncher"

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/platform-tools:$SDK_ROOT/emulator:$SDK_ROOT/cmdline-tools/latest/bin:$PATH"

fail() {
  echo "❌ $*" >&2
  exit 1
}

[[ -x "$ADB" ]] || fail "Không tìm thấy adb tại: $ADB"

# --- Chọn phiên bản muốn build & chạy --------------------------------------
CHOSEN_VERSION=""
if [[ "${1:-}" == "--original" || "${1:-}" == "-o" ]]; then
  CHOSEN_VERSION="2"
elif [[ "${1:-}" == "--custom" || "${1:-}" == "-c" || "${1:-}" == "--modern" ]]; then
  CHOSEN_VERSION="1"
fi

if [[ -z "$CHOSEN_VERSION" ]]; then
  echo "================================================================"
  echo " 🎮 Build & Run Space Shooter"
  echo "================================================================"
  echo "Chọn phiên bản game:"
  echo "  1) Bản tùy biến / mới nhất (Modernized) [Mặc định]"
  echo "  2) Bản gốc (Original - space-shooter-original)"
  read -rp "Lựa chọn [1]: " VERSION_CHOICE
  VERSION_CHOICE="${VERSION_CHOICE:-1}"
else
  VERSION_CHOICE="$CHOSEN_VERSION"
fi

if [[ "$VERSION_CHOICE" == "2" ]]; then
  BUILD_DIR="$PROJECT_ROOT/space-shooter-original"
  APP_LABEL="Bản gốc (Original)"
  [[ -d "$BUILD_DIR" ]] || fail "Không tìm thấy thư mục: $BUILD_DIR"
else
  BUILD_DIR="$PROJECT_ROOT"
  APP_LABEL="Bản tùy biến (Modernized)"
fi

APK="$BUILD_DIR/android/build/outputs/apk/debug/android-debug.apk"
[[ -x "$BUILD_DIR/gradlew" ]] || fail "Không tìm thấy gradlew tại: $BUILD_DIR"

"$ADB" start-server >/dev/null

mapfile -t DEVICES < <("$ADB" devices | awk 'NR>1 && $2=="device" {print $1}')
((${#DEVICES[@]} > 0)) || fail "Không có thiết bị ADB ở trạng thái device. Hãy chạy start_pixel_emu.sh trước."

if ((${#DEVICES[@]} == 1)); then
  SERIAL="${DEVICES[0]}"
  echo "📱 Tự chọn thiết bị duy nhất: $SERIAL"
else
  echo "Các thiết bị đang ở trạng thái device:"
  i=1
  for device in "${DEVICES[@]}"; do
    MODEL="$("$ADB" -s "$device" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || true)"
    echo "  $i) $device - ${MODEL:-unknown}"
    ((i++))
  done
  while true; do
    read -rp "Chọn thiết bị [1]: " n
    n="${n:-1}"
    if [[ "$n" =~ ^[0-9]+$ ]] && ((n >= 1 && n <= ${#DEVICES[@]})); then
      SERIAL="${DEVICES[$((n-1))]}"
      break
    fi
    echo "Lựa chọn không hợp lệ."
  done
fi

MODEL="$("$ADB" -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || true)"
ANDROID_VERSION="$("$ADB" -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r' || true)"
echo "Thiết bị   : $SERIAL (${MODEL:-unknown} - Android ${ANDROID_VERSION:-unknown})"
echo "Phiên bản  : $APP_LABEL"
echo "Thư mục    : $BUILD_DIR"

echo
echo "=== 1. Building Android APK ($APP_LABEL) ==="
cd "$BUILD_DIR"
./gradlew :android:assembleDebug

[[ -f "$APK" ]] || {
  echo "❌ Không tìm thấy APK sau khi build: $APK"
  echo "APK hiện có trong thư mục:"
  find "$BUILD_DIR/android" -type f -name '*.apk' -print 2>/dev/null || true
  exit 1
}

echo
echo "=== 2. Cài đặt APK lên $SERIAL ==="
"$ADB" -s "$SERIAL" install -r "$APK"

echo
echo "=== 3. Khởi chạy $PACKAGE/$ACTIVITY ==="
"$ADB" -s "$SERIAL" shell am force-stop "$PACKAGE" || true
START_OUTPUT="$("$ADB" -s "$SERIAL" shell am start -W -n "$PACKAGE/$ACTIVITY" 2>&1 || true)"
printf '%s\n' "$START_OUTPUT"

if printf '%s\n' "$START_OUTPUT" | grep -qE 'Error|Exception|does not exist|Permission Denial'; then
  echo
  echo "❌ Android không khởi chạy được activity đã cấu hình."
  echo "Các activity/package liên quan:"
  "$ADB" -s "$SERIAL" shell cmd package resolve-activity --brief "$PACKAGE" 2>&1 || true
  echo
  echo "Logcat liên quan trong 10 giây gần nhất:"
  "$ADB" -s "$SERIAL" logcat -d -t 300 2>/dev/null | grep -Ei "$PACKAGE|AndroidRuntime|FATAL EXCEPTION|ActivityTaskManager" | tail -100 || true
  exit 1
fi

echo
echo "🚀 Đã build, cài đặt và khởi chạy Space Shooter trên $SERIAL"
echo "Xem log app bằng lệnh:"
echo "$ADB -s $SERIAL logcat --pid=\$(adb -s $SERIAL shell pidof $PACKAGE | tr -d '\\r')"