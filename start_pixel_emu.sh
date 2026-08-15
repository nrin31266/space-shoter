#!/usr/bin/env bash
set -Eeuo pipefail

# Cấu hình đúng theo máy của bạn
SDK_ROOT="/home/nrin31266/Android/Sdk"
EMULATOR="$SDK_ROOT/emulator/emulator"
ADB="$SDK_ROOT/platform-tools/adb"
AVD_NAME="pixel_emu"
LOG_FILE="$HOME/${AVD_NAME}-$(date +%Y%m%d-%H%M%S).log"

export ANDROID_HOME="$SDK_ROOT"
export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$SDK_ROOT/platform-tools:$SDK_ROOT/emulator:$PATH"

fail() {
  echo "❌ $*" >&2
  exit 1
}

[[ -x "$EMULATOR" ]] || fail "Không tìm thấy emulator: $EMULATOR"
[[ -x "$ADB" ]] || fail "Không tìm thấy adb: $ADB"

mapfile -t AVD_LIST < <("$EMULATOR" -list-avds 2>/dev/null)
((${#AVD_LIST[@]} > 0)) || fail "Không tìm thấy AVD nào."

choose_avd() {
  echo "Các AVD đang có:"
  local i=1
  for avd in "${AVD_LIST[@]}"; do
    echo "  $i) $avd"
    ((i++))
  done
  if ((${#AVD_LIST[@]} == 1)); then
    AVD_NAME="${AVD_LIST[0]}"
    echo "Tự chọn AVD duy nhất: $AVD_NAME"
    return
  fi
  local n
  while true; do
    read -rp "Chọn AVD [1]: " n
    n="${n:-1}"
    if [[ "$n" =~ ^[0-9]+$ ]] && ((n >= 1 && n <= ${#AVD_LIST[@]})); then
      AVD_NAME="${AVD_LIST[$((n-1))]}"
      return
    fi
    echo "Lựa chọn không hợp lệ."
  done
}

choose_gpu() {
  echo
  echo "Chọn chế độ GPU:"
echo "  1) host       (mặc định, dùng GPU thật và tránh popup Wayland)"
echo "  2) software   (dùng khi host gặp lỗi đồ họa)"
echo "  3) swiftshader (software renderer khác)"
echo "  4) auto       (emulator tự chọn)"
  local n
  while true; do
    read -rp "Chọn [1]: " n
    case "${n:-1}" in
      1) GPU_MODE="host"; return ;;
      2) GPU_MODE="software"; return ;;
      3) GPU_MODE="swiftshader"; return ;;
      4) GPU_MODE="auto"; return ;;
      *) echo "Lựa chọn không hợp lệ." ;;
    esac
  done
}

choose_avd
choose_gpu

# Chỉ dừng emulator cũ, không đụng tới app hay thiết bị điện thoại thật.
if pgrep -af "$SDK_ROOT/emulator/qemu/linux-x86_64/qemu-system-x86_64" >/dev/null; then
  echo "Đang có emulator cũ."
  read -rp "Dừng emulator cũ trước khi chạy? [Y/n]: " answer
  if [[ ! "$answer" =~ ^[Nn]$ ]]; then
    pkill -f "$SDK_ROOT/emulator/qemu/linux-x86_64/qemu-system-x86_64" || true
    sleep 3
  fi
fi

"$ADB" kill-server >/dev/null 2>&1 || true
"$ADB" start-server >/dev/null

# Với Ubuntu Wayland + Intel/NVIDIA hybrid, xcb thường tránh được một số lỗi cửa sổ Qt.
# Nếu xcb không mở được cửa sổ, chạy lại script và bỏ biến QT_QPA_PLATFORM ở dòng dưới.
export QT_QPA_PLATFORM="${QT_QPA_PLATFORM:-xcb}"

# Không dùng -noaudio: cờ này tắt hoàn toàn âm thanh emulator.
CMD=("$EMULATOR" "@$AVD_NAME" "-gpu" "$GPU_MODE" "-no-snapshot" "-no-boot-anim")

echo
echo "🚀 Khởi động @$AVD_NAME"
echo "GPU: $GPU_MODE"
echo "QT_QPA_PLATFORM: $QT_QPA_PLATFORM"
echo "Log: $LOG_FILE"
echo
echo "Lệnh thực tế: ${CMD[*]}"
echo "Nhấn Ctrl+C trong cửa sổ log nếu cần dừng script; emulator sẽ tiếp tục chạy nền."

nohup "${CMD[@]}" >"$LOG_FILE" 2>&1 &
EMU_PID=$!
echo "PID emulator launcher: $EMU_PID"

wait_for_adb() {
  echo "⏳ Đợi emulator xuất hiện trong ADB..."
  for _ in $(seq 1 90); do
    if "$ADB" devices 2>/dev/null | awk '$1=="emulator-5554" && $2=="device" {found=1} END{exit !found}'; then
      return 0
    fi
    if ! kill -0 "$EMU_PID" 2>/dev/null; then
      echo "❌ Tiến trình emulator đã thoát. 30 dòng log cuối:"
      tail -30 "$LOG_FILE" || true
      exit 1
    fi
    sleep 2
  done
  return 1
}

if ! wait_for_adb; then
  echo "❌ Emulator chưa vào trạng thái device sau 180 giây."
  echo "Trạng thái ADB:"
  "$ADB" devices -l || true
  echo "30 dòng log cuối:"
  tail -30 "$LOG_FILE" || true
  exit 1
fi

echo "⏳ Đợi Android báo boot_completed..."
for _ in $(seq 1 120); do
  BOOTED="$("$ADB" -s emulator-5554 shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  if [[ "$BOOTED" == "1" ]]; then
    echo "✅ Emulator đã khởi động xong: emulator-5554"
    echo "Bạn có thể chạy file build app ở script thứ hai."
    exit 0
  fi
  sleep 2
done

echo "⚠️ ADB đã thấy emulator nhưng Android chưa báo boot_completed."
"$ADB" -s emulator-5554 shell getprop ro.product.model || true
"$ADB" -s emulator-5554 shell getprop sys.boot_completed || true
echo "Log: $LOG_FILE"
exit 2
