# Weapon, item và drop

## Hiện trạng

`entity/Ship.java`, các lớp `weapon/` và `entity/Item*` sở hữu các nhánh vũ khí,
level/stockpile, pickup và damage. Drop/debug override đọc ở DebugConfig và callers. Level chung khởi đầu 1,
tối đa 7; stockpile chung khởi đầu 0, tối đa 3, theo Ship.java.

## Số liệu vũ khí Java hiện tại

Nguồn: update()/fire() của WeaponShipLaser, WeaponExplosiveBlaster và
WeaponHomingLightning. Khoảng bắn tính bằng **ms**, không phải viên/giây.
Damage trong constructor không thay thế damage per-level được fire() gán.

| Lv | Laser: số tia / ms | Explosive: số orb / ms | Homing: số dart / ms |
|---|---|---|---|
| 1 | 1 / 150 | 1 / 300 | 1 / 280 |
| 2 | 2 / 130 | 2 / 260 | 2 / 230 |
| 3 | 3 / 115 | 2 / 240 | 2 / 210 |
| 4 | 3 / 100 | 3 / 215 | 3 / 190 |
| 5 | 4 / 90 | 3 / 195 | 3 / 175 |
| 6 | 4 / 80 | 3 / 180 | 3 / 160 |
| 7 | 5 / 70 | 3 / 165 | 3 / 150 |

DebugConfig hiện có drop rate Weapon 0.06, Energy 0.09, HP 0.05, Star 1.00;
đây là giá trị config, không là cam kết xác suất thực sau mọi nhánh reward/pity.

## Thiết kế phát triển

Ba nhánh Pulse Laser/Plasma/Homing có progression riêng và shared heat.
Overheat có latch/recovery rõ; weapon switch không phá thermal state.
Pickup/core, reward quota và pity được quản lý trong subsystem reward.
Thiết kế này chưa thay mô hình shared level hiện tại.

Config tách heat hiện tại, pool state và lịch sử reward. Khi triển khai, test
cadence, hit/pierce/tracking, overheat/recovery, switch, pickup/quota và reset sau
retry. Persistence thuộc [save/profile](../systems/save-profile.md).
