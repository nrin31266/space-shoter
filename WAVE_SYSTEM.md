# WAVE_SYSTEM.md — Wave, Enemy & Formation System

> Nguồn sự thật cho toàn bộ wave/enemy/spawn logic. Đối chiếu trực tiếp với
> `EnemyFactory.java`, `WaveManager.java`, `waves.json` (07/2026).
> ⚠️ Các file `phase3_wave_system_design.md` và `wave_system_reference.md` cũ (LINE/V_SHAPE, 6 wave, loop)
> đã lỗi thời — không dùng nữa, giữ lại chỉ để tham khảo lịch sử thiết kế.

---

## 1. `waves.json` — schema thật (đã xác nhận qua code)

```json
{ "delay": 0.5, "enemyType": "EnemyShipA", "pattern": "GRID", "count": 16, "hoverYPct": 0.55 }
```

| Field | Type | Ý nghĩa | Bắt buộc? |
|---|---|---|---|
| `delay` | float | Giây kể từ lúc wave **bắt đầu** (không phải từ action trước) đến khi action trigger | Có |
| `enemyType` | String | `EnemyShipA` / `B` / `C` / `D` / `BOSS` | Có |
| `pattern` | String | Xem mục 3 | Có |
| `count` | int | Số quái muốn spawn | Có — **NHƯNG bị ignore hoàn toàn với pattern `BOSS`, xem BUG #1** |
| `hoverYPct` | float | % chiều cao màn hình nơi quái dừng hover (0 = đỉnh, 1 = đáy) | Không — mặc định `-1f` nếu absent → factory tự chọn theo loại quái (xem mục 4) |

**Actions trong mỗi wave phải sắp xếp `delay` tăng dần** — `WaveManager.update()` duyệt tuần tự và `break` ngay khi gặp action chưa tới giờ, không skip để tìm action sau.

