# Điều hướng tài liệu

## Cách đọc và nguồn sự thật

Bắt đầu từ [AGENTS.md](../AGENTS.md), rồi chọn domain trong bảng dưới. Một fact
chi tiết có một owner; tài liệu khác chỉ tóm tắt và link tương đối. Source/config
chứng minh hành vi hiện tại. Đích đã duyệt có thể khác source vì gameplay mới chưa triển khai;
phải ghi rõ khác biệt, không gọi thiết kế là bằng chứng đã triển khai.

Space Shooter được phát triển từ prototype Fando bằng Java/LibGDX. Tài liệu theo
domain mô tả hệ thống hiện có và thiết kế sắp triển khai của bản custom.

## Owner theo chủ đề

| Chủ đề | Owner |
|---|---|
| Mục tiêu và trải nghiệm | [Thiết kế game](design/game-design.md) |
| Điều khiển, damage/death, quyền input/fire | [Player combat](gameplay/player-combat.md) |
| Score, currency, campaign/replay | [Progression/economy](gameplay/progression-economy.md) |
| Wave/encounter, formation, camera, chuyển vùng | [Level/encounter](content/levels-encounters.md) |
| Enemy và boss | [Enemy/boss](content/enemies-bosses.md) |
| Vũ khí, vật phẩm, reward/drop | [Weapon/item/drop](content/weapons-items-drops.md) |
| Module, lifecycle, rendering và toolchain | [Kiến trúc](systems/architecture.md) |
| Save hiện tại và ranh giới migration | [Save/profile](systems/save-profile.md) |
| Debug, build và runtime verification | [Kiểm thử](systems/verification.md) |
| Context7, Claude, 9Router và tiết kiệm context | [Quy trình AI](systems/ai-workflow.md) |
| Menu, HUD, layout, điều hướng | [UI/UX](presentation/ui-ux.md) |
| Hình ảnh, audio và VFX | [Trình bày](presentation/audio-vfx.md) |
| Credit, giấy phép và bằng chứng nguồn | [Nguồn asset](production/assets.md) |
| Import, atlas, conversion và tái tạo | [Asset pipeline](production/asset-pipeline.md) |
| Mốc phát triển và tiêu chí bàn giao | [Kế hoạch phát triển](production/development-plan.md) |
| Tiến độ và bằng chứng mới nhất | [Checkpoint](production/checkpoint.md) |

## Quy ước

Nội dung mô tả bằng tiếng Việt; giữ thuật ngữ kỹ thuật thông dụng, API, identifier
và command bằng English. Dùng heading có nghĩa, không đánh số thủ công; tên file
lowercase kebab-case. Không ghi lại bảng balance/schema vốn đã do source/config
sở hữu. Khi phát triển thay contract có chủ đích, sửa owner cùng task.

| Nhãn | Ý nghĩa |
|---|---|
| `[LOCKED]` | Quyết định đã được duyệt |
| `[TUNABLE]` | Giá trị có thể cân bằng sau kiểm thử |
| `[PROVISIONAL]` | Đề xuất chưa khóa |
| `[TBD]` | Cần quyết định khi tới mốc liên quan |
| `[FUTURE]` | Không thuộc phạm vi đã duyệt |

Không tự nâng trạng thái. Bằng chứng pass/fail có ngày và môi trường nằm ở
checkpoint, không rải lịch sử debug vào từng owner.
