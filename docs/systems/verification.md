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

## Kiểm tra campaign theo cụm

```bash
./gradlew :core:checkCampaign -Pandroid.builder.sdkDownload=false
```

Gói assertion tích hợp đọc catalog thật: store backup/corrupt/schema/write failure,
Shard high-water/ownership, flight/formation, lives/heat/swept collision, reward,
hazard, 8 mission/21 encounter bằng test damage và sweep attack theo thời gian.
Kiểm tra test damage không chứng minh balance hoặc cảm giác chơi thực tế.

DebugConfig/Wave19/Lv7 chỉ còn thuộc code legacy, không điều khiển New Game
campaign. Không chỉnh config đó để thử boss mới. CampaignCombatCheck khởi tạo
state/encounter trực tiếp cho sweep hàng loạt; không ghi save trên thiết bị.

## Smoke trên emulator/thiết bị đã mở

Chạy script chọn bản, xác minh loading/menu, New Game, bắn/effect, HUD, pause/
resume, game-over và Continue sau mở lại app. Kiểm tra mission đang sửa và điểm chuyển hướng/region khi liên quan. Nghe music/SFX riêng với output audio có sẵn; xem log không chứng minh mix
nghe đúng. Kiểm tra layout ở tỉ lệ màn hình hiện tại và một tỉ lệ khác khi có thể.

Mốc gameplay bổ sung regression theo domain và một end-to-end flow; không chạy full
historical suite sau từng đổi text. Device FPS/thermal và iOS cần môi trường thật
phù hợp, không suy từ Linux/emulator. Ghi rõ mục nào bị chặn và lý do.
