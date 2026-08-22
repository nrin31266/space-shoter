# NGUỒN GỐC VÀ BẢN QUYỀN TÀI NGUYÊN (ASSET ATTRIBUTIONS)

Tài liệu này ghi nhận nguồn gốc và giấy phép bản quyền của tất cả tài nguyên (assets) được sử dụng trong trò chơi.

## Tổng quan (Summary)

Tất cả sprite và âm thanh được phát hành trong game đều là **tài nguyên gốc, tự tạo** thông qua các script trong thư mục `tools/`. Chúng thuộc phạm vi công cộng (public domain) / tác phẩm gốc của tác giả dự án — không phân phối lại bất kỳ tệp nào của bên thứ ba, do đó không có nghĩa vụ ghi nhận bản quyền bên ngoài hoặc xung đột giấy phép.

| Nhóm tài nguyên | Tệp | Nguồn gốc | Giấy phép |
|-----------------|-----|-----------|-----------|
| Sprite (phi thuyền, boss, đạn, vật phẩm, tinh vân) | `assets/*.png` | Tạo bởi `tools/gen_sprites.py` | Tác phẩm gốc (public domain) |
| Nhạc nền (menu + gameplay) | `assets/music/*.ogg` | Tổng hợp bởi `tools/gen_audio.py` | Tác phẩm gốc (public domain) |
| Hiệu ứng âm thanh đặc trưng (nhặt đồ, nâng cấp, wave, boss) | `assets/sounds/*.ogg` | Tổng hợp bởi `tools/gen_audio.py` | Tác phẩm gốc (public domain) |
| Hiệu ứng âm thanh cũ (Legacy SFX) | `assets/sounds/*.mp3` | Đã có sẵn trong repository | Tệp dự án có sẵn |

## Sprite (`tools/gen_sprites.py` + `tools/gen_projectiles_items.py`)

Được tạo tự động bằng mã nguồn (offline, tại thời điểm build — **không phải** khi runtime) để mang lại phong cách "arcade neon hiện đại" đồng nhất với kênh alpha đảm bảo sắc nét:

- `ship.png` — phi thuyền người chơi *(tác giả sở hữu, không tạo lại)*
- `enemy1.png` … `enemy_f.png` — 6 hình dáng kẻ địch *(tác giả sở hữu, không tạo lại)*
- `enemy_boss.png` — tàu chiến trùm (boss dreadnought) *(tác giả sở hữu, không tạo lại)*
- `laser_blue.png`, `laser_red.png`, `plasma_orb.png` — đạn bắn
- `orb_red.png`, `orb_green.png`, `orb_gold.png`, `orb_purple.png`, `orb_pink.png` — đạn tròn của kẻ địch
- `shot_orb.png` (cầu plasma xuyên thấu neon), `shot_dart.png` (phi tiêu tự tìm mục tiêu neon) — đạn vũ khí người chơi
- `item_star.png` (tiền sao vàng), `item_hp.png`, `item_upgrade.png` (tia sét plasma),
  `item_upgrade_explosive.png` (vụ nổ màu cam), `item_upgrade_homing.png` (tâm ngắm màu tím),
  `item_energy.png` (tăng cấp năng lượng xanh lá) — vật phẩm nhặt
- `nebula.png` — tấm nền không gian (tối, với các vòng hào quang mờ)

Tất cả sprite được kết xuất với kỹ thuật siêu lấy mẫu (supersampling) 4× và thu nhỏ tỷ lệ bằng bộ lọc Lanczos, giúp loại bỏ các lỗi vỡ ảnh/pixel bị kéo giãn theo chiều ngang từng xuất hiện ở các tệp PNG tạo thủ công trước đây (các tệp cũ đó chứa hàng trăm ngàn pixel bóng mờ bán trong suốt).

Để tạo lại đạn/vật phẩm/sao: `python3 tools/gen_projectiles_items.py`
(`gen_sprites.py` tạo lại toàn bộ bộ tài nguyên).

## Âm thanh (`tools/gen_audio.py`)

Tất cả nhạc nền và SFX đặc trưng đều được tổng hợp từ đầu bằng numpy (bộ dao động sine/tam giác/răng cưa, bộ bao ADSR, bộ lọc thông thấp lowpass). **Không tái sử dụng âm thanh mẫu (sample)**, vì vậy các tệp đều là những tác phẩm gốc.

- `music/action_music.ogg` — vòng lặp synthwave mượt mà (128 BPM, La thứ / A minor) cho gameplay
- `music/ut.ogg` — vòng lặp ambient êm dịu cho menu
- `sounds/pickup.ogg` — tiếng chuông thanh sáng
- `sounds/powerup.ogg` — hợp âm rải thăng tiến (rising arpeggio)
- `sounds/wave_start.ogg` — âm thanh vút báo hiệu bắt đầu đợt quái
- `sounds/wave_clear.ogg` — khúc nhạc ngắn báo hiệu qua màn thành công (fanfare)
- `sounds/boss_warning.ogg` — tiếng còi cảnh báo trùm nguy hiểm

Để tạo lại: `python3 tools/gen_audio.py`

## Đã đánh giá nhưng không đưa vào bản phát hành

Gói **Kenney "Space Kit"** (CC0, www.kenney.nl) đã được tải về và đánh giá như một nguồn tài nguyên ảnh vẽ thực tế ứng viên. Phong cách hoạt hình góc nhìn ngang của gói này không phù hợp với định hướng neon-arcade của trò chơi, vì vậy không có tệp nào của Kenney được phân phối lại. Các tài nguyên của Kenney vẫn là nguồn tham khảo tốt trong tương lai cho giao diện UI/hiệu ứng hạt (particle).
