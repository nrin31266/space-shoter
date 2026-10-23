#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$PROJECT_ROOT/tools/android_env.sh"
fail() { echo "Lỗi: $*" >&2; exit 1; }

case "${1:-}" in
  --custom|--modern|-c) VERSION_CHOICE=1 ;;
  --original|-o) VERSION_CHOICE=2 ;;
  '')
    echo "Chọn phiên bản:"
    echo "  1) Space Shooter custom [mặc định]"
    echo "  2) Space Shooter Original (Fando)"
    read -rp "Lựa chọn [1]: " VERSION_CHOICE || fail "Cần chọn phiên bản hoặc dùng --custom/--original."
    VERSION_CHOICE="${VERSION_CHOICE:-1}"
    ;;
  *) fail "Cách dùng: $0 [--custom|--modern|-c|--original|-o]" ;;
esac
[[ $# -le 1 ]] || fail "Chỉ nhận một lựa chọn phiên bản."

PACKAGE="com.alexei.spaceshooter"
BUILD_DIR="$PROJECT_ROOT"
case "$VERSION_CHOICE" in
  1) APP_LABEL="Space Shooter custom" ;;
  2)
    APP_LABEL="Space Shooter Original"
    PACKAGE="com.alexei.spaceshooter.original"
    BUILD_DIR="$PROJECT_ROOT/space-shooter-original"
    [[ -x "$BUILD_DIR/gradlew" ]] || fail "Thiếu bản gốc. Chạy python3 tools/setup_original.py trước."
    python3 "$PROJECT_ROOT/tools/setup_original.py" --check
    ;;
  *) fail "Lựa chọn phiên bản không hợp lệ: $VERSION_CHOICE" ;;
esac

space_android_env "$PROJECT_ROOT"
ADB="$ANDROID_SDK_ROOT/platform-tools/adb"
[[ -x "$ADB" ]] || fail "Không tìm thấy adb tại $ADB."
"$ADB" start-server >/dev/null
DEVICE_OUTPUT="$("$ADB" devices)" || fail "Không đọc được danh sách ADB."
mapfile -t DEVICES < <(awk 'NR>1 && $2=="device" {print $1}' <<< "$DEVICE_OUTPUT")
((${#DEVICES[@]})) || fail "Không có thiết bị sẵn sàng. Hãy tự mở emulator hoặc kết nối/ủy quyền USB debugging."

if [[ -n "${ANDROID_SERIAL:-}" ]]; then
  SERIAL=""
  for device in "${DEVICES[@]}"; do
    [[ "$device" != "$ANDROID_SERIAL" ]] || SERIAL="$device"
  done
  [[ -n "$SERIAL" ]] || fail "ANDROID_SERIAL không ở trạng thái device: $ANDROID_SERIAL"
elif ((${#DEVICES[@]} == 1)); then
  SERIAL="${DEVICES[0]}"
else
  echo "Thiết bị đang sẵn sàng:"
  for i in "${!DEVICES[@]}"; do
    echo "  $((i+1))) ${DEVICES[$i]}"
  done
  while true; do
    read -rp "Chọn thiết bị [1]: " n || fail "Cần chọn thiết bị hoặc đặt ANDROID_SERIAL."
    n="${n:-1}"
    if [[ "$n" =~ ^[1-9][0-9]{0,5}$ ]] && ((10#$n <= ${#DEVICES[@]})); then
      SERIAL="${DEVICES[$((10#$n-1))]}"
      break
    fi
    echo "Lựa chọn thiết bị không hợp lệ."
  done
fi

APK="$BUILD_DIR/android/build/outputs/apk/debug/android-debug.apk"
echo "Build $APP_LABEL → $SERIAL"
cd "$BUILD_DIR"
./gradlew :android:assembleDebug -Pandroid.builder.sdkDownload=false
[[ -f "$APK" ]] || fail "Không tìm thấy APK sau khi build: $APK"
"$ADB" -s "$SERIAL" install -r "$APK"
"$ADB" -s "$SERIAL" shell am force-stop "$PACKAGE"
if ! START_OUTPUT="$("$ADB" -s "$SERIAL" shell am start -W -n "$PACKAGE/com.alexei.spaceshooter.AndroidLauncher" 2>&1)"; then
  fail "Không mở được app: $START_OUTPUT"
fi
printf '%s\n' "$START_OUTPUT"
if grep -qiE 'Error|Exception|does not exist|Permission Denial|Status: timeout' <<< "$START_OUTPUT"; then
  fail "Android không khởi chạy được activity."
fi
echo "Đã build, cài và mở $APP_LABEL trên $SERIAL."
