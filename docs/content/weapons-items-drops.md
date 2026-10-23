# Weapon, item và drop

## Runtime campaign

`CombatState` giữ ba nhánh Pulse/Plasma/Homing, level riêng tới Lv5. Nhánh khóa có
level 0; core mở hoặc tăng level rồi chọn nhánh đó. `CampaignWorld` đọc cadence,
projectile count/spread/spacing/damage, splash và homing từ catalog weapon.
Thermal profile giữ capacity, heat rate, cooling delay/rate và overheat latch.
Các lớp weapon/Item legacy không còn đường gọi đã được xóa.

Missile reserve khởi đầu 1, trần 3; cooldown .8 giây, flight .32 giây và protection
.7 giây. Detonate dọn đạn địch; normal mất HP còn lại, large nhận 60% max HP,
boss nhận 10% max HP, có visibility/vulnerability gate.

`RewardRules` quản seeded roll, major quota/floor, deadline và low-life crate pity.
Pickup gồm Shard, weapon core, shield, overdrive, support drone, missile ammo và
extra life. Config drop/weapon trong catalog là owner số liệu; không nhân lại
balance theo độ phân giải. Pickup được pool, giữ qua handoff encounter và không
được chuyển thành persistent config.

Save/credit chống cộng trùng thuộc [save/profile](../systems/save-profile.md).
Bằng chứng test và giới hạn homing/collision/VFX ở
[checkpoint](../production/checkpoint.md).
