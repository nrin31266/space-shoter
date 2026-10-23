# Thiết kế Space Shooter

## Nền tảng dự án

Space Shooter là bản tùy biến từ prototype Fando trên GitHub, phát triển bằng
Java/LibGDX cho mobile, ưu tiên Android. Quy trình hiện tại là phát triển source
này theo từng mốc chạy được; không thay engine hoặc chuyển sang Unity.

## Game hiện tại

Luồng chơi: menu → New Game/Continue → wave → boss/endless → game-over. Luật,
content và số liệu thực tế được phân về owner trong [router](../README.md);
source/config là bằng chứng hiện trạng, không lấy mô tả lịch sử thay source.

## Thiết kế bản custom đã chốt

[LOCKED] Landscape mobile, relative drag/hold-to-fire, campaign theo encounter,
combat có nhiều nhánh vũ khí, progression/save và hệ thống hình ảnh/audio/VFX
đồng nhất. Campaign được tổ chức theo mission và encounter; phạm vi nội dung thuộc
[level/encounter](../content/levels-encounters.md).

Các tính năng này là đích phát triển, chưa là hành vi chạy được trong Java.
Không tự thêm multiplayer, backend, cloud save hoặc monetization.

## Phát triển và bằng chứng

[Kế hoạch](../production/development-plan.md) giữ thứ tự mốc;
[checkpoint](../production/checkpoint.md) giữ kết quả kiểm chứng.
