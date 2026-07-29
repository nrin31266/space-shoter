# Phase 3: Data-Driven Wave System + Save/Load Architecture
# TÀI LIỆU THIẾT KẾ

> **Trạng thái:** Design — 07/2026
> **Phụ thuộc:** Phase 1 (Refactor) + Phase 2 (Scene2D UI) đã hoàn thành.

---

## 1. Wave Data Architecture

### Nguyên lý cốt lõi
`assets/data/waves.json` là **nguồn dữ liệu duy nhất** (single source of truth) cho toàn bộ wave system.
Không có bất kỳ hard-code wave nào trong Java.

### Cấu trúc dữ liệu phân cấp

```
WaveConfig (root)
  │
  └── List<WaveData>
        │
        └── List<SpawnAction>
```

### SpawnAction fields

| Field       | Type    | Ý nghĩa                                                                 |
|-------------|---------|-------------------------------------------------------------------------|
| `delay`     | float   | Thời gian chờ (giây) từ lúc wave bắt đầu đến khi spawn action này chạy |
| `enemyType` | String  | Loại enemy: `"EnemyShipA"` hoặc `"EnemyShipB"`                         |
| `pattern`   | String  | Pattern spawn: `"LINE"`, `"V_SHAPE"`, `"RANDOM"`                       |
| `count`     | int     | Số lượng enemy spawn trong action này                                   |

Một WaveData chứa danh sách SpawnAction. WaveManager duyệt tuần tự các action theo `delay`.

---

## 2. JSON Design

File: `assets/data/waves.json`

```json
{
  "waves": [
    {
      "waveId": 1,
      "actions": [
        { "delay": 1.0, "enemyType": "EnemyShipA", "pattern": "LINE", "count": 5 },
        { "delay": 6.0, "enemyType": "EnemyShipB", "pattern": "V_SHAPE", "count": 3 }
      ]
    },
    {
      "waveId": 2,
      "actions": [
        { "delay": 1.0, "enemyType": "EnemyShipA", "pattern": "LINE", "count": 6 },
        { "delay": 4.0, "enemyType": "EnemyShipB", "pattern": "V_SHAPE", "count": 4 },
        { "delay": 8.0, "enemyType": "EnemyShipA", "pattern": "RANDOM", "count": 3 }
      ]
    },
    {
      "waveId": 3,
      "actions": [
        { "delay": 1.0, "enemyType": "EnemyShipB", "pattern": "LINE", "count": 4 },
        { "delay": 4.0, "enemyType": "EnemyShipA", "pattern": "V_SHAPE", "count": 5 },
        { "delay": 8.0, "enemyType": "EnemyShipB", "pattern": "RANDOM", "count": 4 }
      ]
    }
  ]
}
```

### Quy tắc mở rộng
- Thêm wave mới: thêm object vào mảng `waves`, tăng `waveId`.
- Thêm enemy mới: thêm string mới vào `enemyType`, cập nhật `EnemyFactory`.
- Thêm pattern mới: thêm string mới vào `pattern`, cập nhật `EnemyFactory.createFormation()`.

---

## 3. WaveManager Design

### Trách nhiệm
WaveManager là **state machine quản lý wave**, KHÔNG spawn enemy trực tiếp.

| Responsibility        | Mô tả                                                        |
|-----------------------|--------------------------------------------------------------|
| Load wave data        | Đọc `waves.json` → `WaveConfig`                              |
| Quản lý thời gian     | `elapsedTime` tăng mỗi frame, so sánh với action `delay`     |
| Trigger spawn event   | Khi `elapsed >= action.delay` → gọi `EnemyFactory.spawn()`    |
| Chuyển wave           | Khi tất cả action đã chạy & enemies đã bị tiêu diệt          |
| Kiểm tra clear        | `isWaveCleared()` → true khi wave finished + no enemies       |

### KHÔNG làm
- Không tạo enemy trực tiếp (delegate cho EnemyFactory)
- Không render
- Không xử lý collision
- Không access Stage/UI

### API

```java
void loadWaves(String jsonPath);       // Đọc waves.json
void startWave(int waveId);           // Bắt đầu wave mới
void update(float deltaTime);         // Update mỗi frame
boolean isWaveCleared();              // Wave đã clear chưa?
boolean isWaveFinished();             // Tất cả action đã spawn chưa?
int getCurrentWaveId();               // Wave hiện tại
boolean hasMoreWaves();               // Còn wave tiếp theo không?
List<SpawnAction> getPendingActions(); // Actions chưa spawn
void reset();                         // Reset về wave 1
```

