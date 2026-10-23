# Asset pipeline

## Runtime hiện tại

LoadingScreen xếp hàng tải texture/audio bằng AssetManager; TextureRegistry và
AudioManager giữ các tham chiếu để dùng lại. Các PNG đang được load riêng lẻ,
chưa có TextureAtlas chung. Tài nguyên được chuẩn bị offline; runtime chỉ load
file đã hoàn thiện.

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
TextureRegistry hiện dùng static cache; kiểm tra reset khi Activity hoặc màn
hình được tạo lại trước khi mở rộng pipeline.

## Thay và dọn asset

Chỉ giữ asset được game hoặc công cụ hiện dùng tham chiếu. Khi thay một nhóm,
cập nhật loader/registry/callers và kiểm tra build trước khi xóa bộ cũ. Xóa credit
của asset hết dùng cùng thay đổi; giữ credit/license của clip/font còn sử dụng.
Không đặt ảnh hướng dẫn, screenshot cũ hoặc cache công cụ trong assets runtime.