Hiện có **15 wave** trong `waves.json` (wave 1→15, tăng dần độ khó, có 5 lần xuất hiện BOSS ở wave 5, 10 (×2), 15 (×3 theo count nhưng xem BUG #1)).
`WaveManager.hasMoreWaves()` = `false` sau wave 15 (không tự loop — hành vi loop-về-wave-1 mô tả ở doc cũ **không còn đúng**, cần GamePlayScreen tự quyết định làm gì khi hết wave 15).

---

## 2. WaveManager — vòng đời 1 wave (đã verify qua code)

startWave(id)
→ elapsedTime=0, currentActionIndex=0, allActionsTriggered=false,
waveCleared=false, totalEnemiesSpawned=0

update(deltaTime_ms, screenW, screenH) [mỗi frame]
→ elapsedTime += deltaTime/1000
→ while (action hiện tại có delay <= elapsedTime): trigger → EnemyFactory.createFromAction()
→ cộng dồn totalEnemiesSpawned, return List<Unit> mới cho GamePlayScreen add vào GameState
→ khi hết action: allActionsTriggered = true

markWaveCleared() ← PHẢI được GamePlayScreen gọi thủ công khi:
isWaveFinished() && enemies.isEmpty() && totalEnemiesSpawned > 0
(WaveManager không tự kiểm tra enemies rỗng — không có tham chiếu tới GameState.enemies)

Guard `totalEnemiesSpawned > 0` để tránh false-clear nếu action list rỗng do JSON parse lỗi.

---

## 3. Pattern hỗ trợ (EnemyFactory — đã verify, có NHIỀU pattern hơn doc cũ tưởng)

Factory hỗ trợ **7 pattern** cùng lúc (cả bộ cũ lẫn mới), nhưng `waves.json` hiện tại chỉ dùng `GRID / CHEVRON / INTERLEAVED_ROWS / RANDOM / BOSS`:

| Pattern | Trạng thái dùng trong waves.json | Mô tả logic thật |
|---|---|---|
| `GRID` | ✅ Đang dùng | `cols = ceil(sqrt(count))`, xếp theo hàng, mỗi hàng spawn cao hơn hàng trước `screenHeight*0.1`/hàng |
| `CHEVRON` | ✅ Đang dùng | Dàn ngang đều theo `screenWidth/count`; quái càng xa tâm càng spawn cao hơn (`distFromCenter * screenHeight*0.08`) → tạo hình V khi bay vào |
| `INTERLEAVED_ROWS` | ✅ Đang dùng | Cố định **4 hàng**; hàng chẵn dùng `action.enemyType`, hàng lẻ dùng `action.secondaryEnemyType` nếu có, ngược lại fallback về `"EnemyShipA"` (hành vi mặc định / legacy). Xem field `secondaryEnemyType` trong schema mục 1. |
| `RANDOM` | ✅ Đang dùng | Chia màn hình thành `count` cột, mỗi quái random X trong cột của nó |
| `BOSS` | ✅ Đang dùng | Spawn đúng **`count` boss** dàn đều ngang màn hình (BUG #1 đã sửa). Mỗi boss được stagger Y `screenHeight*0.05*i` để không vào cùng lúc |
| `LINE` | Code hỗ trợ, JSON hiện không dùng | Dàn hàng ngang đều `screenWidth/count`, jitter nhẹ |
| `V_SHAPE` | Code hỗ trợ, JSON hiện không dùng | Hình V cổ điển, đỉnh 1 quái, mỗi hàng sau +2, spread `screenWidth*0.15*row` |

Pattern không khớp case nào → fallback về `RANDOM` (log lỗi `Unknown pattern`).

---

## 4. `applyHoverYPct()` — logic hover thật

baseHover = hoverYPct * screenHeight (nếu action.hoverYPct != -1)
hoặc default theo loại quái nếu absent:
EnemyShipA → 0.60 EnemyShipB → 0.65
EnemyShipC → 0.55 EnemyShipD → 0.75
finalHover = baseHover + spawnOffset (chênh lệch Y do formation stagger)
finalHover = min(finalHover, screenHeight - 120) ← safety clamp CHỈ CHẶN TRÊN

⚠️ **Không có clamp chặn dưới** — ~~nếu `hoverYPct` đặt quá thấp (gần 0) kết hợp `spawnOffset` âm lớn, về lý thuyết có thể hover quá sát top màn hình phía dưới không đúng ý đồ~~. **ĐÃ SỬA (BUG #3)**: thêm `Math.max(finalHover, screenHeight * 0.35f)` — clamp dưới tại 35% chiều cao màn hình.
⚠️ **BOSS hoàn toàn không áp dụng hoverYPct** — code có nhánh kiểm tra riêng trong `createBossFormation()` nhưng **comment thẳng: "EnemyBoss does not have setHoverY, so we ignore hoverYPct for it"**. Field `hoverYPct` trong các action BOSS của `waves.json` (thực ra hiện tại còn không có field này ở BOSS action) là vô nghĩa — không cần thêm.

---

## 5. Bảng thông số enemy (đã verify qua entity code — 07/2026)
| Enemy | Màu | Size | HP | Enter Speed | Hover Speed | Vũ khí | Xuất hiện từ |
|---|---|---|---|---|---|---|---|
| EnemyShipA | `#5DBBFF` (ice blue) | 60×60 | 1 | 380 ±12% | 70 (sin drift) | **WeaponEnemyLaser** (1 đạn thẳng xuống, fire rate **8000ms**) | Wave 1 |
| EnemyShipB | `CHARTREUSE` | 70×70 | 5 | 260 ±12% | 55 (sin drift) | WeaponEnergyBallA (nhắm player, fire rate **8000ms**) | Wave 2 |
| EnemyShipC | `#FF4FF4` (hot pink) | 44×44 | 1.5 | 450 ±15% | 95 (sin drift) | WeaponSniperBeam (nhắm player, 900px/s, fire rate **3500ms**) | Wave 3 |
| EnemyShipD | `#FF7700` (orange) | 90×90 | 8 | 170 ±10% | 40 (sin drift) | WeaponSpreadShot (3 đạn fan ±22°, fire rate **3000ms**) | Wave 4 |
| BOSS (EnemyBoss) | `#FF0055` (crimson) | 250×250 | 100 | 150 (enter), 100 (hover ngang) | 100 | WeaponSpreadShot + WeaponEnemyLaser (fire rate **3000ms / 8000ms**) | Wave 5, 10, 15 |

> **Ghi chú EnemyShipA**: ShipA có WeaponEnemyLaser bắn thẳng xuống với fire rate 6000ms. Không nhắm player.
> **Ghi chú EnemyBoss**: drop guaranteed khi chết — 1 ItemWeaponUpgrade + 1 ItemHP. Flag `hasDropped` đảm bảo chỉ drop đúng 1 lần.

---

## 6. 🐞 BUGS — trạng thái sau khi fix (07/2026)

1. **~~BOSS action bỏ qua `count`~~** — ✅ **ĐÃ SỬA** (`EnemyFactory.createBossFormation()`). Giờ spawn đúng `action.count` boss dàn đều ngang màn hình, stagger Y mỗi boss `screenHeight*0.05*i`. Wave 15 action cuối `count:2` → spawn 2 boss.
2. **`INTERLEAVED_ROWS` hàng lẻ** — ✅ **ĐÃ DOCUMENT + THÊM FIELD**. Hành vi fallback `EnemyShipA` cho hàng lẻ được giữ nguyên. Thêm field optional `secondaryEnemyType` trong `SpawnAction`/JSON để override nếu muốn. Waves.json cũ không cần thay đổi.
3. **~~`applyHoverYPct` không clamp chặn dưới~~** — ✅ **ĐÃ SỬA**. Thêm `Math.max(finalHover, screenHeight * 0.35f)`. Không ảnh hưởng balance vì giá trị nhỏ nhất hiện có trong JSON là `0.45` (>35%).

**Bugs bổ sung từ audit — đã sửa:**
- N1: Boss double-drop → ✅ **ĐÃ SỬA** (flag `hasDropped` trong `EnemyBoss`).
- N3: `dropStars()` NPE khi `SpaceShooter.items == null` → ✅ **ĐÃ SỬA** (null-guard + log).
- N4: `createFromAction()` NPE khi `action.pattern == null` → ✅ **ĐÃ SỬA** (null-guard + fallback RANDOM).
- N5: `INTERLEAVED_ROWS` mất quái khi `count % 4 != 0` → ✅ **ĐÃ SỬA** (distribute remainder to first rows).
- N8: `Unit.addLife()` không clamp → ✅ **ĐÃ SỬA** (clamp `[0, maxLife]` ngay trong method).
- L1: Dead code slow-motion trong `GameState` → ✅ **ĐÃ XÓA** (`gameSpeed`, `SLOW_MO_GAME_SPEED_LIMIT`, `SLOW_DOWN_PERIOD`, `screenTouched`).

**Bugs/items chưa sửa (để sau):**
- C1: Boss hover velocity dùng `HOVER_SPEED * hoverDir / SpaceShooter.FPS` hard-coded — không nhất quán với các ship khác nhưng không ảnh hưởng gameplay.
- L2/L3/L4: AudioManager không giới hạn số Sound instance; WeaponShipLaser timer reset khi thay đổi level; touch Y flip comment.

## 7. Việc cần làm tiếp & Hướng dẫn Debug Test
- [x] ~~Đọc `EnemyBoss.java` để lấy HP/speed/pattern bắn thật~~ — đã điền vào bảng mục 5.
- [x] ~~Quyết định sửa BUG #1 hay sửa waves.json~~ — đã sửa `createBossFormation()` để tôn trọng `count`.
- [x] ~~Xác nhận BUG #2 là chủ đích hay lỗi~~ — giữ behaviour cũ, thêm field `secondaryEnemyType`.
- [x] ~~Viết test/playtest wave 9 action 1~~ — clamp dưới 35% đã added (BUG #3).
- [x] ~~Cơ chế Debug Test tập trung~~ — Đã tạo `DebugConfig.java` trong `utils/` (bật `ENABLE_DEBUG = true` để chọn wave start, HP, weapon level).
- [x] ~~Tiếng rè nhạc nền~~ — Root cause: `ut.mp3` và `action_music.mp3` có peak = 0.0 dBFS (clipping). Đã re-encode bằng ffmpeg `-filter:a volume=-3dB`, peak hiện ~-3 dBFS. File gốc backup `*.mp3.bak`.
- [x] ~~Item khó nhìn~~ — Vẽ lại bằng `ShapeRenderer`: vtâm có icon `+` (HP) và `↑` (Upgrade), kít cỡ 18px, glow ring nổi bật.
- [ ] Playtest thực tế wave 10/15 với 1/2 boss để xác nhận vị trí spawn không overlap.
- [ ] Xem xét thêm pity system (guaranteed weapon upgrade trước boss wave) — feature proposal.
- [ ] Health bar cho Boss (player không biết HP boss còn bao nhiêu).