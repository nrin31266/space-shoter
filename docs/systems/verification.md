# Kiểm thử và debug

## Kiểm tra theo phạm vi

```bash
python3 tools/check_docs.py
python3 -m unittest discover -s tools/tests -v
source tools/android_env.sh
space_android_env "$PWD"
./gradlew :android:assembleDebug -Pandroid.builder.sdkDownload=false
python3 tools/setup_original.py --check
```

Tooling test dùng SDK/JDK/ADB/Gradle giả trong thư mục tạm, không kết nối thiết bị
hoặc tải dependency. Build thật là bước riêng. Kết quả và giới hạn hiện tại ở
[checkpoint](../production/checkpoint.md); không suy runtime pass từ unittest.

## Debug hiện có

Owner duy nhất là `core/src/com/alexei/spaceshooter/utils/DebugConfig.java`.
Snapshot đầu mốc nền: ENABLE_DEBUG=true, Wave 19, Explosive Lv7, HP 5,
DEBUG_TEST_SINGLE_ENEMY=false. Giữ nguyên cấu hình này trong mốc nền.

Muốn kiểm tra luồng thường Wave 1–5: tạm đặt ENABLE_DEBUG=false, build và chạy,
rồi khôi phục đúng giá trị trước test. Không commit debug tuning ngoài task.
Gameplay late-game mới cần đường test nhanh tương đương, không xóa DevConfig nguồn rồi
buộc người kiểm thử chơi cả campaign.

## Smoke trên emulator/thiết bị đã mở

Chạy script chọn bản, xác minh loading/menu, New Game, bắn/effect, HUD, pause/
resume, game-over và Continue sau mở lại app. Với cấu hình thường, kiểm tra Wave
1–5. Nghe music/SFX riêng với output audio có sẵn; xem log không chứng minh mix
nghe đúng. Kiểm tra layout ở tỉ lệ màn hình hiện tại và một tỉ lệ khác khi có thể.

Mốc gameplay bổ sung regression theo domain và một end-to-end flow; không chạy full
historical suite sau từng đổi text. Device FPS/thermal và iOS cần môi trường thật
phù hợp, không suy từ Linux/emulator. Ghi rõ mục nào bị chặn và lý do.
