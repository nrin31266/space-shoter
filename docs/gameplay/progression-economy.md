# Progression và economy

## Hiện trạng

ScoreTracker, GamePlayScreen và SaveManager xử lý score và sao. Wave/endless là
tiến trình hiện tại. High score và tổng sao được commit ở cuối lượt; source
SaveManager giữ quy tắc cộng và lưu dữ liệu. Chưa có campaign profile mới.

## Thiết kế phát triển

Campaign gồm mission/encounter, tiến trình mở khóa, Stellar Shard, Mission Replay
và cosmetic. Tách run score, campaign progress và profile; replay/dev run không
được ghi nhầm vào tiến trình campaign. Thiết kế này chưa triển khai trong Java.

Reward/drop thuộc [weapon/item/drop](../content/weapons-items-drops.md);
transaction và tương thích dữ liệu thuộc [save/profile](../systems/save-profile.md).
Cửa hàng tổng quát, talent và gacha ngoài phạm vi hiện tại.
