# Kế hoạch phát triển bản custom

## Mục tiêu và ranh giới

Phát triển Space Shooter từ prototype Fando trên Java/LibGDX thành game landscape
mobile, ưu tiên Android: tám mission qua hai region, combat/progression, artwork,
VFX/audio và UI hoàn chỉnh. Giữ applicationId custom và menu chạy bản original.
Mốc setup đã commit ở d59720c; G1–G6 đang được triển khai; trạng thái nghiệm thu thật nằm tại checkpoint.

Giữ Gradle/LibGDX và các module core/android/ios. Tách trách nhiệm ở những phần
đang sửa, đặc biệt GamePlayScreen hiện 1.212 dòng; không dựng một engine/framework
mới hoặc giữ thêm mode game custom cũ sau khi đã thay thành công.

## Kiến trúc triển khai

- MainGame sở hữu AssetManager và chuyển màn hình; LoadingScreen tải catalog,
  atlas, font/audio. Registry là tài nguyên của phiên game, không giữ static
  cache qua Activity mới. Asset được load theo catalog, không dò file mỗi frame.
- GamePlayScreen điều phối session và Scene2D; simulation, renderer và HUD có
  trách nhiệm riêng. GameState giữ runtime state; definition/config bất biến.
- Thời gian simulation dùng giây; chuyển đổi input và renderer ở biên. Camera
  orthographic dùng world units, riêng với pixel/safe area của HUD. Giữ tỉ lệ
  sprite theo PPU; không nhân lại balance theo độ phân giải màn hình.
- EncounterRunner quản lịch spawn/completion; movement, fire coordination và
  reward có owner rõ. Pool reset đầy đủ; presentation không ghi damage/save.
- Dùng Java 8 trong core theo build hiện tại; không dùng records hoặc API Android
  trong logic dùng chung. Collision dùng geometry LibGDX, không thêm physics
  engine để thay những phép query/collision hiện có thể biểu diễn trực tiếp.
- Config được đưa về JSON/ID ổn định, asset dùng TextureAtlas/region có metadata.
  JSON là dữ liệu; Java giữ behavior. Không biến config thành mutable save.

## Các mốc và phân công

| Mốc | Kết quả phải có | Việc giao Claude qua 9Router | Codex tích hợp |
|---|---|---|---|
| G1 — Catalog và asset | Inventory toàn bộ nội dung cần dùng; export catalog cho lát Mission01, atlas/font/clip/metadata; giữ provenance/license | Exporter/validator offline; preparation asset và manifest theo file scope riêng | Chốt catalog/DTO, loader/lifecycle và preview asset trên Android |
| G2 — Một encounter chơi được | Landscape, camera/bounds, relative drag/hold-to-fire, một weapon/enemy, lives/death/respawn, HUD/pause; có art/SFX thật | Logic input/gate/lives; runner/flight đơn giản; renderer/UI từng phần sau khi interface đã chốt | Tách GamePlayScreen, nối New Game vào flow mới, chạy encounter xuyên suốt |
| G3 — Player combat và rewards | Ba nhánh weapon, progression riêng tới Lv5, shared heat, missile reserve, protection, pickup/core, score/Shard/quota/pity | Weapon/thermal/missile; reward/pickup theo module độc lập | Nối combat/collision/HUD; kiểm tra damage, cooldown và reset |
| G4 — Enemy, hazard và boss | Đủ enemy/hazard/asteroid, flight/formation, phối hợp bắn, escort và boss phase/module/shield/beam/barrage | Behavior/attack modules và VFX tương ứng, tách rõ file ownership | Encounter cleanup, attack budget, boss completion và pool reuse |
| G5 — Campaign và save | Mission01–08, hai region, hướng combat/camera, intro/travel/transition, checkpoint/Continue/Retry/Replay, profile/cosmetics | Content/data batches; persistence validation/serialization sau khi contract chốt | Campaign lifecycle, save transaction, chống ghi trùng reward và nối menu |
| G6 — Presentation đầy đủ | Menu/settings/results/hangar/credits, HUD/boss UI, animation/background/cinematics, audio event bindings/mix | Scene2D screens, animation/effect và audio modules độc lập | Review hình/âm thanh, safe area, lifecycle và performance đại diện |
| G7 — Bàn giao Android | Build ổn định, cleanup phần cũ, đủ asset/license, regression quan trọng và checkpoint cuối | Rà chéo những module vừa đổi, sửa lỗi theo phạm vi | Chạy một flow tổng thể, lập danh sách giới hạn và đóng mốc |

