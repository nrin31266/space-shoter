# Kế Hoạch Tái Cấu Trúc - Space Shooter (ĐÃ HOÀN THÀNH)

> **Trạng thái:** ✅ Phase 1 & Phase 2 hoàn thành — 07/2026
> **Số file:** 35 file `.java`, 7 package, 0 lỗi biên dịch.
> **Phase 2:** Scene2D UI, Loading progress %, MainMenu Stage+Dialog, GamePlay HUD+GameOver popup

---

## 1. Mục Tiêu

Tái cấu trúc dự án Space Shooter (LibGDX) để:
- Phá bỏ God Class `SpaceShooter.java` (vốn đảm nhiệm mọi thứ: render, input, âm thanh, va chạm, quản lý trạng thái).
- Loại bỏ biến `static` của lớp `Ship`.
- Thay thế `EarClippingTriangulator` trong `ItemStar` bằng texture sinh từ `Pixmap`.
- Áp dụng mô hình Screen-based architecture, giữ nguyên core gameplay.

**NGUYÊN TẮC:** Game vẫn là endless shooter, KHÔNG thêm màn chơi, level, quái mới hay cơ chế mới.

---

## 2. Cấu Trúc Package (Thực Tế)

```
com.alexei.spaceshooter/
├── MainGame.java              (extends Game, Router chính, quản lý AssetManager)
├── SpaceShooter.java          (GIỮ LẠI: static bridge methods cho code cũ)
├── Starfield.java             (background parallax)
│
├── screen/
│   ├── LoadingScreen.java     (AssetManager, progress bar)
│   ├── MainMenuScreen.java    (Menu chính: starfield, ship, nút Play)
│   └── GamePlayScreen.java    (Toàn bộ gameplay + Popup Game Over)
│
├── manager/
│   ├── AudioManager.java      (Non-static: load/play/stop sound & music)
│   └── GameState.java         (Non-static: enemies, projectiles, items, score, ship...)
│
├── entity/
│   ├── Visual.java            (+ setVisualEffectsList bridge)
│   ├── Unit.java              (bỏ vòng lặp update weapon)
│   ├── Ship.java              (ĐÃ XÓA static fields: position, size, center, ship)
│   ├── EnemyShipA.java
│   ├── EnemyShipB.java
│   ├── Projectile.java
│   ├── ProjectileRocket.java  (dùng getTarget() thay Ship.center)
│   ├── Item.java
│   └── ItemStar.java          (ĐÃ BỎ EarClippingTriangulator, dùng Pixmap texture)
│
├── weapon/
│   ├── Weapon.java            (ĐÃ BỎ static projectiles, update() nhận list param)
│   ├── WeaponShipLaser.java
│   ├── WeaponShipRocket.java
│   ├── WeaponEnergyBallA.java (dùng setTarget(Visual) thay Ship.center)
│   └── WeaponFactory.java
│
├── effect/
│   ├── Effect.java
│   ├── EffectExplosion.java
│   ├── EffectFlash.java
│   ├── EffectSparks.java
│   ├── EffectSpawrksSpawner.java
│   ├── Particle.java
│   └── ParticleEmitter.java
│
└── utils/
    ├── Utils.java
    ├── Timer.java
    ├── TouchData.java
    ├── ScoreTracker.java
    ├── SoundName.java
    └── SoundType.java
```

**Đã xóa:** `GameModeManager.java` (không còn dùng).

---

## 3. Luồng Chuyển Màn (Screen Flow)

```
MainGame.create()
    │
    ▼
LoadingScreen
    │  AssetManager.load() textures + AudioManager load sounds/music
    │  Hiển thị progress bar (ShapeRenderer)
    │
    ▼
MainMenuScreen
    │  Starfield scrolling, ship góc dưới, nút Play (vòng tròn xanh)
    │  Nhạc nền menu (Ut)
    │  Touch → GamePlayScreen
    │
    ▼
GamePlayScreen
    │  Toàn bộ gameplay: spawn, va chạm, slow-motion, score
    │  Khi ship chết → Popup Game Over (vòng tròn đỏ + điểm)
    │  Touch → quay MainMenuScreen
    │
    └──► MainMenuScreen (loop)
```

---

## 4. Giải Pháp Logic (Đã Triển Khai)

### 4.1. Phá Bỏ God Class `SpaceShooter.java`

- `MainGame` (extends `Game`) là entry point mới, khởi tạo `AssetManager`.
- `SpaceShooter.java` được **giữ lại** như một static bridge class:
  - `acquireTarget()` / `isTargetDead()` → delegate sang `activeEnemiesRef` do `GamePlayScreen` set.
  - `playSound()` / `playMusic()` / ... → delegate sang `staticAudioManager` do `GamePlayScreen` set.
  - `GROUND_SCROLL_SPEED` → hằng số vẫn dùng bởi `Item`, `EnemyShipB`.
  - `items` → delegate sang `activeItemsRef` do `GamePlayScreen` set (cho `Unit.dropStars()`).
- `GamePlayScreen` gọi `SpaceShooter.setActiveEnemiesList()`, `setStaticAudioManager()`, `setActiveItemsList()` trong `show()`.

### 4.2. Loại Bỏ Biến `static` Của Lớp `Ship`

