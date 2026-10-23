# UI và UX

## Hiện trạng

MainMenuScreen/GamePlayScreen và CustomUI/UIIcons dùng Scene2D/VisUI. Menu có
New Game, Continue khi có save và Settings; gameplay có HUD, pause và game-over.

## Thiết kế phát triển

Hoàn thiện menu, HUD, pause/settings/results, hangar/credits và chuyển màn hình
bằng Scene2D. Dùng Table/Stack và viewport phù hợp, hỗ trợ landscape nhiều tỉ lệ
và safe area; chữ rõ, nút đủ lớn cho touch, phong cách thống nhất với gameplay.

HUD đọc state; gameplay và save có owner riêng. Back/Pause và quyền nhận input
được điều phối theo màn hình; xử lý pointer khi chuyển màn hình hoặc mất focus.
Kiểm tra New Game/Continue, resume và layout ở nhiều kích thước.
Palette, VFX và audio thuộc [trình bày](audio-vfx.md).
