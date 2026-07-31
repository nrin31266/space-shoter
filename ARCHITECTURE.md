# ARCHITECTURE.md — Space Shooter (LibGDX)

> Nguồn sự thật duy nhất về kiến trúc & trạng thái refactor. Cập nhật khi có thay đổi cấu trúc lớn.
> Cập nhật lần cuối: 07/2026, đã đối chiếu với source thật (EnemyFactory.java, WaveManager.java).

---

## 1. Tổng quan
Game bắn súng không gian 2D, LibGDX, OOP. Đã trải qua 3 phase refactor:
- **Phase 1**: Phá God Class `SpaceShooter.java`, chuyển sang Screen-based (`MainGame extends Game`).
- **Phase 2**: Scene2D UI (Loading %, MainMenu, HUD, GameOver popup).
- **Phase 3**: Data-driven Wave System (JSON) + Pause Menu, xóa slow-motion.

Trạng thái: 35+ file `.java`, 7 package, 0 lỗi biên dịch (tính đến 07/2026).

## 2. Class Hierarchy

Visual.java → position, velocity, size, color, direction, speed. AABB collision. static list hiệu ứng toàn cục.
└─ Unit.java → life, maxLife, List<Weapon>, flash/damagePoints khi trúng đạn.
├─ Ship.java → player. Rung (vibration), cảnh báo máu thấp (alarm).
├─ EnemyShipA/B/C/D → quái, xem WAVE_SYSTEM.md mục "Bảng thông số enemy".
└─ EnemyBoss → boss, spawn qua pattern "BOSS" trong EnemyFactory.
└─ Projectile.java → đạn bay, hỗ trợ isHoming.
Item.java / ItemStar.java → vật thu thập, vẽ bằng Pixmap texture cache (KHÔNG còn dùng EarClippingTriangulator).


## 3. Package structure

com.alexei.spaceshooter/
├── MainGame.java (entry point, AssetManager)
├── SpaceShooter.java (static bridge — nợ kỹ thuật giữ lại có chủ đích, xem mục 5)
├── Starfield.java (parallax 2 lớp, object pooling)
├── screen/ LoadingScreen, MainMenuScreen, GamePlayScreen
├── manager/ AudioManager, GameState, WaveManager, SaveManager
├── factory/ EnemyFactory, WeaponFactory
├── data/wave/ WaveConfig, WaveData, SpawnAction
    │  `SpawnAction` fields: `delay`, `enemyType`, `pattern`, `count`, `hoverYPct`(-1=default),
    │  `secondaryEnemyType`(null=default, dùng cho INTERLEAVED_ROWS hàng lẻ — xem WAVE_SYSTEM.md mục 3)
├── entity/ Visual, Unit, Ship, EnemyShipA-D, EnemyBoss, Projectile, ProjectileRocket, Item, ItemStar
├── weapon/ Weapon, WeaponShipLaser, WeaponShipRocket, WeaponEnergyBallA
├── effect/ Effect, EffectExplosion, EffectFlash, EffectSparks, Particle, ParticleEmitter
└── utils/ Utils, Timer, TouchData, ScoreTracker, SoundName, SoundType, DebugConfig

Đã xóa: `GameModeManager.java`.

## 4. Screen Flow

LoadingScreen → MainMenuScreen → GamePlayScreen → (Game Over) → MainMenuScreen (loop)

- **GamePlayScreen** state machine: `INTRO → PLAYING → WAVE_TRANSITION → PLAYING → ... → GAME_OVER`.
- **INTRO** (~3.5s): "GET READY!" overlay → `waveManager.startWave(1)` tại 2.8s → chuyển PLAYING tại 3.5s.
- **PLAYING**: `waveManager.update(deltaTime, screenW, screenH)` mỗi frame trả về `List<Unit>` cần thêm vào `GameState.enemies`. GamePlayScreen tự kiểm tra `isWaveFinished() && enemies.isEmpty() && totalEnemiesSpawned>0` rồi gọi `markWaveCleared()`.
- Touch-to-shoot: player chỉ bắn khi đang chạm màn hình, kể cả lúc GET READY / WAVE CLEAR. Nhả tay → `Weapon.setEnabled(false)`.
- **Slow-motion: ĐÃ XÓA** (Phase 3) — game luôn chạy tốc độ thực. Dead code (`gameSpeed`, `SLOW_MO_GAME_SPEED_LIMIT`, `SLOW_DOWN_PERIOD`, `screenTouched`) đã dọn khỏi `GameState.java` (L1 fix 07/2026).
- **Hết wave 15**: game loop về wave 1 (`GamePlayScreen` tự quyết định: `int nextId = hasMoreWaves() ? currentWaveId+1 : 1`). Không có màn hình "ALL WAVES COMPLETE" — behaviour có chủ đích (N2 behaviour, không phải bug).
- **Continue từ save**: khi `continueFromSave=true`, `GamePlayScreen` gọi `launchWave(id, false)` → bỏ qua màn hình wave announcement. Behaviour có chủ đích (N7 behaviour, không phải bug).