Mỗi mốc xong phải có flow chạy được; không đợi chuyển xong mọi hệ thống mới build
lần đầu. G1 chỉ cần build khi loader/asset runtime đã tích hợp, không build sau
mỗi lần exporter chạy. G2 dùng New Game hiện có, không thêm launcher hoặc menu
chọn implementation thử nghiệm. Giữ phần cũ tạm thời khi chưa có replacement;
xóa code/asset/credit cũ khi đã chuyển hết callers tương ứng.

## Hợp đồng tối thiểu trước khi giao code

G1 chốt catalog version, ID và quan hệ config/asset; field/giá trị cần thiết lấy
từ dữ liệu đã kiểm tra, không tự điền balance. G2 chốt session states, input/fire/
damage permissions, update theo giây và events combat/presentation. Từ đó chia
module; không để hai job tự định nghĩa lại cùng interface.

Trước G5, chốt save envelope gồm campaign/profile/settings, version và ID content.
Mặc định: giữ nguyên Preferences cũ, dùng vùng lưu mới cho campaign; không tự đổi
sao thành Shard hoặc HP thành lives, không tự nhập save của game khác. New Game/
Continue trong flow mới dùng campaign save. Schema mới có validation, temp-write/
replace/backup và từ chối version chưa hỗ trợ; write failure giữ kết quả để retry.
Đây là lựa chọn bảo toàn dữ liệu cho kế hoạch này; muốn chuyển đổi dữ liệu cũ
thành tiến trình mới là một yêu cầu riêng trước khi code persistence.

Definition/DTO dùng chung được Codex chốt trước; Claude triển khai theo contract.
Các chi tiết đối chiếu nguồn thuộc tài liệu nội bộ, không rải vào docs gameplay.

## Cổng kiểm tra và quy tắc không lặp

| Điểm kiểm tra | Nội dung cần chứng minh | Cách chạy |
|---|---|---|
| G1 export | ID/ref đầy đủ; frame/pivot/PPU đúng; catalog tái tạo ổn định; không thiếu license cần thiết | Python unittest/validator của exporter; xem một mẫu sprite/animation/audio trên Android khi tích hợp loader |
| G2–G4 logic | Input/gate, damage một lần, lives/respawn, heat recovery, missile reserve, reward/quota, encounter completion, attack cancel/pool reset | Checks Java thuần trong core, chạy bằng Gradle JavaExec với assertions; không thêm desktop target hoặc test framework chưa cần |
| G5 persistence | Save/load, corrupt/backup, unsupported version, write failure, reward không credit hai lần; Replay/Dev tách campaign | Regression persistence và một Continue/Retry flow trên Android |
| G6–G7 Android | Layout landscape nhiều tỉ lệ, pause/background/resume, asset lifetime, boss transition, nghe mix và đo hiệu năng đại diện | Một smoke nhóm tính năng ở cuối mốc; một flow tổng thể ở lần bàn giao |

- Claude không tự chạy Gradle/APK/full suite ở mỗi job. Codex gom các phần liên
  quan rồi chạy compile/check phù hợp một lần tại integration checkpoint.
- Khi một check fail: chỉ sửa và chạy lại check đó; mở rộng nếu fix chạm owner khác.
- Check đã pass chỉ chạy lại khi input/source/config liên quan thay đổi. Đổi docs
  chỉ kiểm tra docs; đổi content chỉ validate content và smoke màn bị ảnh hưởng.
- Không test lại wave/endless của custom cũ hoặc build original sau mỗi mốc mới.
  Chỉ kiểm tra script/original lại nếu sửa chính tooling hoặc đóng gói liên quan.
- Kết quả ghi kèm commit/diff, input hoặc config đã kiểm và giới hạn; không lưu
  hàng trang log trong chat. Chi tiết vận hành ở [quy trình AI](../systems/ai-workflow.md).

## Cách bắt đầu

Bắt đầu G1 bằng inventory production và catalog cho Mission01; đồng thời chốt
interface loader/asset registry. Khi G1 pass mới mở G2. Chưa tự commit/push hoặc
cài công cụ hệ thống; công cụ hiện có đủ để bắt đầu. Các mốc sau mở theo phụ thuộc
và cập nhật [checkpoint](checkpoint.md), không làm lại mốc setup đã hoàn tất.