- Đã xóa: `Ship.position`, `Ship.size`, `Ship.center`, `Ship.ship`.
- `Ship` nhận `AudioManager` qua `setAudioManager()` để phát âm thanh alarm.
- `WeaponEnergyBallA` có `setTarget(Visual)` — `GamePlayScreen` gọi mỗi frame trước khi update.
- `ProjectileRocket` dùng `getTarget()` từ `Projectile` thay vì `Ship.center`.
- `Projectile.update()` vẫn dùng `SpaceShooter.acquireTarget()` để tìm mục tiêu mới.

### 4.3. Thay Thế `EarClippingTriangulator`

- `ItemStar` không còn dùng `EarClippingTriangulator`, `ShortArray`, `float[] polys`.
- Texture ngôi sao được sinh một lần qua `Pixmap` (lazy static init), cache vào `TextureRegion`.
- Render bằng `batch.draw()` với rotation và scale, giữ nguyên animation xoay và hiệu ứng pick-up.

### 4.4. Tách `static projectiles` khỏi `Weapon`

- `Weapon.update(float deltaTime, ArrayList<Projectile> projectiles)` nhận list projectiles từ bên ngoài.
- `GamePlayScreen` giữ `state.projectiles` và truyền vào khi update từng weapon.
- `Unit.update()` không còn gọi `w.update()` — việc update weapon do `GamePlayScreen` đảm nhiệm.

### 4.5. Bridge Pattern cho Visual Effects

- `Visual.setVisualEffectsList(list)` cho phép `GamePlayScreen` gán `state.visualEffects` làm list chung.
- `Unit.receiveDamage()` vẫn gọi `Visual.addVisualEffects()` — effect tự động vào đúng list của `GameState`.

---

## 5. Tổng Kết Thay Đổi So Với Code Gốc

| Hạng mục | Trước | Sau |
|----------|-------|-----|
| Entry point | `SpaceShooter` (ApplicationAdapter) | `MainGame` (Game) |
| God Class | 1 file ~900 dòng | Tách thành 3 Screen + 2 Manager |
| Ship static fields | 4 biến static | 0 — dùng instance + setter |
| EarClippingTriangulator | ItemStar tính tam giác mỗi frame | Pixmap texture cache, batch.draw() |
| Weapon.projectiles | static list | Instance list trong GameState |
| GameModeManager | Class riêng quản lý enum | ĐÃ XÓA — Screen-based navigation |
| Âm thanh | Rải rác trong SpaceShooter | Gộp vào AudioManager (non-static) |
| Package | 1 package phẳng | 7 package có tổ chức |
| Số file | 31 | 35 (thêm 6, xóa 2) |

---

## 6. Phase 2: Hoàn Thiện UI (Scene2D) ✅

### 6.1. `LoadingScreen.java`
- Progress bar (`ShapeRenderer`) + text "% loading" (`BitmapFont`)
- Tự động chuyển `MainMenuScreen` khi `AssetManager.update() == true`

### 6.2. `MainMenuScreen.java`
- **Stage + Skin** (programmatic, Pixmap-generated). Button style dùng key `"btn"` riêng để tránh conflict với Label style `"default"`.
- **Table layout:** Title "SPACE SHOOTER" (GOLD, scale 3x), nút PLAY, nút SETTINGS
- **Settings Dialog:** "Toggle Sound" + "Close" button, hiển thị overlay không chuyển màn

### 6.3. `GamePlayScreen.java`
- **Stage overlay** cho HUD: Score (GREEN) + HP (RED) ở góc trên trái. Đã **bỏ** `ScoreTracker.render()` cũ để tránh text chồng chéo.
- **Game Over Window (Scene2D):** khi ship chết → popup giữa màn hình:
  - "GAME OVER" (RED, scale 3x)
  - Score, Killed, Stars
  - Nút "Chơi Lại" → reset GameState + ẩn popup
  - Nút "Về Menu" → chuyển MainMenuScreen
- `isGameOver` flag ngăn update gameplay khi popup đang hiển thị
- Button style dùng key `"btn"` riêng, Label style dùng `"default"` — tránh crash ClassCastException.

### 6.4. Điều chỉnh ItemStar
- Pixmap texture: giảm từ 64x64 → 40x40, `innerR = outerR * 0.4f` (gọn hơn)
- Tốc độ rơi: `GROUND_SCROLL_SPEED * 3` → `GROUND_SCROLL_SPEED * 5` (250 dpi/s)

---

## 7. Phase 3: Pause Menu, UI Refinements & Slow-Mo Removal ✅

### 7.1. Loại bỏ cơ chế Slow-Motion
- Đã xóa `gameSpeed` và `gameSpeedDelta` trong `GamePlayScreen`.
- Game luôn chạy ở tốc độ thực (`deltaTime` không bị nhân hệ số), ngay cả khi không chạm màn hình.

### 7.2. Pause Menu (`GamePlayScreen.java`)
- Thêm nút **Pause** (`||`) ở góc trên bên phải HUD.
- **Pause Dialog:** Hiển thị overlay khi nhấn Pause, dừng toàn bộ logic update game.
- Các nút: **Resume** (tiếp tục chơi) và **Menu** (thoát về màn hình chính).

### 7.3. Cải thiện UI & Kích thước chữ
- Tăng đáng kể `fontScale` cho nhãn (labels) bên trong các `TextButton` ở mọi dialog (GameOver, Settings, Pause).
- Đảm bảo các nút bấm to và chữ bên trong dễ đọc trên thiết bị di động.
- HUD trong game được căn chỉnh lại: Score/HP/Coins bên trái, nút Pause bên phải.