## 5. Nợ kỹ thuật đã biết (có chủ đích, chưa dọn)
- `SpaceShooter.java` giữ lại làm **static bridge**: `acquireTarget()`, `isTargetDead()`, `playSound()`, `playMusic()`, hằng số `GROUND_SCROLL_SPEED`, list `items` — tất cả delegate sang instance thật do `GamePlayScreen.show()` set (`setActiveEnemiesList`, `setStaticAudioManager`, `setActiveItemsList`). Không dùng thêm static field mới ngoài các bridge này.
- `Ship` đã xóa hết static field cũ (`position/size/center/ship`), nhận `AudioManager` qua setter.
- `ProjectileRocket` dùng `getTarget()` (instance), `WeaponEnergyBallA` dùng `setTarget(Visual)` được `GamePlayScreen` gọi mỗi frame.
- `Weapon.update(deltaTime, projectiles)` nhận list từ ngoài — không còn `static projectiles`.

## 6. Item / Vũ khí / Âm thanh (Không liên quan wave — xem WAVE_SYSTEM.md cho phần wave)
- **Weapon Level 1-5** (max 5), tia laser đồng bộ màu Vàng:
  | Lv | Số tia | Fire delay |
  |----|--------|-----------|
  | 1 | 1 | 200ms |
  | 2 | 1 | 120ms |
  | 3 | 3 | 200ms |
  | 4 | 3 | 120ms |
  | 5 | 5 | 120ms |
- **Item physics**: Ngôi Sao (điểm) rơi nhanh + nảy; Item HP/Nâng cấp đạn rơi êm (gravity riêng, dễ nhặt). Icon nâng cấp đạn: Double Chevron viền Cyan lõi Vàng.
- **Player HP Cap** (07/2026): Máu ban đầu = 5 HP (`Ship.INITIAL_LIFE = 5f`), Cap máu tối đa = 10 HP (`Ship.MAX_LIFE = 10f`). Nhặt Item HP sẽ hồi máu vượt mức ban đầu lên tối đa 10 HP.
- **Audio fix (07/2026)**: Cả 2 file nhạc nền `ut.mp3` và `action_music.mp3` trước đó có `max_volume = 0.0 dBFS` (peak clipping). Đã re-encode bằng ffmpeg với `-filter:a volume=-3dB -q:a 2` → peak hiện ở ~ -3 dBFS, cho headroom tránh clipping khi Android mixer cộng thêm gain. File gốc được backup tại `*.mp3.bak`. Throttle SFX vẫn giữ (giảm tải instance đồng thời).
- **Item design (07/2026)**: `ItemHP` và `ItemWeaponUpgrade` được vẽ lại hoàn toàn bằng `ShapeRenderer` (không dùng Pixmap/Texture nữa). Size nhỏ hơn (18px). `ItemHP`: vòng tròn đỏ-hồng có viền glow + dấu `+` trắng. `ItemWeaponUpgrade`: vòng tròn cyan có viền glow + mũi tên `↑` trắng. Không còn xáy vòng tràn, bắt đầu cố định (orientation=0).
- **Audio throttling**: limit giữa các lần phát cùng `SoundName` — xem `AudioManager.soundMinIntervals`. `Laser` = 350ms; `Explode5` = 150ms; `Explode2/3/4/8` = 180ms.
- **Music Mute** (Part B, 07/2026): `AudioManager` có flag `isMusicMuted` (boolean) lưu vào `Preferences` key `"musicMuted"`. Hoàn toàn **độc lập với volume slider** (key `"volume"`). Toggle button mượt mà in-place trong `MainMenuScreen.showSettingsDialog()`.
- **Central Debug Config** (07/2026): `DebugConfig.java` nằm trong `utils/`. Đặt `ENABLE_DEBUG = true` để tùy chỉnh `DEBUG_START_WAVE`, `DEBUG_START_HP`, `DEBUG_START_WEAPON_LEVEL` cho việc test nhanh.
- Fix đã xác nhận: đồng bộ velocity quái với `deltaTime` (hết bay lệch màn hình khi FPS trồi sụt); clamp `hoverY` hai chiều (BUG #3 fix) để đội hình không tràn mép ngoài (xem `applyHoverYPct` trong WAVE_SYSTEM.md).

## 7. Việc cần làm tiếp
- [ ] Pity system: guaranteed weapon upgrade drop trước wave có BOSS — hiện chưa có.
- [ ] Health bar cho Boss: player không có UI để biết HP boss còn bao nhiêu.
- [ ] LibGDX Sound instance limit: AudioManager chỉ throttle theo thời gian (150ms/âm), không giới hạn số instance chạy đồng thời — có thể gây distort trong wave lớn (L2, để sau).