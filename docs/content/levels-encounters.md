# Level và encounter

## Hiện trạng

assets/data/waves.json định nghĩa 20 wave. WaveConfig đọc config, WaveManager
quản lịch spawn và EnemyFactory tạo enemy/formation; GamePlayScreen điều phối
wave loop và trạng thái màn chơi.

| Wave | Số action BOSS | Tổng boss theo config |
|---|---:|---:|
| 5 | 1 | 1 |
| 10 | 2 | 2 |
| 15 | 3 | 4 |
| 19 | 1 | 1 |
| 20 | 1 | 2 |

WaveManager gắn pity cho action cuối ở 4/9/14/18/19. Reward boss còn được xử lý
trong EnemyBoss; đây là hai nhánh riêng. Số liệu trên lấy từ config/source hiện tại.

## Thiết kế phát triển

Campaign gồm tám mission qua hai region, chia thành encounter với lịch spawn,
formation/path và điều kiện hoàn thành rõ. Landscape hỗ trợ hướng combat/camera
thay đổi theo màn, cùng playable bounds và chuyển vùng. Đây là thiết kế chưa
triển khai trong Java; chưa mở rộng thêm Mission09–12.

Definition/config bất biến, runtime clock và tiến trình chạy giữ riêng. Dùng ID
ổn định cho content và kiểm tra references. Hoàn thiện một encounter xuyên suốt
trước khi triển khai toàn campaign theo [kế hoạch](../production/development-plan.md).