---

## 4. EnemyFactory Design

### Trách nhiệm
EnemyFactory nhận `SpawnAction` → tạo `List<Unit>` theo pattern.

### API
```java
List<Unit> createFromAction(SpawnAction action, float screenWidth, float screenHeight);
```

### Pattern hỗ trợ

#### LINE
```
[E] [E] [E] [E] [E]
```
Các enemy xếp ngang, cách đều, spawn từ trên cùng màn hình.

#### V_SHAPE
```
       [E]
    [E]    [E]
 [E]          [E]
```
Hình chữ V, mỗi hàng 2 enemy (trừ đỉnh có 1), khoảng cách tăng dần.

#### RANDOM
Mỗi enemy spawn ở vị trí X ngẫu nhiên trong khoảng `[0, screenWidth - enemyWidth]`.

### Sau khi tạo
- Add từng enemy vào `GameState.enemies`
- Wire weapon (gọi `wireEnemyWeapons()` cho EnergyBall)

### Mở rộng tương lai
- `CIRCLE`: enemy xếp vòng tròn
- `BOSS`: spawn 1 boss lớn
- `FORMATION`: đội hình phức tạp (diamond, arrow...)

---

## 5. GamePlay State Machine

```
INTRO ──► PLAYING ──► WAVE_TRANSITION ──► PLAYING ──► ... ──► GAME_OVER
```

### INTRO
- Reset ship position về giữa dưới màn hình
- Reset GameState
- WaveManager.startWave(1)
- Chuyển ngay sang PLAYING

### PLAYING
- WaveManager.update(delta)
- EnemyFactory.spawn() được gọi khi action trigger
- Enemy update + Weapon update
- Collision detection
- Effects update
- Khi `isWaveCleared()` → chuyển WAVE_TRANSITION

### WAVE_TRANSITION
- SaveManager.save()
- Hiển thị text "WAVE X COMPLETE!" trong 2 giây
- Nếu `hasMoreWaves()` → next wave, chuyển PLAYING
- Nếu hết wave → có thể loop hoặc victory screen

### GAME_OVER
- Giữ nguyên logic hiện tại (popup game over)

---

## 6. Save System

### SaveManager dùng LibGDX Preferences

```java
Preferences prefs = Gdx.app.getPreferences("space-shooter-save");
```

### Dữ liệu lưu

| Key          | Type  | Ý nghĩa                    |
|-------------|-------|----------------------------|
| `savedWave` | int   | Wave hiện tại đã lưu       |
| `score`     | long  | Điểm số                    |
| `life`      | float | Máu ship                   |
| `stars`     | long  | Số coin thu thập           |
| `hasSave`   | bool  | Có save data hay không     |

### Thời điểm save
- Hoàn thành wave (WAVE_TRANSITION)
- Pause game
- Game over

### API
```java
void save(int wave, long score, float life, long stars);
SaveData load();                    // Trả về null nếu không có save
void clear();                       // Xóa save
boolean hasSavedGame();
```

---

## 7. Luồng dữ liệu mới

```
waves.json ──► WaveConfig ──► WaveManager
                                  │
                                  │ getPendingActions()
                                  ▼
                            EnemyFactory
                                  │
                                  │ List<Unit>
                                  ▼
                            GameState.enemies
                                  │
                                  ▼
                       GamePlayScreen (update + render)
```

## 8. Cách mở rộng

### Thêm wave mới
Chỉ cần sửa `waves.json`, thêm object mới vào mảng `waves`.

### Thêm enemy mới (vd: EnemyShipC)
1. Tạo class `EnemyShipC extends Unit`
2. Thêm case trong `EnemyFactory.createEnemy()`
3. Dùng `"EnemyShipC"` trong `waves.json`

### Thêm pattern mới (vd: CIRCLE)
1. Thêm method `createCircleFormation()` trong `EnemyFactory`
2. Thêm case trong `EnemyFactory.createFormation()`
3. Dùng `"CIRCLE"` trong `waves.json`

### Thêm difficulty
1. Tạo `waves_easy.json`, `waves_hard.json`
2. WaveManager chọn file dựa trên difficulty setting

### Thêm boss
1. Tạo `BossShip extends Unit`
2. Thêm `"BOSS"` pattern trong EnemyFactory
3. WaveManager có thêm logic `isBossWave()`
