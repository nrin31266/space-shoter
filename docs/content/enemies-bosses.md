# Enemy và boss

## Hiện trạng

Game có sáu lớp EnemyShipA–F và EnemyBoss. EnemyFactory tạo đội hình; các lớp
weapon giữ kiểu khai hỏa. HP, tốc độ và damage lấy từ source/config đang chạy.
Wave có boss được ghi tại [level/encounter](levels-encounters.md).

## Thiết kế phát triển

Mở rộng roster, flight/formation, phối hợp khai hỏa, hazard/asteroid và escort.
Boss có phase, attack module, shield/vulnerability và barrage rõ nhịp, có telegraph
để đọc được trên mobile. Các hệ thống mở rộng chưa triển khai trong Java.

Movement có một owner; attack phát intent, combat giải quyết damage. Presentation
quan sát health/phase, không tự mở vulnerability hoặc phát reward. Pool reset,
attack cancel, death, cleanup và encounter completion cần regression tương ứng.
