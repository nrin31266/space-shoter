# Save và profile

## Campaign hiện tại

`CampaignStore` ghi JSON riêng tại local `campaign/campaign-v1.json`: version,
checkpoint mission/encounter, lives/loadout, score, profile Shard/unlocks/records,
cosmetic inventory và settings. Ghi file tạm, sync, validate, backup rồi replace;
load từ chối schema mới, giữ file corrupt và dùng backup hợp lệ khi có.
Field mới của v1 dùng default khi đọc snapshot của phiên triển khai trước;
không suy lại thống kê per-mission vốn chưa có trong snapshot cũ.

`MainGame` giữ snapshot chờ khi ghi lỗi và cho thử lưu lại. Snapshot checkpoint
chụp ở đầu encounter, không serialize đạn/enemy đang combat. Shard credit dùng
encounter high-water: Retry không farm lại phần đã nhận. Clear ghi record/unlock;
Continue từ mission đã clear tiếp tục chuyến bay đến mission kế; campaign cuối
mở result. `regionReady` phân biệt chuyến sang vùng đang bay với đích đang chờ
chạm; snapshot v1 cũ thiếu field này dùng false và chạy lại chuyến bay, giữ nguyên
inventory/checkpoint/record. Chỉ ghi true sau pha reveal, xóa khi vào mission mới.
Replay có state riêng, không ghi checkpoint/profile. Cosmetic mua/trang bị dùng
catalog cost/slot, giữ inventory; mua lại món đã sở hữu không trừ thêm Shard.

## Dữ liệu cũ

Preferences `space-shooter-save` của SaveManager và `SpaceShooter` của AudioManager
được giữ nguyên, không đọc như campaign save, không chuyển sao thành Shard.
Custom/original khác applicationId và sandbox. Script dùng install -r; không
auto uninstall/pm clear. Chưa có yêu cầu migration Preferences sang campaign.

Kiểm chứng và giới hạn ở [checkpoint](../production/checkpoint.md).
