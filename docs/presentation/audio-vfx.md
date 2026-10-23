# Hình ảnh, audio và VFX

## Hiện trạng

Game dùng sprite neon, background Starfield/nebula, muzzle flash, explosion,
shield và hit effects. LoadingScreen/AudioManager tải tài nguyên qua AssetManager.
[Nguồn tài nguyên](../production/assets.md) sở hữu credit và license.

## Thiết kế phát triển

Hoàn thiện silhouette tàu/enemy/boss, animation, exhaust, hit/explosion, shield,
telegraph, background và cinematic. VFX dùng sprite/atlas, pooled particle và
SpriteBatch; chỉ thêm shader khi hiệu ứng thực sự cần. Presentation nhận sự kiện
combat, không quyết damage, reward hoặc save.

Audio có nhóm music, SFX và UI; giới hạn số voice đồng thời, ưu tiên sự kiện quan
trọng, dùng variation và cooldown cho âm lặp. Music dùng stream và chuyển track
mượt; SFX ngắn được cache. Kiểm tra gain, ducking, loop và crossfade bằng nghe thật.

Friendly projectile, hostile projectile, pickup và hazard phải dễ phân biệt.
Kiểm tra effect khi pause/resume, pool reuse, boss death và encounter cleanup.
Layout thuộc [UI/UX](ui-ux.md), quy trình tài nguyên thuộc
[asset pipeline](../production/asset-pipeline.md).
