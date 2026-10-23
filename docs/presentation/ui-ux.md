# UI và UX

## Runtime campaign

Scene2D `CampaignUi`, MainMenuScreen và GamePlayScreen dùng ExtendViewport
1280×720 và safe insets. Menu có Continue/New Game/mission replay/settings/help/
credits, preview tàu động, thẻ tàu/skin/vệt, hangar và cửa hàng cosmetic. New Game có xác nhận; cửa hàng chọn xem trước rồi bấm mua/trang bị; ghi lỗi giữ pending snapshot để thử lưu lại.

HUD theo anchor: heat gauge trên trái, score/Shard phía trên, pause trên phải,
lives dưới trái, missile phía trên nút weapon ở dưới phải. Boss hiện tên, HP,
phase và số node còn sống; temporary effects có timer. Overlay giữ pause/retry/results và lời giới thiệu vị trí bên trái màn hình;
"Chạm để tiếp tục" ở dưới khi briefing hoặc PlanetJourney chờ chạm; chạm màn hình để đi tiếp, không có nút riêng. UI cập nhật cả khi cùng state chuyển sang pha chờ, không chỉ lúc đổi state.
Preview dùng EngineSockets/MovementTrailSocket, động cơ .075s/frame, ribbon
.1s/frame và dao động hull; nút scale theo hover/press với tốc độ 22/s. HUD chỉ hiển thị state, không quyết damage/save.

Relative drag đi qua InputMultiplexer sau UI; cancel pointer khi mất focus/mất quyền điều khiển. Back/Escape về menu hoặc pause tùy màn. Keyboard/gamepad focus và các chi
tiết layout/animation nguồn vẫn cần đối chiếu thêm; không gọi bản Java pixel-perfect.
Bằng chứng và phần chưa nghiệm thu ở [checkpoint](../production/checkpoint.md).

## Dev trong giao diện

Menu và pause dùng chung DevPanel. Dropdown chọn mission, encounter thực tế,
route, branch và level; giữ lựa chọn trong session, không serialize profile.
Routes: đợt chọn, boss, boss phase B, boss victory, chuyển nhiệm vụ,
M4 → hành tinh 2 và hành tinh 2 chờ chạm. Boss route không hợp lệ báo lỗi ngay;
M8 không có route chuyển nhiệm vụ. Route hành tinh 2 chọn M4 làm điểm xuất phát.

God/skip intro/fake shots có checkbox rõ; dropdown có marker mở danh sách và
highlight lựa chọn. Thẻ/tab checked dùng nền cyan và gạch dưới. Fake shots vẫn
chạm/hiện impact nhưng không gây direct/splash damage. Trong pause của Dev replay,
có thể áp dụng branch/level/god/fake rồi chơi tiếp, phóng đạn địch thử khi có enemy
fire definition đang hoạt động, thêm tiếp tế hoặc diệt địch. Chạy route mới luôn
khởi tạo replay riêng từ bản copy profile; không biến campaign đang chơi thành Dev.
