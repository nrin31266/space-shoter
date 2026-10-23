# Space Shooter

Game bắn tàu không gian được phát triển và tùy biến từ prototype
[Fando/space-shooter](https://github.com/Fando/space-shooter), dùng **Java + LibGDX**,
ưu tiên Android và giữ module iOS.

## Trạng thái phát triển

Bản custom hiện có gameplay arcade cuộn dọc, nhiều loại enemy, ba nhánh vũ khí,
item, boss, wave/endless, save, HUD và audio. Chi tiết cùng số liệu đã đối chiếu
source nằm trong [tài liệu theo domain](docs/README.md).

Hướng phát triển tiếp theo là game landscape với campaign, combat và trình bày
hoàn chỉnh hơn trên kiến trúc Java/LibGDX. **Mốc setup codebase và docs đã đóng;
chưa triển khai gameplay mới hoặc thay bộ asset.** Phần đã kiểm chứng ở
[checkpoint](docs/production/checkpoint.md).

Cấu hình debug hiện đang bật ở Wave 19, vũ khí Lv7; cách kiểm tra gameplay thường
nằm trong [kiểm thử](docs/systems/verification.md).

## Chuẩn bị và build

Cần Bash, Git, Python 3.11+, JDK 17, Android SDK có API 34 và Build Tools 33.0.1
cho AGP 8.1.3. Gradle Wrapper tải dependency vào cache ở lần build đầu.
Không cần mở hoặc build dự án Unity. Helper có thể tái sử dụng JDK/Android SDK
đã có trong Android Studio hoặc Unity Android Build Support.

```bash
source tools/android_env.sh
space_android_env "$PWD"
./gradlew :android:assembleDebug -Pandroid.builder.sdkDownload=false
```

APK nằm ở `android/build/outputs/apk/debug/android-debug.apk`. Đóng gói có version bằng
`./build_release.sh`; xem [ghi chú release](releases/README.md).

Đặt `JAVA_HOME` và `ANDROID_SDK_ROOT` nếu muốn chọn toolchain cụ thể. Helper cũng
đọc `sdk.dir` trong `local.properties`, rồi tìm SDK/JDK có sẵn; không tự cài gói
hoặc thay Java mặc định của hệ thống. Chi tiết ở [kiến trúc](docs/systems/architecture.md).

## Chạy trên thiết bị hoặc emulator đã mở

Tự mở emulator hoặc bật USB debugging và ủy quyền thiết bị trước:

```bash
./build_run_spaceshooter.sh             # menu chọn hai bản
./build_run_spaceshooter.sh --custom    # hoặc --modern, -c
python3 tools/setup_original.py         # chuẩn bị bản Fando, chỉ cần lần đầu
./build_run_spaceshooter.sh --original  # hoặc -o
```

Script chọn thiết bị sẵn sàng, build, cài và mở app. Có nhiều thiết bị thì cho
chọn; automation có thể đặt `ANDROID_SERIAL`. Script không khởi động emulator,
không gỡ app và không xóa dữ liệu. Bản custom dùng `com.alexei.spaceshooter`, bản
Fando dùng `com.alexei.spaceshooter.original` để cài song song.

## Cấu trúc và phát triển

| Khu vực | Trách nhiệm |
|---|---|
| `core/` | Gameplay, rendering, UI và audio LibGDX dùng chung |
| `android/`, `ios/` | Launcher và đóng gói theo nền tảng |
| `assets/` | Asset runtime của game hiện tại |
| `docs/` | Thiết kế, hiện trạng và bằng chứng kiểm tra theo domain |
| `tools/` | Công cụ offline, chuẩn bị bản gốc và kiểm thử |
| `space-shooter-original/` | Checkout Fando cố định, Git ignore |

Đọc [AGENTS.md](AGENTS.md), rồi [trang điều hướng](docs/README.md).
[Phát triển](docs/production/development-plan.md) ghi thứ tự các mốc;
[nguồn asset](docs/production/assets.md) sở hữu credit/license.

```bash
python3 tools/check_docs.py
python3 -m unittest discover -s tools/tests -v
```

Context7 có cấu hình dự án cho Codex/Claude; client cần trust/approve khi nạp cấu
hình lần đầu. Cách dùng Claude CLI qua 9Router, chia việc và tiếp tục sau compaction
nằm trong [quy trình AI](docs/systems/ai-workflow.md).

## iOS

Module iOS dùng MobiVM/RoboVM 2.3.20; build bằng `./gradlew :ios:build` trên macOS
có Xcode phù hợp. Không khẳng định iOS đã được xác minh từ Linux. Desktop/web không
phải target của dự án.

## Ghi nhận

Nền prototype: [Fando/space-shooter](https://github.com/Fando/space-shooter).
Bản custom: Nguyễn Văn Rin. Giữ nguồn thực và giấy phép từng tài nguyên tại
[tài nguyên](docs/production/assets.md).
