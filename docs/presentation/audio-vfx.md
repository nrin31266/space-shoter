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

## Phần chưa tương đương hoàn toàn

Cinematic PlanetJourney, staged fragments/armor destruction, boss skill cue,
background parallax và projectile trail/material cần đối chiếu hình ảnh tiếp.
Collision không lấy shape từ sprite cuối. Không đánh đồng build pass hoặc test
logic với nghe audio mix hay nghiệm thu hình ảnh. Gain/duck/loop và output audio
cần nghe trực tiếp trên thiết bị.

[Nguồn asset](../production/assets.md) sở hữu credit/license;
[checkpoint](../production/checkpoint.md) giữ kết quả đã chạy.
