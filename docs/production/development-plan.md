# Kế hoạch phát triển bản custom

## Trạng thái

Space Shooter tiếp tục phát triển từ prototype Fando bằng Java/LibGDX,
Android-first. Mốc setup codebase/docs đã hoàn tất; kết quả nằm ở
[checkpoint](checkpoint.md). Các mốc gameplay bên dưới chưa bắt đầu.

## Lộ trình

| Mốc | Nội dung | Điều kiện bàn giao |
|---|---|---|
| Asset pipeline | Artwork, font, audio, config và atlas | Đủ reference/license; load/dispose đúng; xem/nghe mẫu |
| Một encounter hoàn chỉnh | Landscape, input/fire, một weapon/enemy, HUD/pause, death/respawn | Chơi xuyên suốt trên Android |
| Combat và content | Weapon/heat, pickup/reward, enemy/hazard và boss | Regression hành vi và một boss end-to-end |
| Campaign và persistence | Tám mission, chuyển region, progression/replay/profile/checkpoint | Save/retry/resume đúng và bảo vệ dữ liệu cũ |
| UI/audio/VFX | Menu/settings/results/hangar, art/feedback và mix | Review nhiều tỉ lệ, nghe thật, kiểm tra cleanup/pause |
| Android hoàn thiện | Touch/lifecycle, profiling và asset closure | Hồi quy phù hợp, số đo thiết bị và giới hạn được ghi thật |

## Quy trình

Đọc owner/source liên quan → triển khai một lát chơi được → regression → build →
runtime → cập nhật checkpoint. Thay implementation theo từng subsystem; xóa phần
cũ sau khi phần thay thế chạy được. Chốt migration trước khi sửa persistence.
Giữ đường test nhanh cho late-game và chạy kiểm tra theo phạm vi thay đổi.
