# Player combat

## Runtime campaign

`CampaignWorld` sở hữu simulation; `CombatState` giữ quyền input/fire, lives,
shield, thermal và temporary effects. `GamePlayScreen` chuyển delta pointer về
world units, chạy bước cố định 1/60 giây và hủy pointer khi pause/mất quyền điều khiển/mất focus; giữ gesture qua
những pha vẫn cho phép chơi.
Giữ tay để bắn, thả để nguội; đổi súng giữ heat chung.

Khởi đầu 3 lives, trần 5. Hit tiêu shield trước và bảo vệ .7 giây; hit không shield
mất một life, giảm một level của nhánh đang dùng nếu trên Lv1. Death .6 giây,
respawn .55 giây, protection 2.5 giây và enemy fire grace 1 giây. Game-over không
được ghi checkpoint có lives=0. Retry khôi phục snapshot đầu encounter.

Camera profile quyết vùng bay; envelope PlayerMovementBounds chặn cả thân tàu,
độc lập damage hitbox. Briefing, chuyến sang vùng trước tap và results khóa input; sau tap,
approach/travel/brake vẫn cho phép kéo/bắn. Chuyển mission cùng vùng giữ input.
Tap tiếp tục chỉ nhận release bình thường trong .6 giây, drag/cancel không nhận.
Death giữ hostile attacks đang chạy; enemy grace chỉ chặn attack mới.
Pause không tiến simulation.
Thông số weapon/drop thuộc [weapon/item/drop](../content/weapons-items-drops.md).
Các class Ship/weapon cũ còn trong source nhưng không là runtime custom hiện tại.

## Cần đối chiếu tiếp

Đường entrance/respawn và body collision đang được tái hiện trong Java; không
coi build pass là bằng chứng hình ảnh/cảm giác điều khiển đã tương đương hoàn toàn.
Bằng chứng kiểm tra ở [checkpoint](../production/checkpoint.md).
