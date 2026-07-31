# SYSTEM.md — Master Game Systems & Technical Specification Sheet

> Nguồn sự thật duy nhất (Single Source of Truth) về Wave System, Bảng Chi Tiết Enemy, Hệ Thống Vũ Khí Player, Cơ Chế Bảo Vệ HP & Khiên, Quy Tắc Hút Đồ, Tùy Chỉnh Debug, và Lưu Trữ Điểm/Sao Game Over.
> Cập nhật lần cuối: 08/2026.

---

## 1. Hệ Thống Wave & Cơ Chế Loop Vô Tận (Endless Wave Loop)

Hệ thống Wave hoàn toàn data-driven, tải từ `assets/data/waves.json` qua `WaveConfig.java`.
Gồm **20 Wave** tiêu chuẩn và cơ chế **Endless Wave Loop**:

- **Wave 1–4**: Giới thiệu quái cơ bản `EnemyShipA` -> `EnemyShipB` -> `EnemyShipC` -> `EnemyShipD`.
- **Wave 5**: Màn Boss thử thách đầu tiên (Boss x1).
- **Wave 6–9**: Đội hình dầy đặc (Grid, Chevron, Interleaved Rows).
- **Wave 10**: Màn Boss đôi (Boss x1 + Boss x1).
- **Wave 11–15**: Màn tổng hợp giáp tăng cường (Wave 15: Boss x4 tổng cộng).
- **Wave 16–18**: Giới thiệu 2 loại quái giáp nặng mới `EnemyShipE` (Fast Striker) & `EnemyShipF` (Heavy Dragoon).
- **Wave 19**: Pre-Boss Climax (2 lượt Boss trung cấp xen kẽ quái `EnemyShipF`).
- **Wave 20**: Final Climax Boss Rush (5 lượt Boss, kết thúc bằng đợt **Boss x3 dàn ngang**).
- **Endless Wave Loop**: Sau khi hoàn thành Wave 20, game tự động chờ 2 giây (hiển thị "WAVE CLEAR!") rồi tự chuyển về Wave 1 của vòng loop tiếp theo (hiển thị `WAVE: 1/20 (L2)`).
- **HP Scaling Hàm Mũ Theo Loop**: Máu quái tự động tính theo `effectiveWaveId = (waveLoopCount * 20) + waveId`. Ví dụ: Wave 1 của Loop 2 sẽ có HP tương đương Wave 21.

---

## 2. Bảng Thông Số Enemy (6 Loại Quái + Boss) & Giải Thích Chi Tiết

| Enemy | Size | HP Base | Speed Vào | Speed Hover | Vũ khí & Giãn Cách Bắn | First Wave | growthRate |
|---|---|---|---|---|---|---|---|
| **EnemyShipA** | 60×60 | 1.0 | 380 px/s | 70 px/s | `WeaponEnemyLaser` (Tia laser thẳng, 8000ms) | Wave 1 | 0.04 (4%) |
| **EnemyShipB** | 70×70 | 5.0 | 260 px/s | 55 px/s | `WeaponEnergyBallA` (Cầu năng lượng đuổi tàu, 8000ms) | Wave 2 | 0.06 (6%) |
| **EnemyShipC** | 44×44 | 1.5 | 450 px/s | 95 px/s | `WeaponSniperBeam` (Sniper nhắm chuẩn, 3500ms) | Wave 3 | 0.06 (6%) |
| **EnemyShipD** | 90×90 | 8.0 | 170 px/s | 40 px/s | `WeaponSpreadShot` (Bắn xoè 3 viên đạn, 3000ms) | Wave 4 | 0.08 (8%) |
| **EnemyShipE** | 52×52 | 3.0 | 420 px/s | 85 px/s | `WeaponDoublePulse` (2 viên đạn tím song song, 2400ms) | Wave 16 | 0.06 (6%) |
| **EnemyShipF** | 85×85 | 12.0 | 150 px/s | 35 px/s | `WeaponRingBurst` (4 viên đạn vàng toả chéo, 4000ms) | Wave 16 | 0.08 (8%) |
| **EnemyBoss** | 250×250 | 100.0 | 150 px/s | 100 px/s | Combined (`WeaponSpreadShot` + `WeaponEnemyLaser`, 800ms) | Wave 5 | 0.05 (5%) |

