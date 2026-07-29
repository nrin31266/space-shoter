# Wave System — Design & Mechanics Reference

> Cập nhật: 2026-07-29

---

## 1. Cấu trúc dữ liệu wave (`waves.json`)

```json
{
  "waves": [
    {
      "waveId": 1,
      "actions": [
        { "delay": 0.5,  "enemyType": "EnemyShipA", "pattern": "LINE",    "count": 8 },
        { "delay": 8.0,  "enemyType": "EnemyShipA", "pattern": "V_SHAPE", "count": 7 }
      ]
    },
    ...
  ]
}
```

| Field | Ý nghĩa |
|-------|---------|
| `waveId` | ID của wave, đánh số từ 1 |
| `delay` | Số giây kể từ khi wave **bắt đầu** thì action này trigger (không phải delay từ action trước) |
| `enemyType` | Loại quái: `EnemyShipA` / `EnemyShipB` / `EnemyShipC` / `EnemyShipD` |
| `pattern` | Kiểu đội hình: `LINE` / `V_SHAPE` / `RANDOM` |
| `count` | Số quái spawn |

---

## 2. Vòng đời của một Wave

```
WaveManager.startWave(waveId)
    ↓ elapsedTime = 0, allActionsTriggered = false, totalEnemiesSpawned = 0
    
WaveManager.update(deltaTime, ...) [mỗi frame]
    ↓ elapsedTime += deltaTime / 1000f  (convert ms → giây)
    ↓ Với mỗi SpawnAction chưa trigger:
         if (elapsedTime >= action.delay) → EnemyFactory.createFromAction() → spawn enemies
    ↓ Khi tất cả actions đã trigger: allActionsTriggered = true

GamePlayScreen kiểm tra mỗi frame:
    if (isWaveFinished() && enemies.isEmpty() && totalEnemiesSpawned > 0)
        → Wave cleared!
```

> **Guard quan trọng:** `totalEnemiesSpawned > 0`  
> Nếu `waves.json` parse lỗi trả về actions rỗng thì `allActionsTriggered = true` ngay lập tức.  
> Guard này đảm bảo wave chỉ được coi là "hoàn thành" khi ít nhất 1 quái đã thực sự được spawn.

---

## 3. Cơ chế chuyển wave (loop)

Khi **hết wave cuối** (wave 6), game **loop về wave 1** — không có màn hình kết thúc game:

```java
// GamePlayScreen.java
int nextId = waveManager.hasMoreWaves() ? state.currentWaveId + 1 : 1;
launchWave(nextId, true);
```

`hasMoreWaves()` trả về `false` khi `currentWaveId + 1` không tồn tại trong config.

---

## 4. Flow đầy đủ: Từ New Game → Gameplay

```
MainMenuScreen [Nhấn NEW GAME]
    ↓
GamePlayScreen.show()
    ↓ introActive = true, wave CHƯA bắt đầu
    
INTRO SEQUENCE (3.5 giây):
    ├─ 0.0s → 2.8s : "GET READY!" + "HOLD SCREEN TO SHOOT" overlay
    ├─ 2.8s         : waveManager.startWave(1) — enemies bắt đầu spawn theo delay trong JSON
    │                 "WAVE 1" overlay slide xuất hiện
    └─ 3.5s         : introActive = false → vào PLAYING bình thường

PLAYING:
    ├─ Enemies spawn theo action.delay
    ├─ Player bắn (chỉ khi đang chạm màn)
    ├─ Khi kill hết enemies trong wave:
    │       pendingNextWave = true, nextWaveDelay = 2.2s
    │       "WAVE CLEAR!" overlay xuất hiện
    └─ Sau 2.2s:
            launchWave(nextWaveId)
            "WAVE X" overlay slide xuất hiện
            Enemies bắt đầu spawn sau delay 0.5s (action đầu tiên)
```

---

## 5. Đội hình (Patterns)

| Pattern | Mô tả | Spacing |
|---------|-------|---------|
| `LINE` | Hàng ngang phân bố đều chiều rộng màn hình | `screenWidth / count` mỗi slot |
| `V_SHAPE` | Hình V đỉnh ở trên, mở rộng ra theo từng hàng | Spread = `screenWidth * 0.15f * rowIndex` |
| `RANDOM` | Chia màn hình thành `count` cột, random X trong mỗi cột | Tránh overlap nặng |

> Tất cả đội hình đều tỷ lệ theo `screenWidth` và `screenHeight` — không hardcode pixel.

---

## 6. Các loại quái

| Enemy | Màu | Size | HP | Speed | Vũ khí | Hover Y | Xuất hiện |
|-------|-----|------|----|-------|--------|---------|-----------|
| **EnemyShipA** | Ice blue (`5DBBFF`) | 60×60 | 1 | 380 | Không | 60% màn hình | Wave 1+ |
| **EnemyShipB** | Chartreuse (xanh lá vàng) | 70×70 | 5 | 260 | Energy ball (nhắm player) | 70% | Wave 2+ |
| **EnemyShipC** | Hot pink (`FF4FF4`) | 44×44 | 1.5 | 450 | Sniper beam (nhắm player) | 55% | Wave 3+ |
| **EnemyShipD** | Orange (`FF7700`) | 90×90 | 8 | 170 | Spread 3 đạn (xuống) | 75% | Wave 4+ |

**Hover Y** = vị trí tính từ đáy màn hình mà enemy sẽ dừng lại và bắt đầu drift ngang.

---

## 7. Cơ chế bắn đạn (Touch-to-Shoot)

- **Player**: Tàu sẽ **luôn luôn bắn đạn** bất cứ khi nào người chơi đang chạm (giữ tay) vào màn hình (`screenTouched == true`), kể cả lúc đang ở trạng thái chuẩn bị vào wave (GET READY) hay lúc chuyển wave (WAVE CLEAR).
- **Kéo di chuyển**: Kéo ngón tay để điều khiển tàu.
- **Enemies**: Tự động bắn theo fire rate của vũ khí (auto-fire).

Khi nhả tay, weapon của player bị disable (`Weapon.setEnabled(false)`) — tàu sẽ ngừng bắn.

---

## 8. Lưu game

`SaveManager` tự động lưu khi:
- Nhấn nút Pause
- Hết wave (wave complete)
- Tàu chết (game over)

Dữ liệu lưu: `waveId`, `score`, `life`, `starsCollected`.  
Khi **Continue** từ main menu, game load và bắt đầu từ wave đã lưu (không qua intro).

---

## 9. Vấn đề đã biết & giới hạn

- **Âm thanh rè**: LibGDX Sound có thể bị distort khi quá nhiều sound instance chạy cùng lúc (nhiều laser + enemies bắn). Không phải bug code — là giới hạn của AudioManager hiện tại.
- **Wave loop**: Sau wave 6 thì loop lại từ wave 1. Chưa có màn "chiến thắng" riêng.
- **Difficulty scaling**: Độ khó được hardcode trong `waves.json` — chưa có dynamic scaling theo thời gian chơi.
