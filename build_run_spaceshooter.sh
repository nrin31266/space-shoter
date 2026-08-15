#!/usr/bin/env bash
set -Eeuo pipefail

# Cấu hình đúng theo project và SDK của bạn
SDK_ROOT="/home/nrin31266/Android/Sdk"
ADB="$SDK_ROOT/platform-tools/adb"
PACKAGE="com.alexei.spaceshooter"
ACTIVITY="com.alexei.spaceshooter.AndroidLauncher"

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/platform-tools:$SDK_ROOT/emulator:$SDK_ROOT/cmdline-tools/latest/bin:$PATH"

# Nếu chạy file từ project root thì dùng thư mục hiện tại.
# Nếu chạy từ nơi khác nhưng file nằm cạnh project thì dùng thư mục chứa script.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -x "$(pwd)/gradlew" ]]; then
  PROJECT_ROOT="$(pwd)"
elif [[ -x "$SCRIPT_DIR/gradlew" ]]; then
  PROJECT_ROOT="$SCRIPT_DIR"
else
  echo "❌ Hãy chạy script tại thư mục project có ./gradlew."
  echo "Ví dụ: cd /duong-dan/project && $0"
  exit 1
fi

APK="$PROJECT_ROOT/android/build/outputs/apk/debug/android-debug.apk"

fail() {
  echo "❌ $*" >&2
  exit 1
}

[[ -x "$ADB" ]] || fail "Không tìm thấy adb: $ADB"
[[ -x "$PROJECT_ROOT/gradlew" ]] || fail "Không tìm thấy gradlew tại: $PROJECT_ROOT"

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
echo "Thiết bị: $SERIAL"
echo "Model: ${MODEL:-unknown}"
echo "Android: ${ANDROID_VERSION:-unknown}"

echo
echo "=== 1. Building Android APK ==="
cd "$PROJECT_ROOT"
./gradlew :android:assembleDebug

[[ -f "$APK" ]] || {
  echo "❌ Không tìm thấy APK sau khi build: $APK"
  echo "APK hiện có:"
  find "$PROJECT_ROOT/android" -type f -name '*.apk' -print 2>/dev/null || true
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