### Giải thích ý nghĩa các cột thông số:
- **`Size`**: Kích thước chiều rộng × chiều cao (pixel) của Hitbox & Sprite quái.
- **`HP Base`**: Lượng máu gốc ban đầu khi quái xuất hiện lần đầu tiên ở `First Wave`.
- **`Speed Vào`**: Vận tốc di chuyển lao từ mép trên màn hình xuống vị trí hover (`px/s`).
- **`Speed Hover`**: Vận tốc di chuyển đảo ngang lượn trái/phải sau khi đã giữ vị trí chiến đấu (`px/s`).
- **`Vũ khí & Giãn Cách`**: Loại vũ khí quái sử dụng và thời gian giãn cách giữa các đợt bắn (milliseconds).
- **`First Wave`**: Wave đầu tiên loại quái này bắt đầu xuất hiện trong game.
- **`growthRate`**: Tỉ lệ phần trăm tăng trưởng HP theo hàm mũ cho mỗi wave tiếp theo (`0.08` = 8%/wave).

---

## 3. Công Thức Tăng Trưởng HP Hàm Mũ (Dynamic HP Scaling)

$$\text{HP}_{\text{wave}} = \text{HP}_{\text{base}} \times (1 + \text{growthRate})^{(\text{effectiveWaveId} - \text{firstAppearWave})}$$

- Công thức được tính toán trực tiếp trong `EnemyFactory.createFromAction(action, screenW, screenH, effectiveWaveId)`.
- Ví dụ ở Wave 20 (Loop 1):
  - **EnemyBoss**: $100 \times (1 + 0.05)^{(20 - 5)} \approx 207.89 \text{ HP}$.
  - **EnemyShipD**: $8.0 \times (1 + 0.08)^{(20 - 4)} \approx 27.4f \text{ HP}$.
  - **EnemyShipF**: $12.0 \times (1 + 0.08)^{(20 - 16)} \approx 16.32 \text{ HP}$.

---

## 4. Chi Tiết 3 Hệ Vũ Khí Player (7 Level Tiến Trình & Màu Neon)

### 🟡 **Track 0: Plasma Laser (`WeaponShipLaser.java`)**
- **Màu đạn**: Vàng Kim (`#FFD700`) mảnh gọn thanh thoát (6px × 24px).
- **Đặc điểm**: Tốc độ bắn dồn dập (65ms ở Lv 7), góc xoè siêu hẹp tối đa **18°**.
- **Tiến trình**:
  - Lv 1: 1 tia (150ms, 0.8 dmg).
  - Lv 2: 2 tia (130ms, 0.7 dmg, 6° spread).
  - Lv 3: 3 tia (110ms, 0.6 dmg, 10° spread).
  - Lv 4: 4 tia (95ms, 0.55 dmg, 12° spread).
  - Lv 5: 5 tia (85ms, 0.5f dmg, 14° spread).
  - Lv 6: 6 tia (75ms, 0.5f dmg, 16° spread).
  - Lv 7: **7 tia laser mảnh dồn dập** (65ms, 0.5f dmg, 18° spread).

### 🟠 **Track 1: Explosive Blaster (`WeaponExplosiveBlaster.java`)**
- **Màu đạn**: Cam Neon (`#FF6600`) quả cầu năng lượng lớn (22px đến 29px).
- **Đặc điểm**: Sát thương rất cao (`2.5f` -> `4.2f`/viên), tối đa **3 luồng đạn sát nhau với góc xoè siêu nhẹ (max 8°)**.
- **Tiến trình**:
  - Lv 1: 1 quả cầu (280ms, 2.5 dmg).
  - Lv 2–3: 2 quả cầu (220ms, 3.2 dmg, 4° spread).
  - Lv 4–6: 3 quả cầu (150ms, 4.0 dmg, 6-8° spread).
  - Lv 7: **3 quả cầu nổ cực đại sát nhau** (130ms, 4.2 dmg, 8° spread).

