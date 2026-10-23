# Hình ảnh, audio và VFX

## Runtime campaign

CampaignAssets tải atlas, font và audio một lần. PrefabVisual cache hierarchy,
pivot/PPU, sorting order và SpriteFrameLoop; renderer đọc health để thay damage
sprite, hide node bị hạ. Artwork hiện tại thuộc bộ campaign. `CampaignWorld`
pool explosion/hit/muzzle/pickup bursts; renderer hiển thị shield, telegraph,
beam/barrage, backdrop/planet, engine plume và cosmetic trail.

CampaignAudio đọc AudioPalette: cue gain/variation/interval/voice limit, music
stream/crossfade và ducking khi cue quan trọng. Pause Activity dừng audio;
settings giữ volume và reduced motion. AssetManager sở hữu atlas/audio; font
được CampaignAssets dispose. Không tạo Texture/Sound/font trong render.

## Tinh chỉnh theo phản hồi ngày 2026-10-24

- Sao menu/combat tăng footprint nhẹ: baseline 1.6 + 0.65 × lớp kích thước,
  giữ mật độ 112/96 và twinkle hiện tại; combat scale theo view/900.
- Movement trail dùng ribbon màu trắng được tô gradient như TrailRenderer Unity,
  không nhân alpha của animation sprite; width Ion .09/Rainbow .10, lifetime tối đa .16 s.
- Cảnh báo beam dày tối đa .10 world unit; curtain warning alpha .24;
  boss prepare/lock cue alpha tối thiểu .9. Đây là tuning presentation theo yêu cầu,
  không thay thời gian báo trước hoặc vùng damage.
- Boss nhận đạn qua các BoxCollider2D đang enabled của bộ phận/thân trong prefab;
  bỏ khung flight disabled khỏi narrow-phase. Shield nodes vẫn nhận damage riêng.

## Phần chưa tương đương hoàn toàn

Cinematic PlanetJourney, staged fragments/armor destruction, boss skill cue,
background parallax và projectile trail/material cần đối chiếu hình ảnh tiếp.
Collision không lấy shape từ sprite cuối. Không đánh đồng build pass hoặc test
logic với nghe audio mix hay nghiệm thu hình ảnh. Gain/duck/loop và output audio
cần nghe trực tiếp trên thiết bị.

[Nguồn asset](../production/assets.md) sở hữu credit/license;
[checkpoint](../production/checkpoint.md) giữ kết quả đã chạy.

## Nối UI và phần ảnh còn thiếu

CampaignUi phát Confirm (32) hoặc Back (33) trước callback điều hướng cho click
và keyboard; disabled không phát. Toggle vẫn phát Confirm; dropdown phát khi
đổi giá trị ở Stage. Invalid Dev route và missile không dùng được phát Error (34).
Bỏ lời gọi Confirm/Back trùng ở screen; pool UI giữ tail khi đổi màn.

Renderer lấy hai mảnh nền từ scene (pose/tint/alpha và journey drift), bật accent
của elite theo modifier và vẽ mũi tên hướng quét Warden trong warning phase.
Các field này trước đây có tham chiếu asset nhưng thiếu đường render runtime.
