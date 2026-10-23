# Player combat

## Hiện trạng

Ship.java giữ HP, protection và trạng thái vũ khí; GamePlayScreen nối touch/drag,
bắn và collision. HP khởi đầu **5**, tối đa **10**. Weapon level và stockpile dùng
chung cho ba nhánh; thông số thuộc [weapon/item/drop](../content/weapons-items-drops.md).
Debug có thể override lượt New Game, xem [kiểm thử](../systems/verification.md).

## Thiết kế phát triển

- Relative drag dùng delta ngón tay để dịch chuyển tàu, tránh ngón tay che tàu.
- Hold-to-fire giữ cadence khi pointer còn hợp lệ; release/cancel dừng shot mới.
- Gate điều phối quyền di chuyển, bắn và nhận damage theo gameplay state.
- Death/respawn, pause, cinematic và mất focus phải xử lý input anchor nhất quán,
  không kéo bù delta cũ hoặc bắn bù cadence.
- Mô hình lives/respawn là thay đổi gameplay sắp triển khai, chưa thay HP hiện tại.

Movement/combat sở hữu state; effect chỉ biểu diễn kết quả. Kiểm tra damage một
lần, protection, death/respawn và fire cadence khi triển khai subsystem này.
Persistence thuộc [save/profile](../systems/save-profile.md).