### 🟣 **Track 2: Homing Lightning (`WeaponHomingLightning.java`)**
- **Màu đạn**: Tím Neon (`#CC00FF`) phi tiêu sét kích thước **15px**.
- **Đặc điểm**: 2 đến 5 phi tiêu sét tự bẻ cong quỹ đạo đuổi quái (Homing).
- **Tiến trình**:
  - Lv 1–2: 2 phi tiêu (240ms, 0.8 dmg, 1000 speed).
  - Lv 3–4: 3 phi tiêu (180ms, 1.0 dmg, 1150 speed).
  - Lv 5–6: 4 phi tiêu (140ms, 1.2 dmg, 1250 speed).
  - Lv 7: **5 phi tiêu sét tím homing dồn dập** (100ms, 1.4 dmg, 1350 speed).

- **Global Shared Level & Stockpile**: Level (1-7) và Stockpile (0-3) là biến DÙNG CHUNG cho cả 3 loại đạn. Nhặt bất kỳ Item nâng cấp nào đều +1 Level chung và chuyển đạn tương ứng.

---

## 5. Quy Tắc Trừ HP, Bảo Vệ Stockpile & Vòng Khiên Neon Shield Aura

- **Quy tắc Trừ HP**: Khi trúng đạn, **Người chơi LUÔN BỊ TRỪ HP (life -= damage)**.
- **Bảo Vệ Đạn (Stockpile Option A)**:
  - Nếu có Stockpile (`[1★]`, `[2★]`, `[3★]`): Trừ 1 điểm Stockpile nhưng **GIỮ NGUYÊN LEVEL VŨ KHÍ (Lock Lv 7)**.
  - Nếu không có Stockpile: Giảm 1 Level vũ khí (về Lv 6).
- **Vòng Khiên Neon Shield Aura**: Trúng đạn lập tức kích hoạt **Vòng Khiên Neon Shield (Cyan & Gold phát sáng nhấp nháy)** bao quanh tàu trong **1.5 giây bất tử**.

---

## 6. Quy Tắc Hút Vật Phẩm (Item Magnetization Rules)

- **Sao (`ItemStar`)**: Tự động hút khi tàu ở khoảng cách **300px** với tốc độ 1200px/s.
- **Máu (`ItemHP`)**: Siêu siết chặt bán kính hút xuống **90px** ("cho sát mới hút được máu").
- **Item Vũ Khí (`PLASMA`, `EXPLOSIVE`, `HOMING`)**: **KHÔNG HÚT (0px)** để người chơi né tránh nếu không muốn đổi đạn.

---

## 7. Hiệu Ứng Nổ Boss (Boss Radial Star Burst)

- Tiêu diệt Boss kích hoạt **NỔ BUNG 20 SAO toả tròn 360°** văng xa rực rỡ với vận tốc nổ `450 - 750 px/s`.
- Đi kèm 2 Item Nâng Cấp Vũ Khí và 2 Item Máu văng theo 4 hướng chéo.

---

## 8. Cấu Hình Debug Config Central (`DebugConfig.java`)

Khi bật `ENABLE_DEBUG = true`:
- `DEBUG_START_WAVE`: Wave bắt đầu test (1 đến 20).
- `DEBUG_START_WAVE_LOOP_COUNT`: Số vòng loop bắt đầu test (`0` = Loop 1 [Wave 1-20], `1` = Loop 2 [Wave 21-40], `2` = Loop 3 [Wave 41-60]...) để test HP quái scale theo loop.
- `DEBUG_START_HP`, `DEBUG_START_WEAPON_TYPE` (0-2), `DEBUG_START_WEAPON_LEVEL` (1-7).
- Tỉ lệ rơi vật phẩm: `DROP_RATE_WEAPON_UPGRADE = 0.05f` (5%), `DROP_RATE_HP = 0.05f` (5%), `DROP_RATE_STAR = 0.90f` (90%).

---

## 9. Thống Kê Game Over & Lưu Trữ Điểm/Sao Vĩnh Viễn

- **Quy Tắc Tính Điểm & Sao (End-of-Game Only)**:
  - Điểm High Score và Tổng Số Sao chỉ được tính/chốt **KHI XONG 1 GAME (Player chết ở Game Over)**. Không tính realtime hay khi resume/pause.
- **Hiển Thị Main Menu (2 Dòng Tiếng Anh)**:
  - Dòng 1: `HIGH SCORE: XXXX` (Màu vàng kim, điểm kỷ lục cao nhất).
  - Dòng 2: `TOTAL STARS: YYYY` (Màu xanh ngọc cyan, cộng dồn vĩnh viễn qua các lượt chơi).
