# Kiến trúc và toolchain

## Hiện trạng

`MainGame` tạo AssetManager, CampaignAssets/Audio/Store và chuyển LoadingScreen →
MainMenuScreen → GamePlayScreen. CampaignWorld chạy simulation, CombatState và
RewardRules giữ combat/drop; BossCombat/DirectedSkill/EscortController giữ ability.
CampaignRenderer/PrefabVisual xử lý atlas/transform/animation, Scene2D giữ UI.
Các lớp GameState/WaveManager/entity/weapon legacy còn trong source nhưng không
được màn custom mới gọi. Không coi việc còn file là runtime đang dùng.

AssetManager sở hữu atlas/audio; font do CampaignAssets tạo một lần và dispose.
Shot/pickup/burst dùng pool; không tạo texture/sound/font mỗi frame. Catalog là
JSON offline, runtime không đọc YAML hoặc cần repository tham khảo.

## Ranh giới phát triển

Giữ các module Gradle `core`, `android`, `ios`; core chỉ dùng API dùng chung.
Triển khai các subsystem bằng Java và API LibGDX.
Config bất biến tách runtime state. Simulation sở hữu movement/combat/time;
presentation sở hữu sprite, VFX, UI/audio. Pool/reset rõ owner; không DI/ECS/event
bus tổng quát chỉ để giống kiến trúc engine khác.

## Phiên bản quan sát

| Thành phần | Phiên bản/config hiện tại |
|---|---|
| Java chạy Gradle | JDK 17 |
| Gradle Wrapper / AGP | 8.4 / 8.1.3 |
| LibGDX / VisUI | 1.12.0 / 1.5.3 |
| MobiVM/RoboVM | 2.3.20 |
| Android compile/target/min SDK | 34 / 34 / 29 |
| Custom applicationId | `com.alexei.spaceshooter` |

## Chọn môi trường build

`tools/android_env.sh` tôn trọng JAVA_HOME, ANDROID_SDK_ROOT/ANDROID_HOME; tiếp đến
đọc sdk.dir từ local.properties, rồi tìm SDK có API 34 ở Android/Sdk và Unity Hub.
Nếu chưa có JAVA_HOME, tìm OpenJDK của Unity hoặc JDK 17 trong /usr/lib/jvm.
Không dùng đường dẫn máy cũ, không thay Java toàn hệ thống. Cấu hình root đã bỏ
org.gradle.java.home cố định; người dùng còn override trong Gradle user home phải
điều chỉnh riêng nếu build báo lỗi.

SDK phải có Build Tools đúng yêu cầu AGP. Lệnh build dùng
`-Pandroid.builder.sdkDownload=false` để không tự cài SDK component. Thiếu thành
phần thì báo chính xác trước khi cài; không nâng version để né lỗi môi trường.

Bản Fando có checkout riêng, commit cố định qua tools/setup_original.py; helper
kiểm tra lại trước mỗi lần chạy --original. Chỉ đổi applicationId và app label,
không đổi namespace/launcher/gameplay. Quy trình chạy ở [README](../../README.md).
