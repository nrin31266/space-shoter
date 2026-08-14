#!/bin/bash
set -e

echo "=== 1. Building Android APK ==="
./gradlew :android:assembleDebug

echo "=== 2. Checking connected devices ==="
DEVICES=$(adb devices | grep -v "List" | grep "device" | awk '{print $1}')

if [ -z "$DEVICES" ]; then
    echo "❌ Không tìm thấy thiết bị Android nào kết nối qua ADB!"
    echo "💡 Hướng dẫn kết nối điện thoại qua WiFi (Wireless Debugging):"
    echo "   1. Trên điện thoại: Settings -> Developer Options -> Bật Wireless Debugging"
    echo "   2. Chọn 'Pair device with pairing code'"
    echo "   3. Chạy lệnh: adb pair IP:PORT"
    echo "   4. Chạy lệnh: adb connect IP:PORT"
    exit 1
fi

echo "📱 Đã tìm thấy thiết bị: $DEVICES"
echo "=== 3. Cài đặt APK lên điện thoại ==="
adb install -r android/build/outputs/apk/debug/android-debug.apk

echo "=== 4. Mở ứng dụng Space Shooter ==="
adb shell am start -n com.alexei.spaceshooter/com.alexei.spaceshooter.AndroidLauncher

echo "🚀 Đã khởi chạy ứng dụng thành công trên điện thoại!"
