# ARCHITECTURE.md — Space Shooter (LibGDX)

> Nguồn sự thật duy nhất về kiến trúc & trạng thái refactor. Cập nhật khi có thay đổi cấu trúc lớn.
> Cập nhật lần cuối: 08/2026, mở rộng Phase 4 (20 waves, 6 loại quái, 3 loại đạn độc lập, 7 cấp độ, Stockpile Option A, Save Schema mới).

---

## 1. Tổng quan
Game bắn súng không gian 2D, LibGDX, OOP. Đã trải qua 4 phase refactor & mở rộng:
- **Phase 1**: Phá God Class `SpaceShooter.java`, chuyển sang Screen-based (`MainGame extends Game`).
- **Phase 2**: Scene2D UI (Loading %, MainMenu, HUD, GameOver popup, Settings Dialog).
- **Phase 3**: Data-driven Wave System (JSON 15 wave) + Pause Menu, xóa slow-motion.
- **Phase 4 (08/2026)**: Mở rộng 20 Wave, 6 loại quái (bổ sung `ShipE`, `ShipF`), 3 hệ vũ khí (Plasma Laser, Explosive Blaster, Homing Lightning) với 7 cấp độ tiến trình, cơ chế Bảo vệ Stockpile (Option A), Boss Health Bar, Vòng Khiên Neon Shield Aura, Nổ bung 20 Sao diệt Boss, High Score & Total Stars Tiếng Anh tích luỹ trên Main Menu, và Save/Load tương thích ngược. Tham khảo thêm tại [SYSTEM.md](file:///home/nrin31266/IdeaProjects/space-shooter/SYSTEM.md).

Trạng thái: 40+ file `.java`, 7 package, 0 lỗi biên dịch.

## 2. Class Hierarchy

Visual.java → position, velocity, size, color, direction, speed. AABB collision. static list hiệu ứng toàn cục.
└─ Unit.java → life, maxLife, List<Weapon>, isDenseAction, pityWeaponType, flash/damagePoints khi trúng đạn.
   ├─ Ship.java → player. Quản lý 3 track vũ khí, 7 level, stockpile (Option A, bất tử 1.5s), HP (5 ban đầu, 10 max).
   ├─ EnemyShipA/B/C/D → quái thường / tank / sniper.
   ├─ EnemyShipE (Mới) → Fast Striker (Size 52x52, Base HP 3.0, Tím `#AA00FF`, bắn `WeaponDoublePulse`).
   ├─ EnemyShipF (Mới) → Heavy Dragoon (Size 85x85, Base HP 12.0, Vàng `#FFD700`, bắn `WeaponRingBurst`).
   └─ EnemyBoss → trùm, spawn qua pattern "BOSS" trong EnemyFactory.
└─ Projectile.java → đạn bay, hỗ trợ isHoming.
Item.java / ItemStar.java → vật thu thập.
├─ ItemHP.java → hồi 1 HP (vòng tròn viền đỏ, dấu `+` đỏ).
├─ ItemWeaponUpgrade.java → nâng cấp Plasma Laser (vòng cyan, tam giác xanh).
├─ ItemWeaponUpgradeExplosive.java (Mới) → nâng cấp Explosive Blaster (vòng cam-đỏ, thoi cam).
└─ ItemWeaponUpgradeHoming.java (Mới) → nâng cấp Homing Lightning (vòng tím, sao tím).


## 3. Package Structure

com.alexei.spaceshooter/
├── MainGame.java (entry point, AssetManager)
├── SpaceShooter.java (static bridge cho items, audio, enemies)
├── Starfield.java (parallax 2 lớp, object pooling)
├── screen/ LoadingScreen, MainMenuScreen, GamePlayScreen
├── manager/ AudioManager, GameState, WaveManager, SaveManager
├── factory/ EnemyFactory, WeaponFactory
├── data/wave/ WaveConfig, WaveData, SpawnAction
├── entity/ Visual, Unit, Ship, EnemyShipA-F, EnemyBoss, Projectile, Item, ItemStar, ItemHP, ItemWeaponUpgrade, ItemWeaponUpgradeExplosive, ItemWeaponUpgradeHoming
├── weapon/ Weapon, WeaponShipLaser, WeaponExplosiveBlaster, WeaponHomingLightning, WeaponShipRocket, WeaponEnemyLaser, WeaponEnergyBallA, WeaponSpreadShot, WeaponDoublePulse, WeaponRingBurst
├── effect/ Effect, EffectExplosion, EffectFlash, EffectSparks, Particle, ParticleEmitter
└── utils/ Utils, Timer, TouchData, ScoreTracker, SoundName, SoundType, DebugConfig, CustomUI, FontUtil


## 4. Screen Flow & Game State Machine

LoadingScreen → MainMenuScreen → GamePlayScreen → (Game Over / Endless Loop) → MainMenuScreen

- **GamePlayScreen**:
  - State machine: `INTRO → PLAYING → WAVE_TRANSITION → PLAYING → ... → GAME_OVER / ENDLESS LOOP`.
  - **Endless Loop Mode**: Khi hoàn thành Wave 20, game tự động loop về Wave 1 kèm tăng biến `waveLoopCount`. HP quái tiếp tục tăng theo `effectiveWaveId = (waveLoopCount * 20) + waveId` bằng công thức hàm mũ.
  - **Boss HP Bar**: Thanh HP trùm cố định mép trên UI hiển thị % tổng HP các Boss đang sống (ví dụ `BOSS x3 (75%)`).
  - **Pity System**: Tại đợt quái cuối các wave trước Boss (Wave 4, 9, 14, 18, 19), 1 quái được gắn `pityWeaponType` đảm bảo rơi 1 Item nâng cấp thuộc track vũ khí người chơi đang cầm.

## 5. Vũ khí & Tiến Trình 7 Cấp (Global Shared Level)

| Track | Loại Vũ Khí | Item Đại Diện | Màu Đạn | Đặc Điểm Cân Bằng (Lv 1->7) |
|---|---|---|---|---|
| **0** | **Plasma Laser** | ItemWeaponUpgrade (Cyan Triangle) | Vàng Kim (`#FFD700`) | Bắn 1-7 tia mảnh dồn dập (65ms ở Lv 7), sát thương mỗi tia nhỏ (0.5f) |
| **1** | **Explosive Blaster** | ItemWeaponUpgradeExplosive (Orange Diamond) | Cam Neon (`#FF6600`) | Bắn 1-3 quả cầu plasma lớn (22-29px), sát thương rất cao (2.5f-4.2f), góc xoè siêu nhẹ (max 8°) |
| **2** | **Homing Lightning** | ItemWeaponUpgradeHoming (Purple Star) | Tím Neon (`#CC00FF`) | Bắn 2-5 phi tiêu sét tím size 15px tự bẻ cong đuổi quái |

- **Endless Wave Loop**: Đánh hết Wave 20 tự động quay về Wave 1 với độ khó/máu quái tăng tiến liên tục.
- **Neon Shield Aura**: Khi trúng đạn, tàu phát sáng **Vòng Khiên Neon Shield Aura (Vàng & Cyan)** nhấp nháy 1.5s bảo vệ tàu.
- **Stockpile Damage Rule**: Trúng đạn **LUÔN TRỪ HP**; 1 Stockpile charge tiêu tốn để bảo vệ đạn không bị rớt cấp từ Lv 7.
- **Boss Radial Star Burst**: Tiêu diệt Boss kích hoạt **Nổ bung 20 Sao toả tròn 360°** văng xa siêu ấn tượng.
- **Global Shared Weapon Level & Stockpile**: Level đạn (1-7) và Stockpile (0-3) được dùng chung cho tất cả các loại vũ khí. Nhặt bất kỳ Item nâng cấp đạn nào đều tăng 1 Level chung và chuyển sang loại vũ khí đó.
- **Single Active Weapon Enforcer**: `Ship.java` chỉ đăng ký duy nhất 1 vũ khí active vào danh sách khai hoả (`getWeapons().clear()`), loại bỏ hoàn toàn hiện tượng bắn gộp 3 loại đạn.
- **Anti-Aliased Neon Rendering**: Tất cả các loại đạn (quái + player) được vẽ bằng 4 lớp anti-aliased neon glow rực rỡ với `GL_BLEND` và lõi trắng siêu nét.

- **HP Scaling Hàm Mũ**: $\text{HP}_{\text{wave}} = \text{HP}_{\text{base}} \times (1 + \text{growthRate})^{(\text{waveId} - \text{firstWave})}$.
- **Cycle Skip Probability (20%)**: Quái xuất hiện từ action có `count > 20` (`isDenseAction == true`) có 20% xác suất bỏ qua lượt bắn ở mỗi chu kỳ để tản mát tường đạn.

## 6. Save/Load System (Schema Mới & Backward Compatibility)
Sử dụng `Preferences("space-shooter-save")` lưu trữ:
- `savedWave`, `score`, `life`, `stars`
- `activeWeaponType` (0, 1, 2)
- `weaponLevel_0`, `weaponLevel_1`, `weaponLevel_2` (1-7)
- `stockpile_0`, `stockpile_1`, `stockpile_2` (0-3)
- `maxLife` (10f)

*Tự động fallback giá trị mặc định cho file save từ bản cũ mà không gây crash app.*

## 7. Central Debug Config (DebugConfig.java)
- Bật `ENABLE_DEBUG = true` để tùy chỉnh `DEBUG_START_WAVE` (1-20), `DEBUG_START_HP`, `DEBUG_START_WEAPON_TYPE` (0-2), `DEBUG_START_WEAPON_LEVEL` (1-7).
- Tùy chỉnh tỉ lệ rơi vật phẩm: `DROP_RATE_WEAPON_UPGRADE` (15%), `DROP_RATE_HP` (5%), `DROP_RATE_STAR` (90%).
- Bật `DEBUG_TEST_SINGLE_ENEMY = true` để playtest riêng `EnemyShipE` hoặc `EnemyShipF` với số lượng ít (3-5 con) và HP nhân 5x.