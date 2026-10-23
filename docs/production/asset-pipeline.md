# Asset pipeline

## Runtime hiện tại

LoadingScreen khởi tạo CampaignAssets qua AssetManager. Atlas được pack offline,
sprite giữ rect/pivot/PPU và audio được load/cache một lần; runtime không đọc Unity.
Exporter giữ dependency production, đổi placeholder tĩnh sang frame sheet đang dùng
và bỏ record/file không còn dependency. Repack xóa thư mục atlas-input cũ trước copy
để ảnh đã loại không quay lại atlas.

## Chuẩn bị tài nguyên

| Loại | Yêu cầu xử lý |
|---|---|
| Sprite và animation | PNG RGBA; ghi kích thước frame, pivot, thời lượng và điểm đặt muzzle |
| UI và font | Font có license, cache glyph/font; icon thống nhất palette và kích thước |
| SFX | Clip ngắn, kiểm tra gain và biến thể; load thành Sound dùng lại |
| Music | Track có loop phù hợp; stream bằng Music, kiểm tra crossfade |
| Dữ liệu nội dung | ID ổn định, kiểm tra reference thiếu/trùng; config tách runtime state |

Quy trình: chuẩn bị file → kiểm tra hình/âm thanh → đóng atlas khi phù hợp →
validate references → tích hợp → kiểm tra trong game. Giữ metadata frame/pivot
khi đóng atlas để vị trí sprite, collider và hiệu ứng nhất quán.
Nguồn, tác giả và license thuộc [tài nguyên](assets.md).

## Quản lý tài nguyên

AssetManager sở hữu tài nguyên đã load; không dispose riêng từng texture/sound
của manager. Cache region sau khi tìm trong atlas; không findRegion, tạo font
hoặc load file mỗi frame. Pool projectile/effect tần suất cao và reset khi reuse.
CampaignAssets/CampaignAudio sở hữu cache theo MainGame; Activity tạo lại load
phiên mới. Preview extras được tiêu thụ một lần, không tự mở lại màn thử.

## Thay và dọn asset

Chỉ giữ asset được game hoặc công cụ hiện dùng tham chiếu. Khi thay một nhóm,
cập nhật loader/registry/callers và kiểm tra build trước khi xóa bộ cũ. Xóa credit
của asset hết dùng cùng thay đổi; giữ credit/license của clip/font còn sử dụng.
Không đặt ảnh hướng dẫn, screenshot cũ hoặc cache công cụ trong assets runtime.
