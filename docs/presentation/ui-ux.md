# UI và UX

## Runtime campaign

Scene2D `CampaignUi`, MainMenuScreen và GamePlayScreen dùng ExtendViewport
1280×720 và safe insets. Menu có Continue/New Game/mission replay/settings/help/
credits, preview tàu động, thẻ tàu/skin/vệt, hangar và cửa hàng cosmetic. New Game có xác nhận; cửa hàng chọn xem trước rồi bấm mua/trang bị; ghi lỗi giữ pending snapshot để thử lưu lại.

HUD theo anchor: heat gauge trên trái, score/Shard phía trên, pause trên phải,
lives dưới trái, missile phía trên nút weapon ở dưới phải. Boss hiện tên, HP,
phase và số node còn sống; temporary effects có timer. Overlay giữ pause/retry/results và lời giới thiệu vị trí bên trái màn hình;
"Chạm để tiếp tục" ở dưới, không có modal/nút khởi hành chặn giữa hình.
Preview dùng EngineSockets/MovementTrailSocket, động cơ .075s/frame, ribbon
.1s/frame và dao động hull; nút scale theo hover/press với tốc độ 22/s. HUD chỉ hiển thị state, không quyết damage/save.

Relative drag đi qua InputMultiplexer sau UI; cancel pointer khi mất focus/mất quyền điều khiển. Back/Escape về menu hoặc pause tùy màn. Keyboard/gamepad focus và các chi
tiết layout/animation nguồn vẫn cần đối chiếu thêm; không gọi bản Java pixel-perfect.
Bằng chứng và phần chưa nghiệm thu ở [checkpoint](../production/checkpoint.md).
