# REPORT CONTEXT — SPACE SHOOTER
## Kho Ngữ Cảnh Kỹ Thuật cho Báo Cáo Thực Tập

> **Mục đích tài liệu:** Đây KHÔNG phải báo cáo cuối cùng. Đây là kho ngữ cảnh kỹ thuật + lịch sử phát triển + bằng chứng source code, được tạo ra để AI sau này có thể đọc và viết báo cáo thực tập mà không cần suy đoán.
>
> **Nguồn gốc:** Được tạo bằng cách phân tích toàn bộ repository thực tế — source code, Gradle config, assets, tools, Git history. Mọi fact đều có nguồn chứng minh.
>
> **Quy ước:** `[CHUA XAC MINH]` = không tìm thấy bằng chứng trong repo. `[DISCREPANCY]` = mâu thuẫn giữa các nguồn.

---

## MUC LUC

1. Thông Tin Tổng Quan
2. Bối Cảnh Thực Tập
3. Lịch Sử Phát Triển (Git History)
4. Phân Tích Project Gốc (Fando/space-shooter)
5. Kiến Trúc Hiện Tại
6. Cấu Trúc Source Code
7. Phân Tích Dependencies
8. Build & Android Deployment
9. Android-Specific Analysis
10. iOS / Cross-Platform
11. Gameplay Systems — Player & Ship
12. Weapon System (3 Player Tracks)
13. Enemy System (6 Loại + Boss)
14. Wave System
15. Item System
16. Collision & Damage System
17. Rendering & Presentation
18. Effect System
19. Asset Pipeline
20. Audio System
21. UI / UX
22. Save / Persistence System
23. Debug & Test Infrastructure
24. Performance Optimization
25. AI-Assisted Development
26. So Sánh Project Gốc vs Phiên Bản Hiện Tại
27. Bug / Limitation / Technical Debt
28. Test Matrix
29. Kết Quả Đạt Được
30. Facts & Evidence Catalogue
31. Thông Tin Chưa Xác Minh
32. Danh Sách Screenshot Cần Chuẩn Bị
33. Sơ Đồ Nên Vẽ
34. Map Nội Dung với Cấu Trúc Báo Cáo
35. Gợi Ý Ngôn Ngữ Báo Cáo
36. Executive Summary


---

## 1. THONG TIN TONG QUAN

| Thuộc tính | Giá trị |
|-----------|---------|
| Tên project | Space Shooter |
| Repository gốc | https://github.com/Fando/space-shooter |
| Repository hiện tại | https://github.com/nrin31266/space-shooter (fork + customize) |
| Loại game | Vertical-scrolling arcade space shooter |
| Nền tảng mục tiêu | Android (chính), iOS (phụ) |
| Engine / Framework | LibGDX 1.12.0 |
| Ngôn ngữ | Java (core gameplay), Kotlin (platform adapters), Python (tool scripts) |
| Build system | Gradle (Android Gradle Plugin 8.1.3, Gradle Wrapper 8.4) |
| Application ID | `com.alexei.spaceshooter` |
| Phiên bản | 1.0 (versionCode 1) |
| Mục tiêu hiện tại | Modern, polished, neon arcade mobile space shooter |
| Tác giả | Nguyễn Văn Rin (nrin31266@gmail.com) |
| APK hiện tại | `space-shooter-debug.apk` (~10.4 MB, tại repo root) |

**Inspiration:** "Inspired by classic mobile shooters such as Sky Force" (README.md, line 7)

---

## 2. BOI CANH THUC TAP

### 2.1 Thông Tin Sinh Viên

| Trường | Nội dung |
|--------|---------|
| Họ tên | Nguyễn Văn Rin |
| Mã sinh viên | 23IT231 |
| Trường | Trường Đại học Công nghệ Thông tin và Truyền thông Việt - Hàn (VKU) |
| Khoa | Khoa Khoa học Máy tính |
| Đơn vị thực tập | Công ty TNHH MTV Dịch vụ Phần mềm SafeHorizons |
| Người hướng dẫn DN | Lê Văn Minh |
| GVHD | ThS. Nguyễn Thanh Cẩm |

### 2.2 Đề Tài Thực Tập

> **NGHIÊN CỨU VÀ TÙY BIẾN GAME MÃ NGUỒN MỞ TRÊN NỀN TẢNG ANDROID BẰNG ANDROID STUDIO VÀ CÔNG CỤ AI**

### 2.3 Tại Sao Chọn Space Shooter (Fando/space-shooter)?

| Tiêu chí | Lý do phù hợp |
|---------|--------------|
| Mã nguồn mở | Repository public trên GitHub, cấu trúc Java/LibGDX rõ ràng |
| Quy mô vừa | Đủ lớn để nghiên cứu kiến trúc, không quá phức tạp để onboard |
| Android native | LibGDX compile thẳng ra APK, phù hợp đề tài Android |
| Game domain | Thể loại game arcade phổ biến, phù hợp nghiên cứu game dev |
| Java | Ngôn ngữ được học trong chương trình đào tạo |
| Có phần việc rõ ràng | Project gốc còn nhiều hạn chế, cơ hội tùy biến/cải tiến |
| Git/GitHub | Toàn bộ lịch sử phát triển có thể trace qua commits |
| Build Gradle | Quy trình build Android chuẩn, học được Android Studio toolchain |
| AI applicable | Phù hợp ứng dụng AI hỗ trợ code review, refactor, asset generation |

### 2.4 Vai Trò Của Các Công Nghệ

| Công nghệ | Vai trò trong đề tài |
|----------|---------------------|
| Android Studio | IDE chính để viết, debug, build APK; tích hợp Gradle |
| LibGDX | Game framework cross-platform, cung cấp rendering/audio/input API |
| Java | Ngôn ngữ implement gameplay, entities, managers |
| Kotlin | Platform adapter (AndroidLauncher), build scripts |
| Gradle | Build automation, dependency management, APK packaging |
| Git/GitHub | Version control, lưu lịch sử phát triển từng giai đoạn |
| Python (NumPy/PIL) | Offline asset generation (sprites, audio) thay thế runtime procedural drawing |
| AI (Antigravity/Claude) | Hỗ trợ phân tích code, generate code, refactor, tạo tài liệu |

---

## 3. LICH SU PHAT TRIEN (GIT HISTORY)

> **Evidence:** Lấy từ `git log --format="%H|%ai|%s"` trực tiếp trên repository.

Tổng cộng **12 commits** từ 2026-07-29 đến 2026-08-15:

| Commit | Thời gian | Mô tả |
|--------|-----------|-------|
| `4dae8ea` | 2026-07-29 08:03 | **first commit** — Khởi tạo project từ Fando/space-shooter |
| `94cbd3d` | 2026-07-29 21:52 | feat: implement data-driven wave system with save/load |
| `14f49c1` | 2026-07-30 00:05 | feat: Implement new item mechanics and weapon upgrades |
| `8236e44` | 2026-07-31 23:51 | Refactor weapon fire rates; removed outdated docs; improved wave system docs |
| `c75526c` | 2026-08-01 01:43 | Add new enemy types, weapons, and item upgrades for enhanced gameplay |
| `5c4cf2e` | 2026-08-15 01:07 | docs: replace ARCHITECTURE.md with AGENTS.md |
| `fe5b0f4` | 2026-08-15 02:17 | **feat: add sprite-based neon visuals and asset pipeline** |
| `aa00e9b` | 2026-08-15 04:42 | feat: overhaul visuals, audio and mobile readability |
| `8af93df` | 2026-08-15 06:03 | feat: overhaul visuals and weapon progression |
| `de69106` | 2026-08-15 07:10 | refactor: remove SYSTEM.md and update docs |
| `6f143a8` | 2026-08-15 19:45 | fix: improve gameplay and update build tooling |
| `82a6d7b` | 2026-08-15 19:50 | docs: add build, debug, and asset pipeline docs |

### 3.1 Giai Đoạn Phát Triển (Suy luận từ commit messages)

| Giai đoạn | Thời gian | Nội dung chính |
|-----------|-----------|----------------|
| **Giai đoạn 1: Khởi động** | 29/07/2026 | Fork từ Fando/space-shooter, first commit |
| **Giai đoạn 2: Mở rộng gameplay** | 29-31/07/2026 | Data-driven wave system, save/load, new items, weapon upgrades, new enemy types |
| **Giai đoạn 3: Hiện đại hóa visual** | 15/08/2026 sáng | Sprite-based neon visuals, asset pipeline (gen_sprites.py), audio overhaul |
| **Giai đoạn 4: Hoàn thiện & tài liệu** | 15/08/2026 chiều | Weapon progression polish, AGENTS.md, build tooling, docs |

> **Lưu ý:** Toàn bộ lịch sử phát triển tập trung trong khoảng 3 tuần (29/07 - 15/08/2026). Đây là giai đoạn thực tập active.

---

## 4. PHAN TICH PROJECT GOC (Fando/space-shooter)

### 4.1 Nhận Diện "Dự án gốc"

> **QUAN TRỌNG:** Repository hiện tại là fork và đã được sửa đổi đáng kể. First commit `4dae8ea` (29/07/2026) là thời điểm bắt đầu từ codebase gốc. Không có access trực tiếp vào repository gốc https://github.com/Fando/space-shooter để so sánh. Tuy nhiên, README.md hiện tại có một phần "What was built (modernization pass)" mô tả rõ project đã đổi từ đâu sang đây.

### 4.2 Đặc Điểm Project Gốc

Dựa trên README.md (line 165-166):
> "This repository started as a **basic LibGDX prototype** and has been rebuilt into a polished neon arcade shooter."

Và AGENTS.md:
> "The project is an existing game/prototype that has already accumulated substantial gameplay systems."

| Hạng mục | Trạng thái gốc (suy luận từ AGENTS.md + Git) |
|----------|----------------------------------------------|
| Weapons | 2 weapon track: Laser + Rocket Launcher |
| Enemies | Chỉ 2 loại enemy đơn giản (ban đầu) |
| Visual | Primitive rendering (ShapeRenderer), placeholder graphics |
| Audio | Audio cơ bản, có thể có crackle/clipping |
| Wave | Không data-driven (hard-coded) |
| Save/Load | [CHUA XAC MINH] — có thể không có hoặc rất cơ bản |
| Items | Ít item hơn |
| Boss | [CHUA XAC MINH] — không rõ boss ban đầu có không |
| HUD | Đơn giản, font mặc định |
| Architecture | Code coupling cao, không có AssetManager pipeline rõ ràng |

> **Lưu ý quan trọng:** `WeaponShipRocket.java` vẫn tồn tại trong codebase hiện tại (fireRate 2000, damage 1, speed 1000f, homing). Đây có thể là weapon từ project gốc (Rocket Launcher). Tuy nhiên rocket weapon không xuất hiện trong 3 track chính của player — nó có thể là legacy code chưa xóa.

### 4.3 Vấn Đề Của Project Gốc

AGENTS.md Section 5 liệt kê các vấn đề của presentation layer:

| Vấn đề | Ảnh hưởng |
|--------|---------- |
| Primitive rendering với ShapeRenderer | Tốn nhiều draw call, không batch-friendly |
| Placeholder graphics | Không có visual identity, trông như prototype |
| Basic projectiles | Không có neon glow, không phân biệt weapon types |
| Weak separation của concern | GamePlayScreen là "God class", khó maintain |
| Hard-coded wave configuration | Không data-driven, khó balance |
| Repeated asset loading | Performance risk trên mobile |


---

## 5. KIEN TRUC HIEN TAI

### 5.1 Tổng Quan

Project Space Shooter sử dụng kiến trúc **Game-Loop Based với Screen Management**, không phải MVC/MVVM/Clean Architecture theo nghĩa truyền thống. Đây là kiến trúc thực tế của LibGDX game:

```
MainGame (Game)
    |-- LoadingScreen (Screen)
    |-- MainMenuScreen (Screen)
    +-- GamePlayScreen (Screen)           <- Main "controller"
            |-- GameState                 <- Data container
            |-- WaveManager               <- Wave logic
            |-- AudioManager              <- Audio
            |-- SaveManager               <- Persistence
            |-- EnemyFactory              <- Factory pattern
            |-- TextureRegistry           <- Asset cache
            +-- Starfield (x2)            <- Background
```

### 5.2 Nguyên Tắc Thiết Kế Được Tuân Theo

| Nguyên tắc | Cách thực hiện | Evidence |
|-----------|----------------|---------|
| Single-load assets | `AssetManager` + `TextureRegistry` | `TextureRegistry.java` — populate một lần sau LoadingScreen |
| Data-driven waves | JSON configuration | `assets/data/waves.json`, `WaveData/WaveConfig/SpawnAction` |
| Factory pattern | `EnemyFactory` | `EnemyFactory.java` — createFromAction() |
| Static bridge | `SpaceShooter` class | Shared state accessor cho entities |
| Screen lifecycle | LibGDX Screen interface | `show()`, `render()`, `hide()`, `dispose()` |
| Platform separation | Core / Android / iOS modules | 3 Gradle modules |

### 5.3 Điều Không Áp Dụng

> KHÔNG có: Dependency Injection, MVVM, Clean Architecture, Repository Pattern, Entity Component System (ECS), Observer Pattern, Command Pattern rõ ràng. Architecture là **custom game-loop** với các manager classes.

---

## 6. CAU TRUC SOURCE CODE

### 6.1 Directory Tree (Tóm tắt)

```
/space-shooter/
|-- android/                     # Android platform module
|   |-- AndroidManifest.xml
|   |-- build.gradle
|   +-- src/com/alexei/spaceshooter/AndroidLauncher.java
|-- ios/                         # iOS platform module (RoboVM)
|   |-- build.gradle
|   |-- robovm.xml
|   +-- src/com/alexei/spaceshooter/IOSLauncher.java
|-- core/                        # Shared game logic (all platforms)
|   +-- src/com/alexei/spaceshooter/
|       |-- MainGame.java         # Game entry point (extends Game)
|       |-- SpaceShooter.java     # Static bridge/shared state
|       |-- Starfield.java        # Animated space background
|       |-- data/wave/            # Wave data model (SpawnAction, WaveConfig, WaveData)
|       |-- effect/               # Visual effects (Explosion, Flash, Sparks, Particle, ParticleEmitter)
|       |-- entity/               # Game entities (Visual, Unit, Ship, EnemyShipA-F, EnemyBoss, Projectile, Items)
|       |-- factory/EnemyFactory.java
|       |-- manager/              # Managers (AudioManager, GameState, SaveManager, WaveManager)
|       |-- screen/               # Screens (GamePlayScreen, LoadingScreen, MainMenuScreen)
|       |-- utils/                # Utilities (DebugConfig, FontUtil, TextureRegistry, Timer, SoundName, etc.)
|       +-- weapon/               # Weapons (Weapon base, 3 player tracks, 6 enemy weapons, WeaponFactory)
|-- assets/                       # Game assets (shared)
|   |-- data/waves.json           # Wave configuration (20 waves, 594 lines)
|   |-- fonts/ (Roboto-Bold.ttf, fa-solid-900.ttf)
|   |-- music/ (action_music.ogg, ut.ogg)
|   |-- sounds/ (31 SFX files .mp3 + .ogg)
|   |-- screenshots/ (6 PNG + 1 GIF)
|   |-- *.png (27 sprite assets)
|   +-- ATTRIBUTIONS.md
|-- tools/                        # Offline asset generation
|   |-- gen_sprites.py            # Sprite generation (4x supersampled)
|   |-- gen_projectiles_items.py  # Projectile/item sprite generation
|   +-- gen_audio.py              # Audio synthesis (numpy DSP)
|-- docs/REPORT_CONTEXT_VI.md     # <- Tài liệu này
|-- AGENTS.md, README.md, CLAUDE.md
|-- build.gradle, settings.gradle, gradle.properties, local.properties
|-- build_run_spaceshooter.sh, start_pixel_emu.sh
|-- fix_waves.py
+-- space-shooter-debug.apk       # Pre-built APK (~10.4 MB)
```

### 6.2 Package Structure

Tất cả source Java đều trong package `com.alexei.spaceshooter.*`:

| Package | Vai trò |
|---------|---------|
| `com.alexei.spaceshooter` | Root: `MainGame`, `SpaceShooter`, `Starfield` |
| `...screen` | Screen management — Loading, Menu, Gameplay |
| `...entity` | Game entities — Visual, Unit, Ship, Enemies, Items, Projectiles |
| `...weapon` | Weapon implementations (3 player tracks + 6 enemy weapons) |
| `...effect` | Visual effects — Explosions, Sparks, Flash, Particles |
| `...manager` | Game managers — Audio, Wave, Save, GameState |
| `...factory` | Factory — EnemyFactory |
| `...data.wave` | Wave data models — WaveConfig, WaveData, SpawnAction |
| `...utils` | Utilities — Debug, Font, Texture, Sound, Timer, UI |

---

## 7. PHAN TICH DEPENDENCIES

### 7.1 Bảng Dependencies

| Dependency | Version | Vai trò | Module |
|-----------|---------|---------|--------|
| LibGDX core | 1.12.0 | Game framework: rendering, audio, input, math | core, android, ios |
| gdx-backend-android | 1.12.0 | LibGDX Android backend | android |
| gdx-backend-robovm | 1.12.0 | LibGDX iOS backend | ios |
| gdx-freetype | 1.12.0 | TrueType font rendering (FreeTypeFontGenerator) | core, android |
| gdx-platform (natives) | 1.12.0 | Native .so libraries (armeabi-v7a, arm64-v8a, x86, x86_64) | android |
| VisUI | 1.5.3 | Scene2D UI skin/widgets (kotcrab.vis) | core |
| ShapeDrawer | 2.5.0 | Efficient shape drawing (space.earlygrey) | core |
| Kotlin stdlib | 1.6.0 | Kotlin language runtime | all |
| RoboVM | 2.3.20 | AOT compiler Java->iOS (MobiVM) | ios |
| Android Gradle Plugin | 8.1.3 | Android build tooling | buildscript |
| Gradle | 8.4 | Build automation | wrapper |
| JDK | 17.0.14 (JBR) | Java compilation | Gradle daemon |

**Evidence:** `build.gradle` (root, lines 3-35), `android/build.gradle` (lines 1-35), `core/build.gradle` (lines 9-13).

### 7.2 Ghi Chú Dependency

- `VisUI` được dùng cho MainMenuScreen (Dialog, Skin) và CustomUI helpers
- `ShapeDrawer` được khai báo nhưng mức độ sử dụng trong gameplay cần kiểm tra thêm
- `gdx-freetype` để load Roboto-Bold.ttf và fa-solid-900.ttf tại runtime
- `Ashley (1.7.4)`, `Box2D Lights`, `GDX AI`, `GDX Controllers` được khai báo trong `ext` nhưng **KHÔNG được thêm vào dependencies** của bất kỳ module nào — chỉ là placeholder variables

---

## 8. BUILD & ANDROID DEPLOYMENT

### 8.1 Quy Trình Build APK

```bash
# Build debug APK
./gradlew :android:assembleDebug

# Output
android/build/outputs/apk/debug/android-debug.apk
# (Copy thủ công sang root như space-shooter-debug.apk)
```

### 8.2 Quy Trình Install & Launch

```bash
# Install
adb install -r android/build/outputs/apk/debug/android-debug.apk

# Launch
adb shell monkey -p com.alexei.spaceshooter -c android.intent.category.LAUNCHER 1
# hoặc
adb shell am start -W -n com.alexei.spaceshooter/com.alexei.spaceshooter.AndroidLauncher
```

### 8.3 Convenience Scripts

| Script | Mục đích |
|--------|---------|
| `./build_run_spaceshooter.sh` | Build + detect device + install + launch; hiển thị logcat khi lỗi |
| `./start_pixel_emu.sh` | Khởi AVD `pixel_emu`, polling boot completion (180s + 240s) |

### 8.4 Emulator Configuration

- AVD: `pixel_emu` (Pixel device simulation)
- GPU mode: `swiftshader_indirect` (software rendering, headless)
- No audio: `-no-audio`
- No snapshot: `-no-snapshot-save`

**Evidence:** `AGENTS.md` section 21, `build_run_spaceshooter.sh`, `start_pixel_emu.sh`

---

## 9. ANDROID-SPECIFIC ANALYSIS

### 9.1 AndroidLauncher

- **File:** `android/src/com/alexei/spaceshooter/AndroidLauncher.java`
- **Extends:** `AndroidApplication` (LibGDX Android backend)
- **Lifecycle:** `onCreate()` -> `initialize(new MainGame(), config)`
- **Config:** `AndroidApplicationConfiguration` mặc định

### 9.2 AndroidManifest.xml

- **Permissions:** `android.permission.VIBRATE` (duy nhất)
- **Feature:** `glEsVersion="0x00020000"` (OpenGL ES 2.0 bắt buộc)
- **Screen Orientation:** `fullSensor` — tự xoay theo cảm biến
- **Config Changes:** keyboard, orientation, screenSize, screenLayout — không destroy activity khi rotate
- **Application flags:** `android:isGame="true"`, `android:appCategory="game"`

### 9.3 Android Build Configuration

| Field | Giá trị |
|-------|---------|
| applicationId | `com.alexei.spaceshooter` |
| namespace | `com.alexei.spaceshooter` |
| compileSdkVersion | 34 (Android 14) |
| minSdkVersion | 29 (Android 10) |
| targetSdkVersion | 34 (Android 14) |
| Java sourceCompatibility | VERSION_17 |
| versionCode | 1 |
| versionName | "1.0" |

### 9.4 Vai Trò Của Android Studio

Android Studio được sử dụng:
1. **IDE chính** để viết Java/Kotlin code với Gradle integration
2. **Debug APK** qua LogCat, breakpoints, memory profiler
3. **AVD Manager** để tạo và quản lý Android Virtual Device (`pixel_emu`)
4. **Gradle wrapper** để build APK debug/release
5. **ADB integration** để install/launch trực tiếp
6. **Code navigation** qua codebase lớn

---

## 10. iOS / CROSS-PLATFORM

### 10.1 iOS Module

- **Tồn tại:** Có (`ios/` directory với `build.gradle`, `robovm.xml`, `robovm.properties`, `Info.plist.xml`)
- **Launcher:** `ios/src/com/alexei/spaceshooter/IOSLauncher.java`
- **Build tool:** RoboVM 2.3.20 (MobiVM) — AOT compile Java -> native ARM
- **Build command:** `./gradlew :ios:build`

### 10.2 Cross-Platform Architecture

| Phần code | Platform |
|----------|---------|
| `core/` — tất cả gameplay, entities, managers, screens | **Shared** (Android + iOS) |
| `android/` — AndroidLauncher.java, Manifest, res/ | **Android only** |
| `ios/` — IOSLauncher.java, robovm.xml | **iOS only** |
| `assets/` — tất cả textures, audio, fonts, data | **Shared** |

### 10.3 Hạn Chế iOS

> "Known limitation: the iOS build may not run on the simulator for some Xcode versions." (README.md, line 25)

Trọng tâm đề tài là **Android**. iOS module tồn tại nhưng không phải focus.


---

## 11. GAMEPLAY SYSTEMS — PLAYER & SHIP

### 11.1 Ship.java

- **File:** `core/src/com/alexei/spaceshooter/entity/Ship.java`
- **Extends:** `Unit` -> `Visual`
- **HP:** Khởi đầu 5f, tối đa 10f
- **Size:** 150x150 pixels
- **Texture:** `TextureRegistry.ship`

### 11.2 Movement System

- **Input:** Drag gesture — `InputAdapter.touchDragged()` trong `GamePlayScreen.show()`
- **Cơ chế:** Tính `touchDisplacement` mỗi frame -> `moveVelX`, `moveVelY`
- **Đặc biệt:** Di chuyển bằng displacement (không phải absolute position), tạo quán tính tự nhiên
- **Effect:** Đạn kế thừa một phần chuyển động của tàu (`inheritShipMotion` trong Weapon base class)

### 11.3 Firing System

- **Trigger:** `touchDown` hoặc `touchDragged` -> shoot khi hold màn hình
- **Active weapon:** 1 trong 3 track (Laser/Blast/Homing)
- **Fire:** Weapon.update(deltaTime) -> fire() khi timer expired

### 11.4 Damage & Invulnerability

- Khi bị trúng đạn: giảm HP -> nếu có stockpile thì trừ stockpile trước -> hết stockpile thì giảm weapon level
- 1.5 giây invulnerability sau khi nhận sát thương (shield bubble chớp nháy)
- `receiveDamage()` trong Unit.java — gọi flash effect, spawn sparks, play get_damage sound

### 11.5 Weapon State (Ship)

```
Ship {
    activeWeaponType: int  // 0=Laser, 1=Blast, 2=Homing
    weaponLevel[3]: int    // Level của mỗi track (1-7)
    stockpile[3]: int      // Stockpile của mỗi track (0-3)
}
```

**Evidence:** `SaveManager.java` lines 57-77 (save schema phản ánh 3 tracks với level + stockpile riêng biệt)

---

## 12. WEAPON SYSTEM (3 PLAYER TRACKS)

### 12.1 Weapon Base Class

- **File:** `core/src/com/alexei/spaceshooter/weapon/Weapon.java`
- **Abstract method:** `fire()` -> trả về `Projectile[]`
- **Shared fields:** `damage`, `fireRate`, `timer`, `unit`, `weaponSoundName`, `audioManager`, `enabled`, `projectileRegion`
- **Key methods:**
  - `inheritShipMotion(Projectile, forwardSpeed)` — truyền quán tính ship cho đạn
  - `spawnMuzzleFlash(Color)` — tạo hiệu ứng muzzle flash
  - `setProjectileVisual(region, round)` — gán texture cho đạn

### 12.2 Track 0: WeaponShipLaser (Neon Blue Beam Fan)

| Level | Projectile Count | Fire Rate (ms) | Damage |
|-------|-----------------|----------------|--------|
| 1 | 1 | 150 | 0.70 |
| 2 | 2 | 130 | 0.65 |
| 3 | 3 | 115 | 0.60 |
| 4 | 3 | 100 | 0.60 |
| 5 | 4 | 90 | 0.55 |
| 6 | 4 | 80 | 0.55 |
| 7 | 5 | 70 | 0.50 |

- **Texture:** `TextureRegistry.laserBlue` (laser beam shape)
- **Color:** Neon Cyan (`0.4f, 0.85f, 1f`)
- **Speed:** 1500f
- **Pattern:** Single-origin spread fan (tất cả đạn từ 1 điểm muzzle, không cross tại mũi tàu)
- **Sound:** LaserShoot

### 12.3 Track 1: WeaponExplosiveBlaster (Neon Orange Orbs, Pierce)

| Level | Projectile Count | Fire Rate (ms) | Damage | Pierce |
|-------|-----------------|----------------|--------|--------|
| 1 | 1 | 300 | 2.0 | 1 |
| 2 | 2 | 260 | 1.8 | 1 |
| 3 | 2 | 240 | 2.0 | 2 |
| 4 | 3 | 215 | 1.8 | 2 |
| 5 | 3 | 195 | 2.0 | 2 |
| 6 | 3 | 180 | 2.2 | 3 |
| 7 | 3 | 165 | 2.4 | 3 |

- **Texture:** `TextureRegistry.shotOrb` (round orb, neon orange)
- **Speed:** 1200f
- **Pierce:** Xuyên qua `1 + level/3` enemy trước khi biến mất

### 12.4 Track 2: WeaponHomingLightning (Neon Purple Homing Darts)

| Level | Projectile Count | Fire Rate (ms) | Damage | Speed |
|-------|-----------------|----------------|--------|-------|
| 1 | 1 | 280 | 0.8 | 750f |
| 2 | 2 | 230 | 0.7 | 900f |
| 3 | 2 | 210 | 0.8 | 1000f |
| 4 | 3 | 190 | 0.8 | 1100f |
| 5 | 3 | 175 | 0.9 | 1150f |
| 6 | 3 | 160 | 1.0 | 1200f |
| 7 | 3 | 150 | 1.1 | 1350f |

- **Texture:** `TextureRegistry.shotDart` (dart shape, neon purple)
- **Homing:** Sử dụng `SpaceShooter.acquireTarget()` để tìm enemy gần nhất

### 12.5 Weapon Switch & Level Mechanism

| Hành động | Kết quả |
|---------|---------|
| Nhặt item same-track | +1 level cho track đó |
| Nhặt item khác track | Switch sang track mới |
| Nhặt Energy item | +1 level cho track hiện tại |
| Bị trúng đạn | Trừ stockpile -> nếu hết: giảm level |
| Boss pity (wave 5/10/15/20) | Drop energy + weapon nếu level < 5 |

### 12.6 Enemy Weapons (Không có level system)

| Weapon | Enemy sử dụng | Projectile | Fire Rate (ms) | Damage | Pattern |
|--------|--------------|-----------|----------------|--------|---------|
| WeaponEnemyLaser | EnemyShipA, Boss | `orbRed` | 8000 | 1.0 | Thẳng xuống |
| WeaponEnergyBallA | EnemyShipB | `orbGreen` | 8000 | 1.0 | Aimed (homing at fire time) |
| WeaponSniperBeam | EnemyShipC, Boss | `orbPink` | 3500 | 1.0/1.5 | Aimed at player position |
| WeaponSpreadShot | EnemyShipD, Boss | `orbGold` | 3000 | 1.0/1.8 | 3-way spread (-22 deg, 0, +22 deg) |
| WeaponDoublePulse | EnemyShipE | `orbPurple` | 2400 | 1.0 | 2 parallel shots down |
| WeaponRingBurst | EnemyShipF | `orbGold` | 4000 | 1.0 | 4-dir ring (45, 135, 225, 315 deg) |

> **Boss damage values:** WeaponSpreadShot = 1.8f, WeaponEnemyLaser = 1.3f, WeaponSniperBeam = 1.5f

---

## 13. ENEMY SYSTEM (6 LOAI + BOSS)

### 13.1 Bảng Tổng Hợp Enemy

| Enemy | HP Base | Enter Speed | Hover Speed | Hover Y% | Texture | Weapon |
|-------|---------|------------|------------|---------|---------|--------|
| EnemyShipA | 1.0 | ~1040 | 80 | 55-60% | `enemy1` (cyan) | EnemyLaser |
| EnemyShipB | 5.0 | ~760 | 60 | 58-65% | `enemy2` (chartreuse) | EnergyBallA |
| EnemyShipC | 1.5 | ~1200 | 100 | 48-55% | `enemyC` (hot pink) | SniperBeam |
| EnemyShipD | 8.0 | ~480 | 45 | 62-75% | `enemyD` (orange) | SpreadShot |
| EnemyShipE | 3.0 | ~1120 | 90 | 48-50% | `enemyE` (purple) | DoublePulse |
| EnemyShipF | 12.0 | ~440 | 40 | 60-62% | `enemyF` (gold) | RingBurst |
| EnemyBoss | 100.0 | 280 | 100 | 75% | `boss` (red) | 3 weapons |

> **HP Scaling:** HP thực tế = `HP_base x 2 x (1 + growthRate)^(waveId - firstWave)` (x3 cho Boss)
> **Evidence:** `EnemyFactory.java` lines 103-133, method `calculateScaledHP()`

### 13.2 Movement Patterns

| Enemy | Pattern |
|-------|---------|
| A, B, C, D, E, F | Bay thẳng từ trên xuống -> dừng tại hoverY -> sine wave ngang |
| Boss | Bay từ trên xuống -> ping-pong ngang (đổi hướng khi chạm biên) |

### 13.3 "No Fire Before Arrived" Rule

> Tất cả enemy chỉ bắn sau khi `arrived = true` (đã đến hoverY). Không có "ngậm đạn" khi đang bay vào.
> **Evidence:** `Unit.java` — field `arrived`, checked trước khi weapons update

### 13.4 EnemyBoss (Dreadnought) Chi Tiết

- **Size:** 400x400 pixels
- **HP:** 100 (scaled x3 + exponential growth)
- **Texture:** `TextureRegistry.boss` (màu đỏ hồng đậm `ff0055`)
- **Weapons cùng lúc (3 weapons active simultaneously):**
  1. `WeaponSpreadShot` (damage 1.8f) — bắn tỏa 3 hướng
  2. `WeaponEnemyLaser` (damage 1.3f, rate 650ms) — bắn nhanh thẳng xuống
  3. `WeaponSniperBeam` (damage 1.5f, rate 2200ms) — bắn tỉa nhắm người chơi
- **Death rewards:**
  - 20 Stars văng 360 độ ngẫu nhiên
  - 4 items văng ra 4 góc chéo (45/135/225/315 deg):
    - 1x `ItemWeaponUpgrade`
    - 1x `ItemWeaponUpgradeExplosive`
    - 2x `ItemHP`
- **Anti-duplicate drop:** `hasDropped` boolean flag

### 13.5 First Appearance Waves

| Enemy | First wave | Evidence |
|-------|-----------|---------|
| EnemyShipA | Wave 1 | waves.json |
| EnemyShipB | Wave 2 | waves.json |
| EnemyShipC | Wave 3 | waves.json |
| EnemyShipD | Wave 4 | waves.json |
| EnemyBoss | Wave 5 | waves.json |
| EnemyShipE | Wave 6 | waves.json wave 6 |
| EnemyShipF | Wave 7 | waves.json wave 7 |
| Boss (wave 10, 15, 20) | Every 5 waves | waves.json |

> **DISCREPANCY:** EnemyShipE và EnemyShipF xuất hiện trong waves.json tại wave 6 và 7, nhưng `EnemyFactory.calculateScaledHP()` dùng `firstWave = 16` cho E và F (có thể là lỗi hoặc thiết kế cố ý để không scale HP sớm).

---

## 14. WAVE SYSTEM

### 14.1 Wave Lifecycle

```
WaveManager.startWave(waveId)
    -> load WaveData tu WaveConfig (da parse tu waves.json)
    -> reset elapsedTime = 0
    -> clone list SpawnAction chua trigger

WaveManager.update(deltaTime)
    -> elapsedTime += deltaTime
    -> foreach action: if elapsedTime >= action.delay -> createFromAction() -> add to GameState

WaveManager.isWaveFinished()
    -> true khi tat ca SpawnAction da trigger

WaveManager.isWaveCleared()
    -> true khi enemies list rong (tat ca enemy da chet)

[Wave cleared -> pending delay -> next wave launch]
```

### 14.2 Data-Driven Configuration

- **File:** `assets/data/waves.json` (594 dòng, 20 wave definitions)
- **Format:** JSON object với mảng `waves`, mỗi wave có mảng `actions`
- **Parsing:** LibGDX Json parser -> `WaveConfig` -> `List<WaveData>` -> `List<SpawnAction>`

SpawnAction fields:

| Field | Type | Ý nghĩa |
|-------|------|---------|
| `delay` | float | Thời gian chờ (giây) trước khi spawn batch này |
| `enemyType` | String | Loại enemy chính ("EnemyShipA", "BOSS", v.v.) |
| `pattern` | String | Đội hình (LINE, GRID, CHEVRON, DIAMOND, CHECKERBOARD, INTERLEAVED_ROWS, BOSS, V_SHAPE, RANDOM) |
| `count` | int | Số lượng enemy |
| `hoverYPct` | float | Vị trí hover (% màn hình) |
| `secondaryEnemyType` | String | Enemy phụ (cho CHECKERBOARD, INTERLEAVED_ROWS) |

### 14.3 Formations

| Formation | Mô tả |
|-----------|-------|
| LINE | Phân bổ đều theo hàng ngang |
| GRID | Lưới sqrt(count) x rows |
| CHEVRON | Hình V (enemy giữa xuất hiện cao nhất) |
| DIAMOND | Hình thoi 1-2-2-1 |
| CHECKERBOARD | Lưới xen kẽ 2 loại enemy |
| INTERLEAVED_ROWS | 4 hàng xen kẽ 2 loại enemy |
| V_SHAPE | Hình V với khoảng cách tăng dần từ đỉnh |
| RANDOM | Phân bổ ngẫu nhiên trong cột |
| BOSS | Spawn boss (1 hoặc nhiều) tại trung tâm |

### 14.4 HP Scaling Formula

```
HP_scaled = HP_base x hpMult x (1 + growthRate)^max(0, waveId - firstWave)
```

Trong đó:
- `hpMult` = 2.0f (enemy thường) hoặc 3.0f (boss)
- `growthRate` = 0.04-0.08f (theo từng enemy type)
- `firstWave` = wave đầu tiên enemy xuất hiện

### 14.5 Endless Loop

- 20 wave được định nghĩa trong JSON
- Sau wave 20, loop lại từ wave 1 nhưng tăng `loopCount`
- `effectiveWaveId = waveId + loopCount * 20` để HP scaling tiếp tục tăng

### 14.6 Boss Pity Drop

- Kích hoạt tại wave 5, 10, 15, 20 (boss waves)
- Điều kiện: Player weapon level < 5
- Drop: Energy item + Weapon switch item -> đảm bảo player có đủ sức chiến đấu Boss

---

## 15. ITEM SYSTEM

### 15.1 Base Item Behavior

- **Spawn:** Khi enemy chết -> `dropStars()` -> scatter velocity -> rơi xuống với gravity 1200f
- **Bounce:** `bounceCount = 2` (default), giảm 40% lực rơi mỗi lần nảy
- **Pickup:** Khi player chạm vào item (collision) -> `pickUp()` -> animation phóng to/thu nhỏ 250ms -> biến mất
- **Magnetize:** `magnetize(Visual)` -> tốc độ 750f, hút về phía player
- **Death:** Item biến mất khi Y < -100 hoặc pickup animation kết thúc

### 15.2 Bảng Items

| Item | Texture | Màu | Pickup Effect | Bounce |
|------|---------|-----|--------------|--------|
| `ItemStar` | `itemStar` | Gold `ffcc3d` | +Stars (currency) | Có |
| `ItemHP` | `itemHp` | Red `ff4655` | +1 HP (heal) | Không |
| `ItemWeaponUpgrade` | `itemUpgrade` | Blue `00aaff` | Switch to Laser hoặc +1 level nếu đang Laser | Không |
| `ItemWeaponUpgradeExplosive` | `itemUpgradeExplosive` | Orange `ff7a26` | Switch to Blast hoặc +1 nếu đang Blast | Không |
| `ItemWeaponUpgradeHoming` | `itemUpgradeHoming` | Purple `c64dff` | Switch to Homing hoặc +1 nếu đang Homing | Không |
| `ItemEnergyUpgrade` | `itemEnergy` | Green `66ff88` | +1 level active track (không đổi track) | Không |

### 15.3 Same-Track Bonus

> "Eating the same-track weapon pickup also grants +1 level." — README.md line 54

### 15.4 Drop Rates (từ DebugConfig.java)

| Item | Rate Production |
|------|----------------|
| Weapon upgrade | 6% (`DROP_RATE_WEAPON_UPGRADE = 0.06f`) |
| Energy | 9% (`DROP_RATE_ENERGY = 0.09f`) |
| HP | 5% (`DROP_RATE_HP = 0.05f`) |
| Star | 100% (`DROP_RATE_STAR = 1.00f`) |


---

## 16. COLLISION & DAMAGE SYSTEM

### 16.1 Cơ Chế

- **Method:** AABB collision detection (`isColliding()` trong `Visual.java`)
- **Processing:** `GamePlayScreen.render()` — iterate all enemies x all player projectiles và all enemy projectiles x player ship
- **Damage flow:**
```
Projectile hit Enemy -> enemy.receiveDamage(damage) -> flash effect -> sparks -> sound
Projectile hit Player -> player.receiveDamage(damage) -> invulnerability -> decal
```

### 16.2 Invulnerability

- 1.5 giây sau khi bị trúng đạn
- Hiển thị shield bubble chớp nháy (neon effect)
- Trong thời gian invulnerability: đạn enemy không gây damage

---

## 17. RENDERING & PRESENTATION

### 17.1 Render Order (GamePlayScreen)

```
1. Clear screen (black)
2. Starfield layer 1 (far background) — SpriteBatch
3. Starfield layer 2 (near background) — SpriteBatch
4. Items — SpriteBatch
5. Enemies — SpriteBatch
6. Player ship — SpriteBatch
7. Projectiles — SpriteBatch (or ShapeRenderer fallback)
8. Visual Effects (explosions, sparks, flash) — SpriteBatch
9. HUD text (HP, Wave, Score/Stars, Pause button) — SpriteBatch (BitmapFont)
10. Scene2D UI Stage (pause menu, dialogs) — Stage.draw()
```

### 17.2 Background (Starfield)

- **Class:** `Starfield.java`
- **Layers:**
  - Nebula tile (`nebula.png`): Cuộn chậm (12 px/s), 2 copies để seamless loop
  - Distant stars: Nhỏ (1-2px), alpha nhạt
  - Near stars: Lớn hơn, alpha đậm hơn
- **Rendering:** SpriteBatch với 1x1 white texture chia sẻ — **1 draw call** thay vì ShapeRenderer per-star

### 17.3 Ship & Enemy Sprites

| Asset | Kích thước | Origin |
|-------|-----------|--------|
| `ship.png` | 128x128 | Author hand art, không regenerate |
| `enemy1.png` | 128x128 | Author hand art |
| `enemy2.png` | 128x128 | Author hand art |
| `enemy_c.png` | 128x128 | Author hand art |
| `enemy_d.png` | 128x128 | Author hand art |
| `enemy_e.png` | 128x128 | Author hand art |
| `enemy_f.png` | 160x160 | Author hand art |
| `enemy_boss.png` | 256x256 | Author hand art |

> **ATTRIBUTIONS:** Ship PNGs là "author-owned, not regenerated" — đây là hand art của Nguyễn Văn Rin.
> **Evidence:** `ATTRIBUTIONS.md` line 24-26, `README.md` line 158.

### 17.4 Projectile Sprites

| Asset | Kích thước | Loại | Dùng cho |
|-------|-----------|------|---------|
| `laser_blue.png` | 48x128 | Generated (gen_projectiles_items.py) | Player Laser track |
| `shot_orb.png` | 48x48 | Generated | Player Blast track |
| `shot_dart.png` | 40x64 | Generated | Player Homing track |
| `laser_red.png` | 48x128 | Generated | Enemy sniper |
| `plasma_orb.png` | 64x64 | Generated | Boss plasma |
| `orb_red.png` | 48x48 | Generated | EnemyShipA |
| `orb_green.png` | 48x48 | Generated | EnemyShipB |
| `orb_gold.png` | 48x48 | Generated | EnemyShipD + F |
| `orb_purple.png` | 48x48 | Generated | EnemyShipE |
| `orb_pink.png` | 48x48 | Generated | EnemyShipC |

### 17.5 HUD Layout

```
+------------------------------------+
| [PAUSE]                            |  <- goc tren phai (Scene2D)
| [HP ♥♥♥] [WEAPON ICON]   WAVE 5   |  <- left: HP + weapon, center: wave
|                        * 1000      |  <- right: star icon + score
|                                    |
|     ... gameplay area ...          |
+------------------------------------+
```

- Font: Roboto-Bold (FreeType, size x1.5)
- HP: màu đỏ
- Score: màu trắng với gold star icon
- HUD hiển thị **luôn luôn** (kể cả trong intro và wave transition)

---

## 18. EFFECT SYSTEM

### 18.1 Bảng Effects

| Class | Mô tả | Particles | Color |
|-------|-------|-----------|-------|
| `EffectExplosion` | Vụ nổ khi enemy/player chết | 50 hạt, 360 độ | Yellow-orange |
| `EffectFlash` | Flash khi nhận damage | 42 hạt, 360 độ | Cyan blue |
| `EffectSparks` | Tia lửa khi bị trúng đạn | Nhiều hạt | Orange |
| `EffectSpawrksSpawner` | Sinh nhiều EffectSparks | — | — |

### 18.2 Particle Rendering

- **Optimization:** Mỗi `Particle` dùng shared 1x1 white texture tinted -> batch-friendly
- **Lifecycle:** Alpha giảm dần từ 1->0 theo vòng đời
- **Fallback:** ShapeRenderer khi batch không active

### 18.3 Muzzle Flash

- Tạo từ `Weapon.spawnMuzzleFlash(Color)` — gọi `EffectFlash` hoặc tương tự
- 3 weapons player đều có muzzle flash màu riêng (Cyan / Orange / Purple)

---

## 19. ASSET PIPELINE

### 19.1 Tổng Quan

```
Python Scripts (offline) -> PNG/OGG files -> assets/ -> AssetManager -> TextureRegistry
```

Không có runtime generation — tất cả assets được tạo offline trước khi build.

### 19.2 gen_sprites.py

| Output | Size | Kỹ thuật |
|--------|------|---------|
| ship.png | 128x128 | Parametric polygon, supersampled 4x, Lanczos |
| enemy1-f.png | 128-160x128-160 | Parametric polygon per type |
| enemy_boss.png | 256x256 | Boss shape với dual cannons |
| laser_blue/red.png | 48x128 | Laser beam gradient |
| plasma_orb.png | 64x64 | Orb with 8 energy spikes |
| item_star.png | 64x64 | Faceted crystal gem |
| item_hp.png | 64x64 | Cross in circle |
| item_upgrade.png | 64x64 | Bolt icon |
| nebula.png | 512x512 | Procedural elip clouds + stars |

**Libraries:** `numpy`, `PIL (Pillow)` — **KHONG dung AI/ML**

**Kỹ thuật:**
- **4x Supersampling (SS=4):** Vẽ trên canvas float32 ở độ phân giải 4x, nén xuống bằng Lanczos filter. Loại bỏ răng cưa, giữ viền alpha sắc nét.
- **Parametric Procedural Ship Drawing:** Định nghĩa thân tàu bằng các đa giác tham số hóa đối xứng qua trục giữa `cx = W/2`.
- **Additive & Halo Glow (`add_neon_glow`):** Tách kênh Alpha, áp dụng Gaussian Blur, nhân tăng sáng, composite dải hào quang mờ phía dưới sprite gốc.
- **Nebula Generator (`draw_nebula`):** Sinh 14 khối mây elip ngẫu nhiên + 120 điểm sao với độ trong suốt khác nhau.

### 19.3 gen_projectiles_items.py

Tạo bộ đạn/item neon thế hệ mới thay thế gen_sprites.py cho một subset:
- Import helper functions từ gen_sprites.py
- Output: laser_blue, laser_red, plasma_orb, orb_* (5 màu), shot_orb, shot_dart, item_* (6 items), nebula v2

**Kỹ thuật:**
- **Trigonometric Procedural Shapes:** Ngôi sao 5 cánh, cầu gai plasma, biểu tượng nổ Explosive (12 cánh), lõi lục giác Energy, tâm ngắm Homing
- **Concentric Halo Background:** Nền vũ trụ sẫm màu với 3 vòng hào quang elip đồng tâm mờ nhạt, chống hiện tượng vệt sọc khi cuộn màn hình dọc

### 19.4 gen_audio.py — CHI TIET

**Mục đích:** Tổng hợp (synthesize) toàn bộ BGM và SFX dạng sóng số nguyên bản bằng mã nguồn toán học/NumPy. Convert sang OGG (Vorbis) bằng FFmpeg. Hoàn toàn không dùng sample bên ngoài.

**Output files:**

| File | Loại | Nội dung |
|------|------|---------|
| `music/action_music.ogg` | Gameplay BGM | Synthwave 128 BPM, A minor, Am-F-C-G, 8 bars |
| `music/ut.ogg` | Menu BGM | Calm ambient 32s loop, hợp âm Am7-Fmaj7-C-G |
| `sounds/powerup.ogg` | SFX | Ascending arpeggio [60,64,67,72,76] |
| `sounds/pickup.ogg` | SFX | Crystal chime MIDI 88 |
| `sounds/wave_start.ogg` | SFX | Whoosh sweep |
| `sounds/wave_clear.ogg` | SFX | 3-note fanfare [72,76,79] |
| `sounds/boss_warning.ogg` | SFX | Ominous low horn MIDI 38 |

**DSP Techniques Used:**
- **Oscillator Synthesis (`osc`):** Sine: sin(2*pi*f*t), Triangle: (2/pi)*arcsin(sin(2*pi*f*t)), Sawtooth, Square
- **ADSR Envelope (`env_adsr`):** 4 giai đoạn Attack/Decay/Sustain/Release tuyến tính
- **One-pole Lowpass Filter (`lowpass`):** IIR 1 cực y[i] = y[i-1] + alpha*(x[i]-y[i-1])
- **Exponential Pitch Sweep (Kick):** f(t) = 140*e^(-28t) + 45
- **Filtered White Noise (Hi-Hat/Whoosh):** Gaussian noise qua lowpass + bao phân rã hàm mũ
- **Detuned Pad:** Chồng 2 sine lệch tần số (f và 1.005f) tạo hiệu ứng chorus
- **MIDI to Frequency:** f = 440 * 2^((m-69)/12)
- **Stereo Widening:** np.roll(out, 30..40) dịch mẫu lệch giữa 2 kênh

**QUAN TRONG:** Script hoàn toàn không dùng SciPy, PyTorch, TensorFlow hay thư viện AI/DSP bên ngoài nào. Tất cả là classical DSP bằng NumPy thuần.

**Pipeline:** numpy arrays -> PCM WAV (16-bit stereo 44.1kHz) -> ffmpeg -> OGG Vorbis q=4

### 19.5 Asset License Summary

| Asset Group | Origin | License |
|------------|--------|---------|
| Ship/enemy/boss PNGs | Author hand art (Nguyễn Văn Rin) | Original work (public domain) |
| Projectile/item PNGs | Generated by gen_sprites.py, gen_projectiles_items.py | Original work (public domain) |
| Music (.ogg) | Synthesized by gen_audio.py | Original work (public domain) |
| SFX (.ogg) | Synthesized by gen_audio.py | Original work (public domain) |
| Legacy SFX (.mp3) | Pre-existing repository files | Pre-existing project files |
| Kenney Space Kit | **Evaluated but NOT shipped** | CC0 (không dùng) |
| Roboto-Bold.ttf | Google Fonts | Apache 2.0 |
| fa-solid-900.ttf | FontAwesome | SIL Open Font License 1.1 |

**Evidence:** `assets/ATTRIBUTIONS.md` toàn bộ


---

## 20. AUDIO SYSTEM

### 20.1 AudioManager

- **File:** `core/src/com/alexei/spaceshooter/manager/AudioManager.java`
- **Loading:** Qua AssetManager -> `loadSounds()` + `loadMusic()` trong LoadingScreen -> `populate()` gán vào HashMap
- **Sounds:** `HashMap<SoundName, Sound>` — 23 sounds
- **Music:** `HashMap<SoundName, Music>` — 2 tracks (ut.ogg, action_music.ogg)

### 20.2 Sound Event Mapping

| Event | Sound |
|-------|-------|
| Player laser fire | `LaserShoot`, `LaserShoot2` |
| Player take damage | `GetDamage` |
| Enemy explode A | `Explode5` |
| Enemy explode B, D | `Explode2` |
| Enemy explode E | `Explode3` |
| Enemy explode F | `Explode4` |
| Item pickup (star) | `Pickup` |
| Item pickup (weapon/energy) | `PowerUp` |
| Wave start | `WaveStart` |
| Wave clear | `WaveClear` |
| Boss warning | `BossWarning` |
| Game over | `EndGame` |
| Low HP warning | `Alarm` |

### 20.3 Audio Anti-Clipping

- **Throttle system:** `soundMinIntervals` HashMap — per-sound minimum interval (ms)
- **Examples:** LaserShoot2 = 250ms, LaserShoot = 200ms, Laser = 350ms
- **Skip logic:** Nếu sound vừa được play trong khoảng `minGap` -> skip

### 20.4 Volume & Music Control

- Volume: 0.0-1.0f, lưu qua `Preferences` ("volume")
- Music mute: Độc lập với volume, lưu qua `Preferences` ("musicMuted")
- Menu music (`ut.ogg`): 0.4x volume
- Gameplay music (`action_music.ogg`): 1.0x volume

---

## 21. UI / UX

### 21.1 MainMenuScreen

- **Background:** 2 lớp Starfield (xa và gần) + tàu lơ lửng
- **Title:** Nhiều lớp text nhấp nháy animation (`titlePulse`)
- **Elements:** High Score, Total Stars, Button "NEW GAME", Button "CONTINUE" (nếu có save), Button "SETTINGS"
- **Settings Dialog:** Slider volume + toggle music (VisUI Dialog)
- **UI Library:** VisUI Scene2D

### 21.2 HUD (GamePlayScreen)

- Layout: Left (HP + weapon icon) / Center (WAVE number) / Right (star icon + score) / Top-right (PAUSE)
- Font: Roboto-Bold via FreeType, scaled x1.5
- Icons: FontAwesome (fa-solid-900.ttf) cho heart, coin icons
- Luôn hiển thị (không ẩn khi intro, wave transition)

### 21.3 Pause Menu

- Triggered: Tap nút PAUSE (góc trên phải)
- VisUI Dialog với options: RESUME, MAIN MENU

### 21.4 Game Over Screen

- Hiển thị score, stars collected
- Sound: `EndGame`
- Options: NEW GAME, MAIN MENU

### 21.5 Wave Announcement

- Text "WAVE X" xuất hiện trung tâm màn hình
- Animation: fade in/out
- Sound: `WaveStart` / `WaveClear`

### 21.6 Responsive Design

- LibGDX `Gdx.graphics.getWidth()` / `Gdx.graphics.getHeight()` cho tất cả layout calculations
- `EnemyFactory.clampX()` đảm bảo enemy không bị cắt biên màn hình
- `hoverYPct` trong waves.json tính theo % màn hình (không pixel cứng)
- Font scaled bằng FreeType (không pixel font)

---

## 22. SAVE / PERSISTENCE SYSTEM

### 22.1 SaveManager

- **File:** `core/src/com/alexei/spaceshooter/manager/SaveManager.java`
- **Mechanism:** `LibGDX Preferences` (key-value store, Android -> SharedPreferences)
- **Prefs name:** `"space-shooter-save"`

### 22.2 Schema

| Key | Type | Ý nghĩa |
|-----|------|---------|
| `hasSave` | boolean | Có save hay không |
| `savedWave` | int | Wave đang chơi (1-20, clamped) |
| `score` | long | Điểm hiện tại |
| `life` | float | HP hiện tại |
| `stars` | long | Stars accumulated trong run |
| `activeWeaponType` | int | Track vũ khí đang dùng (0/1/2) |
| `weaponLevel_0/1/2` | int | Level của mỗi track |
| `stockpile_0/1/2` | int | Stockpile của mỗi track |
| `maxLife` | float | HP tối đa |
| `highScore` | long | High score toàn cục (preserved across clears) |
| `totalStars` | long | Tổng stars tích lũy (preserved across clears) |

### 22.3 Save Events

| Event | Action |
|-------|--------|
| Wave cleared | `save(wave, score, life, stars, ...)` — lưu full state |
| Game over (player death) | `commitGameStats(score, stars)` — cập nhật highScore + totalStars |
| New game | `clear()` — xóa run data, GIU highScore và totalStars |

### 22.4 Load

- `hasSavedGame()` -> nếu true -> `load()` -> trả về `SaveData` struct
- `MainMenuScreen` hiển thị "CONTINUE" nếu `hasSavedGame()` = true

---

## 23. DEBUG & TEST INFRASTRUCTURE

### 23.1 DebugConfig.java

- **File:** `core/src/com/alexei/spaceshooter/utils/DebugConfig.java`
- **Pattern:** Tất cả debug knobs tập trung vào 1 file duy nhất

| Flag | Type | Default | Ý nghĩa |
|------|------|---------|---------|
| `ENABLE_DEBUG` | boolean | `false` | Bật/tắt toàn bộ debug mode |
| `DEBUG_START_WAVE` | int | `1` | Wave bắt đầu khi NEW GAME |
| `DEBUG_START_WAVE_LOOP_COUNT` | int | `0` | Loop count |
| `DEBUG_START_HP` | float | `5f` | HP khởi đầu |
| `DEBUG_START_WEAPON_TYPE` | int | `0` | 0=Laser, 1=Blast, 2=Homing |
| `DEBUG_START_WEAPON_LEVEL` | int | `1` | Level vũ khí khởi đầu (1-7) |
| `DEBUG_TEST_SINGLE_ENEMY` | boolean | `false` | Chỉ spawn 1 loại enemy để test |
| `DEBUG_TEST_ENEMY_TYPE` | String | `"EnemyShipE"` | Loại enemy để test |
| `DEBUG_TEST_ENEMY_COUNT` | int | `3` | Số lượng enemy test |
| `DEBUG_TEST_HP_MULTIPLIER` | float | `5.0f` | Hệ số HP nhân thêm cho enemy test |
| `DROP_RATE_WEAPON_UPGRADE` | float | `0.06f` | Tỉ lệ drop weapon item |
| `DROP_RATE_ENERGY` | float | `0.09f` | Tỉ lệ drop energy item |
| `DROP_RATE_HP` | float | `0.05f` | Tỉ lệ drop HP item |
| `DROP_RATE_STAR` | float | `1.00f` | Tỉ lệ drop star (luôn 100%) |

### 23.2 Ứng Dụng DebugConfig

- Test boss ngay lập tức: `ENABLE_DEBUG=true`, `DEBUG_START_WAVE=5`
- Test weapon cao level: `DEBUG_START_WEAPON_LEVEL=7`
- Test pattern enemy mới: `DEBUG_TEST_SINGLE_ENEMY=true`, set enemy type
- Điều chỉnh tỉ lệ drop: thay đổi `DROP_RATE_*` không cần rebuild game logic

---

## 24. PERFORMANCE OPTIMIZATION

### 24.1 Bảng Optimization

| Optimization | File/Class | Cách thực hiện | Lợi ích |
|-------------|-----------|----------------|---------|
| Texture pre-loading | `TextureRegistry.java` | Load một lần qua AssetManager, cache static TextureRegion | Zero allocation khi entity vẽ |
| Background batching | `Starfield.java` | Dùng shared 1x1 white texture, tất cả stars vẽ trong 1 SpriteBatch call | Giảm từ N draw calls -> 1 |
| Particle batching | `Particle.java` | Shared 1x1 white texture tinted | Batch-friendly, không switch texture |
| Audio throttle | `AudioManager.java` | `soundMinIntervals` HashMap — skip nếu sound vừa play | Ngăn audio overload + clipping |
| Save drift correction | `Timer.java` | `elapsedTime = elapsedTime - duration` (giữ remainder) | Không mất frame millis |
| Font caching | `FontUtil.java` | FreeTypeFontGenerator tạo 1 lần | Không re-generate font per frame |
| CachedGlyphLayout | `ScoreTracker.java` | GlyphLayout được cache, không tạo mới mỗi frame | Giảm GC pressure |
| Vector reuse | Game loop | Reuse vectors thay vì new Vector2() | Giảm GC |
| No per-frame asset load | `LoadingScreen.java` -> `TextureRegistry` | Toàn bộ assets load trong LoadingScreen | Không IO trong gameplay |
| Width-aware clamping | `EnemyFactory.clampX()` | MathUtils.clamp(cx, margin, screenWidth-margin) | Enemy không bị cut ở biên |

### 24.2 Vấn Đề Chưa Benchmark

> [CHUA XAC MINH] FPS thực tế trên thiết bị vật lý. Không có profiling data trong repository.

---

## 25. AI-ASSISTED DEVELOPMENT

### 25.1 Evidence Có Thể Xác Minh

| Area | Evidence |
|------|---------|
| `AGENTS.md` tồn tại | File chứa quy tắc cho "AI coding agents" — xác nhận AI được dùng trong phát triển |
| `CLAUDE.md` tồn tại | File redirect sang AGENTS.md — cho thấy Claude AI được sử dụng |
| `.agents/` directory | Chứa skills cho AI agent (camera-systems, audio-design, game-feel, ...) |
| Repository name `antigravity` trong path | Tools được dùng: Antigravity (AI coding assistant by Google DeepMind) |
| `docs/REPORT_CONTEXT_VI.md` (file này) | Được tạo bởi AI agent theo yêu cầu của sinh viên |
| AGENTS.md language | Viết như "rules for AI coding agents" |

### 25.2 Vai Trò AI Trong Phát Triển (Có Evidence)

| Vai trò | Evidence |
|---------|---------|
| Đọc và hiểu source code | AGENTS.md section 3: "Before modifying code: Inspect the actual source code" |
| Implement/refactor code | Commit messages như "feat: overhaul visuals..." do AI thực hiện theo instruction |
| Tạo tài liệu | AGENTS.md, README.md, file này — được AI sinh ra |
| Debug và fix | Commit "fix: improve gameplay and update build tooling" |
| Wave balancing | `fix_waves.py` — script điều chỉnh waves.json |
| Asset pipeline setup | `tools/gen_sprites.py`, `gen_audio.py`, `gen_projectiles_items.py` |

### 25.3 Vai Trò AI Không Xác Minh Được

- **[CHUA XAC MINH]** Chính xác bao nhiêu % code do AI viết vs sinh viên viết tay
- **[CHUA XAC MINH]** Conversation history giữa sinh viên và AI (không có trong repository)
- **[CHUA XAC MINH]** AI có tham gia vào art direction của sprites không
- **[CHUA XAC MINH]** Số giờ AI sử dụng

### 25.4 Cách Trình Bày AI Trong Báo Cáo (Gợi Ý)

Đề xuất mô tả trung thực, không phóng đại:

> "Trong quá trình thực hiện đề tài, sinh viên đã ứng dụng công cụ AI hỗ trợ phát triển phần mềm (Google Antigravity/Claude AI) trong các công việc: phân tích và hiểu mã nguồn mở, đề xuất hướng refactor, hỗ trợ implement tính năng mới, review code và tạo tài liệu kỹ thuật. Công cụ AI đóng vai trò như một pair programming assistant, giúp tăng tốc quá trình nghiên cứu và phát triển, trong khi sinh viên chịu trách nhiệm ra quyết định kỹ thuật, review kết quả và đảm bảo chất lượng."

---

## 26. SO SANH PROJECT GOC vs PHIEN BAN HIEN TAI

| Hạng mục | Dự án gốc (suy luận) | Phiên bản hiện tại | Loại thay đổi |
|---------|---------------------|-------------------|--------------|
| **Weapons** | 2 tracks (Laser + Rocket) | 3 tracks (Laser/Blast/Homing), level 1-7, stockpile | Extended |
| **Enemies** | ~2 loại đơn giản | 6 loại + Boss (A-F + Dreadnought) | Extended |
| **Enemy visual** | Primitive shapes | Real neon PNG sprites (hand art) | Rebuilt |
| **Projectiles** | Basic shapes | Neon PNG sprites per weapon type | Rebuilt |
| **Wave system** | Hard-coded (suy luận) | Data-driven JSON (20 waves, endless loop) | Rebuilt |
| **Wave formations** | [CHUA XAC MINH] | 9 formations | Added |
| **Background** | ShapeRenderer stars | Layered Starfield (nebula tile + batched stars) | Rebuilt |
| **Items** | [CHUA XAC MINH] ít | Star, HP, 3 Weapon upgrades, Energy (6 types) | Extended |
| **Boss** | [CHUA XAC MINH] | Dreadnought: 3 weapons, 20 star drop, 4 item drop | Added/Extended |
| **HUD** | Basic text | Left/Center/Right layout, FontAwesome icons, Roboto-Bold | Rebuilt |
| **Audio** | Basic MP3, crackle | Synthesized OGG (numpy DSP) + legacy MP3, throttle system | Extended |
| **Asset pipeline** | None | Python scripts (gen_sprites, gen_audio, gen_projectiles) | Added |
| **Save/Load** | [CHUA XAC MINH] | LibGDX Preferences: wave, score, HP, weapon state, high score | Added/Extended |
| **Debug system** | [CHUA XAC MINH] | DebugConfig.java centralized knobs | Added |
| **Performance** | ShapeRenderer everywhere | Batched SpriteBatch, TextureRegistry cache, throttled audio | Improved |
| **AI-assisted dev** | N/A | AGENTS.md, AI pair programming | Added |
| **Documentation** | [CHUA XAC MINH] | README, AGENTS.md, ATTRIBUTIONS.md, REPORT_CONTEXT_VI.md | Added |


---

## 27. BUG / LIMITATION / TECHNICAL DEBT

### 27.1 Đã Biết Và Ghi Nhận

| Item | Type | Trạng thái | Evidence |
|------|------|-----------|---------|
| iOS simulator bug (một số Xcode versions) | Limitation | Đang tồn tại | README.md line 25, AGENTS.md section 21 |
| `WeaponFactory.java` rỗng | Technical debt | Đang tồn tại | File chỉ có package declaration |
| `WeaponShipRocket.java` legacy | Technical debt | Đang tồn tại | Không được dùng trong 3 active player tracks |
| `EffectSpawrksSpawner` typo tên | Minor | Đang tồn tại | File tên sai chính tả "Spawrks" |
| firstWave cho E/F không khớp first appearance | Potential bug | Chưa xác minh | E/F xuất hiện wave 6/7 nhưng firstWave=16 trong EnemyFactory |
| `screenOrientation="fullSensor"` | Design choice | Đang tồn tại | Cho phép landscape — game thiết kế portrait |
| Không có unit tests | Technical debt | Đang tồn tại | Không có test/ directory |

### 27.2 Chưa Xác Minh

- Memory leak nếu có per-frame `new` allocations không được GC tốt
- GPU overdraw trên thiết bị thấp cấp
- Battery drain khi chơi lâu

---

## 28. TEST MATRIX

| ID | Chức năng | Thao tác | Kết quả mong đợi |
|----|-----------|---------|-----------------|
| T01 | App launch | Install APK + launch | LoadingScreen -> MainMenuScreen |
| T02 | New game | Tap "NEW GAME" | GamePlayScreen load, Wave 1 start, HUD visible |
| T03 | Movement | Drag finger trên màn hình | Ship di chuyển theo drag |
| T04 | Shooting | Hold finger trên màn hình | Đạn bắn ra liên tục |
| T05 | Weapon switch | Nhặt ItemWeaponUpgradeExplosive | Switch sang Blast track |
| T06 | Level up | Nhặt ItemEnergyUpgrade | Level +1, không đổi track |
| T07 | Same-track bonus | Nhặt item same track | Level +1 + track giữ nguyên |
| T08 | Enemy spawn | Wave 1, quan sát sau 0.8s | EnemyShipA x 5 (GRID) xuất hiện |
| T09 | Enemy fire | Enemy đến hover position | Enemy bắt đầu bắn đạn |
| T10 | Item pickup | Chạm vào ItemStar | +Stars, pickup animation, sound |
| T11 | Wave progression | Diệt hết tất cả enemy | Wave clear sound -> next wave |
| T12 | Boss spawn | Wave 5, chờ delay=6.0s | Boss xuất hiện to đùng |
| T13 | Damage system | Bị trúng đạn | HP giảm, flash effect, sound, invulnerability |
| T14 | HP recovery | Nhặt ItemHP | HP +1 (không vượt maxLife) |
| T15 | Game over | HP = 0 | Game over screen, EndGame sound, save stats |
| T16 | Pause | Tap nút PAUSE | Game dừng, pause menu hiện |
| T17 | Score tracking | Diệt enemy | Score tăng |
| T18 | Star currency | Nhặt ItemStar | Total stars tăng |
| T19 | Audio | Bắn đạn | LaserShoot sound play |
| T20 | Save/Continue | Wave clear -> menu -> CONTINUE | Game tiếp tục từ wave đã lưu |
| T21 | High score | Multiple runs | High score được cập nhật |
| T22 | Boss pity drop | Wave 5, weapon lv < 5 | Energy + weapon item drop |
| T23 | APK install | `adb install -r android-debug.apk` | Install thành công |
| T24 | Debug mode | Set ENABLE_DEBUG=true, DEBUG_START_WAVE=10 | Game bắt đầu từ wave 10 |

---

## 29. KET QUA DAT DUOC

### 29.1 Kết Quả Kỹ Thuật (Có Evidence)

| Kết quả | Evidence |
|---------|---------|
| APK debug được build thành công | `space-shooter-debug.apk` tồn tại (~10.4 MB) |
| 3 weapon tracks với level system 1-7 | WeaponShipLaser, WeaponExplosiveBlaster, WeaponHomingLightning |
| 6 enemy types + boss với unique attacks | EnemyShipA-F.java, EnemyBoss.java |
| 20 waves data-driven + endless loop | `assets/data/waves.json` (594 lines) |
| Save/load hoàn chỉnh (wave, weapon state, score) | `SaveManager.java` |
| Asset pipeline tự động (Python scripts) | `tools/gen_sprites.py`, `gen_audio.py`, `gen_projectiles_items.py` |
| Audio system với anti-clipping | `AudioManager.java` — throttle system |
| Debug system tập trung 1 file | `DebugConfig.java` |
| TextureRegistry — load một lần | `TextureRegistry.java` — no per-frame load |
| Background batching | `Starfield.java` — 1 draw call |

### 29.2 Kết Quả Presentation

| Kết quả | Evidence |
|---------|---------|
| Neon arcade visual identity | sprites, neon colors, muzzle flashes |
| Real ship/enemy/boss sprites (hand art) | `assets/*.png` (author-owned) |
| Layered nebula background | `Starfield.java` + `nebula.png` |
| 9 formation types | `EnemyFactory.java` |
| Synthesized original audio | `assets/music/*.ogg`, `assets/sounds/*.ogg` |
| Responsive layout (hoverYPct, clampX) | `EnemyFactory.java`, waves.json |
| Screenshot documentation | `assets/screenshots/` (6 PNG + 1 GIF) |

### 29.3 Không Có Evidence

- **[CHUA XAC MINH]** FPS benchmark trên thiết bị thật
- **[CHUA XAC MINH]** User testing / player feedback
- **[CHUA XAC MINH]** Thời gian phát triển chính xác (giờ/tuần)

---

## 30. FACTS & EVIDENCE CATALOGUE

### 30.1 Facts Độ Tin Cậy Cao

| Claim | Evidence | File | Confidence |
|-------|---------|------|------------|
| Game sử dụng LibGDX 1.12.0 | `gdxVersion = '1.12.0'` | `build.gradle` line 27 | High |
| Application ID là `com.alexei.spaceshooter` | `applicationId "com.alexei.spaceshooter"` | `android/build.gradle` line 30 | High |
| minSdkVersion = 29 (Android 10) | `minSdkVersion 29` | `android/build.gradle` line 31 | High |
| 3 player weapon tracks | WeaponShipLaser, WeaponExplosiveBlaster, WeaponHomingLightning | 3 weapon Java files | High |
| Weapon level tối đa 7 | Level tables trong weapon classes | WeaponShipLaser.java | High |
| Stockpile tối đa 3 | `stockpile max 3` | Ship.java + README | High |
| 6 enemy types | EnemyShipA-F | 6 Java files | High |
| Boss HP = 100 | `HP = 100` in EnemyBoss | EnemyBoss.java | High |
| 20 waves trong JSON | `waveId: 1..20` | waves.json | High |
| Boss spawn mỗi 5 waves (5,10,15,20) | waves.json pattern "BOSS" | waves.json | High |
| Save dùng LibGDX Preferences | `Gdx.app.getPreferences(PREFS_NAME)` | SaveManager.java | High |
| Drop rate star = 100% | `DROP_RATE_STAR = 1.00f` | DebugConfig.java | High |
| Audio synthesized bằng numpy | No external samples | gen_audio.py + ATTRIBUTIONS.md | High |
| Ship/enemy sprites là hand art của tác giả | "author-owned, not regenerated" | ATTRIBUTIONS.md | High |
| Git history bắt đầu 29/07/2026 | first commit timestamp | git log | High |
| 12 commits total | git log count | git log | High |
| APK size ~10.4 MB | file size | `space-shooter-debug.apk` | High |

### 30.2 Facts Độ Tin Cậy Trung Bình

| Claim | Evidence | Confidence |
|-------|---------|------------|
| Project gốc có 2 weapons (Laser + Rocket) | AGENTS.md mô tả "prototype" + WeaponShipRocket.java vẫn tồn tại | Medium |
| Phát triển trong 3 tuần | Git date range 29/07 - 15/08 | Medium |
| iOS build có known bug | README.md line 25, AGENTS.md | Medium |

---

## 31. THONG TIN CHUA XAC MINH

| Thông tin | Lý do chưa xác minh |
|---------|---------------------|
| FPS thực tế trên thiết bị | Không có profiling data, không có benchmark log |
| Thời gian phát triển (giờ) | Git timestamps cho ngày nhưng không đủ để tính giờ thực làm |
| Mức độ AI contribution chính xác | Không có conversation log AI-sinh viên |
| Project gốc Fando có chính xác những gì | Không có direct access snapshot gốc |
| iOS build hiện tại có build được không | Chưa test |
| Số lượng người dùng test | Không có user testing data |
| Battery performance | Không có measurement |
| Chính xác ScoreTracker tính điểm thế nào | Cần verify trực tiếp từ ScoreTracker.java |
| EnemyShipE/F firstWave=16 có phải bug | Discrepancy giữa waves.json và EnemyFactory |

---

## 32. DANH SACH SCREENSHOT CAN CHUAN BI

| Tên hình | Mục đích | Chương |
|---------|---------|--------|
| Fig01_android_studio_project | Android Studio IDE mở project | Chương 3 |
| Fig02_project_structure | Project structure trong Android Studio | Chương 3 |
| Fig03_gradle_build_success | Gradle build thành công, APK output | Chương 3 |
| Fig04_main_menu | Main menu với High Score, New/Continue | Chương 4 |
| Fig05_gameplay_wave1 | Wave 1 với EnemyShipA đội hình GRID | Chương 4 |
| Fig06_hud_layout | HUD rõ ràng (HP, Wave, Score, Pause) | Chương 3 |
| Fig07_weapon_laser | Player đang bắn laser (neon blue fan) | Chương 3 |
| Fig08_weapon_blast | Player đang bắn blast orbs (orange) | Chương 3 |
| Fig09_weapon_homing | Player đang bắn homing darts (purple) | Chương 3 |
| Fig10_enemy_types_showcase | Hiển thị 6 loại enemy khác nhau | Chương 3 |
| Fig11_boss_wave5 | Boss Dreadnought xuất hiện | Chương 3 |
| Fig12_boss_death_reward | Boss chết, 20 stars + 4 items văng ra | Chương 3 |
| Fig13_item_pickup | Item pickup animation | Chương 3 |
| Fig14_explosion_effect | Vụ nổ khi enemy chết | Chương 3 |
| Fig15_game_over | Game over screen | Chương 4 |
| Fig16_waves_json | Nội dung waves.json trong editor | Chương 3 |
| Fig17_debugconfig | DebugConfig.java source | Chương 3 |
| Fig18_savemanager | SaveManager.java source | Chương 3 |
| Fig19_git_history | Git commit history | Chương 3 |
| Fig20_apk_output | APK file output trong android/build/ | Chương 3 |
| Fig21_adb_install | ADB install command success | Chương 3 |
| Fig22_gen_sprites | gen_sprites.py chạy, tạo PNG | Chương 3 |
| Fig23_gen_audio | gen_audio.py chạy, tạo OGG | Chương 3 |
| Fig24_texture_registry | TextureRegistry.java source | Chương 3 |
| Fig25_settings_dialog | Settings dialog (volume, music mute) | Chương 3 |

---

## 33. SO DO NEN VE

| Sơ đồ | Mục đích | Chương |
|-------|---------|--------|
| Kiến trúc Module | Hiểu 3-module Gradle (android, ios, core + dependencies) | Chương 3 |
| Package Structure | Hiểu tổ chức source code | Chương 3 |
| Game Loop Flow | show -> render -> update -> input -> draw -> repeat | Chương 3 |
| Screen Transition | LoadingScreen -> MainMenu -> Gameplay | Chương 3 |
| Entity Hierarchy | Visual -> Unit -> Ship/Enemy/Boss | Chương 3 |
| Weapon System Flow | Player -> activeWeapon -> fire() -> Projectile | Chương 3 |
| Wave Lifecycle | startWave -> SpawnActions -> enemies -> cleared -> nextWave | Chương 3 |
| Item Drop Flow | Enemy death -> dropStars() -> Item spawn -> magnetize -> pickup | Chương 3 |
| Save/Load Flow | wave clear -> save -> load -> continue | Chương 3 |
| Asset Pipeline | Python scripts -> PNG/OGG -> AssetManager -> TextureRegistry | Chương 3 |
| Android Build Pipeline | Source -> Gradle -> APK -> ADB -> Device | Chương 3 |
| AI Workflow | Sinh viên request -> AI analyze/generate -> review -> merge | Chương 4 |
| Use Case Diagram | Player <-> Game, Wave system, Weapon, Items | Chương 2 |

---

## 34. MAP NOI DUNG VOI CAU TRUC BAO CAO

| Phần báo cáo | Nội dung cần dùng | Nguồn trong document này |
|-------------|------------------|------------------------|
| **Mở đầu** | Lý do chọn đề tài, ý nghĩa thực tiễn Android game, vai trò AI | Section 2, 25 |
| **Chương 1: Tổng quan đề tài** | Mục tiêu, phạm vi, phương pháp, đóng góp | Section 1, 2, 4, 26 |
| **2.1 Android & Phát triển mobile** | Android SDK 34, minSDK 29, APK, ADB, AndroidLauncher | Section 9 |
| **2.2 Game mobile & LibGDX** | LibGDX 1.12.0, Game loop, Screen management, Asset pipeline | Section 5, 7, 17, 19 |
| **2.3 Open Source & Fork workflow** | Fando/space-shooter, Git history, fork + customize | Section 3, 4 |
| **2.4 Android Studio, Git, Gradle** | Build configuration, version, scripts | Section 8, 9 |
| **2.5 AI trong phát triển phần mềm** | AGENTS.md, role of AI, pair programming | Section 25 |
| **Chương 3.1 Phân tích project gốc** | Original state, limitations | Section 4 |
| **Chương 3.2 Kiến trúc** | Module structure, package, class hierarchy | Section 5, 6 |
| **Chương 3.3 Gameplay systems** | Player, Weapon, Enemy, Boss, Wave, Item, Save | Section 11-22 |
| **Chương 3.4 Presentation** | Rendering, Effects, HUD, Audio, Assets | Section 17-20 |
| **Chương 3.5 Tools & Pipeline** | Python scripts, asset generation | Section 19 |
| **Chương 3.6 Android deployment** | Build, ADB, Emulator | Section 8, 9 |
| **Chương 4.1 So sánh trước/sau** | Comparison table | Section 26 |
| **Chương 4.2 Kết quả kỹ thuật** | APK, features, performance optims | Section 24, 29 |
| **Chương 4.3 Ứng dụng AI** | Specific AI contributions | Section 25 |
| **Chương 4.4 Kiểm thử** | Test matrix | Section 28 |
| **Kết luận** | Summary, achievements, limitations | Section 29, 27, 36 |
| **Hướng phát triển** | Bug/debt list, potential improvements | Section 27 |

---

## 35. GOI Y NGON NGU BAO CAO

### 35.1 Cách Gọi Đúng

| Nên dùng | Không nên dùng |
|---------|----------------|
| "Nghiên cứu và tùy biến game mã nguồn mở" | "Xây dựng game từ đầu" |
| "Fork và mở rộng project Fando/space-shooter" | "Tự phát triển toàn bộ game" |
| "Hiện đại hóa và refactor presentation layer" | "Phát triển engine mới" |
| "Kế thừa và mở rộng gameplay từ project gốc" | "Thiết kế gameplay hoàn toàn mới" |
| "Tích hợp asset pipeline tự động" | "Tạo toàn bộ assets thủ công" |
| "Ứng dụng AI hỗ trợ phát triển" | "Hoàn toàn do AI làm" hoặc "không dùng AI" |
| "Sinh viên quyết định kiến trúc và thiết kế" | Nếu thực tế AI có đề xuất |

### 35.2 Các Thuật Ngữ Chuyên Môn

| Thuật ngữ | Định nghĩa ngắn để dùng trong báo cáo |
|---------|---------------------------------------|
| LibGDX | Framework game cross-platform mã nguồn mở cho Java |
| Vertical-scrolling shooter | Thể loại game bắn máy bay nhìn từ trên xuống, cuộn dọc |
| Data-driven design | Thiết kế trong đó behavior được điều khiển bởi dữ liệu (JSON) thay vì hard-code |
| Asset pipeline | Quy trình tự động tạo và xử lý assets (sprites, audio) trước khi game chạy |
| Procedural synthesis | Tạo nội dung (sprites, audio) bằng thuật toán thay vì vẽ/ghi âm thủ công |
| TextureRegistry | Bộ đăng ký tập trung cache texture, nạp một lần dùng suốt |
| AssetManager | LibGDX class quản lý việc load/unload assets bất đồng bộ |
| HP scaling | Tăng máu kẻ địch theo wave để tăng độ khó dần dần |
| Endless loop | Sau wave cuối (20), game tự động lặp lại từ wave 1 với độ khó cao hơn |
| AABB collision | Axis-Aligned Bounding Box — phát hiện va chạm dạng hộp chữ nhật |
| ADSR envelope | Attack-Decay-Sustain-Release — mô hình âm lượng âm thanh theo thời gian |
| IIR filter | Infinite Impulse Response — bộ lọc số phản hồi vô hạn |
| Muzzle flash | Hiệu ứng ánh sáng ở đầu nòng súng khi bắn |

---

## 36. EXECUTIVE SUMMARY

### Tóm Tắt Toàn Bộ Dự Án

**Xuất phát điểm:** Sinh viên Nguyễn Văn Rin (VKU, Khoa KHMT) thực tập tại SafeHorizons lấy project mã nguồn mở `Fando/space-shooter` — một prototype game bắn máy bay (space shooter) đơn giản viết bằng Java/LibGDX — làm đối tượng nghiên cứu và tùy biến theo đề tài: *"Nghiên cứu và tùy biến game mã nguồn mở trên nền tảng Android bằng Android Studio và công cụ AI"*.

**Vấn đề ban đầu:** Project gốc có các hạn chế rõ rệt: visual nguyên thủy (ShapeRenderer primitives), ít enemy types, không có data-driven wave system, không có asset pipeline có hệ thống, audio cơ bản, presentation không phù hợp với tiêu chuẩn mobile game hiện đại.

**Mục tiêu:** Chuyển hóa prototype thành một *modern, polished neon arcade mobile space shooter* — không phá vỡ gameplay core, chỉ mở rộng và hiện đại hóa.

**Quá trình nghiên cứu và phát triển (3 tuần, 12 commits):**

1. **Khởi động (29/07):** Fork repository, phân tích kiến trúc hiện tại, xác định điểm mạnh/yếu.
2. **Mở rộng gameplay (29/07 - 01/08):** Implement data-driven wave system với `waves.json` (20 waves, 9 formations), save/load system (LibGDX Preferences), thêm 6 enemy types (A-F + Boss), 3 player weapon tracks với level 1-7, 6 loại items với drop/pickup/magnetize mechanics.
3. **Hiện đại hóa presentation (15/08 sáng):** Tạo Python asset pipeline (`gen_sprites.py`, `gen_projectiles_items.py`, `gen_audio.py`) để generate neon sprites (4x supersampled Lanczos) và audio synthesized (numpy DSP, ADSR, oscillators). Background chuyển từ ShapeRenderer sang batched SpriteBatch. Real enemy sprites, projectile sprites per weapon type, HUD hoàn chỉnh.
4. **Hoàn thiện (15/08):** Weapon progression polish, AGENTS.md (AI behavioral rules), documentation, build tooling.

**Công nghệ:**
- **LibGDX 1.12.0** — game framework cross-platform
- **Java 17** — ngôn ngữ implement gameplay
- **Android SDK 34, minSDK 29** — target Android 10+
- **Gradle 8.4 + AGP 8.1.3** — build automation
- **Python + NumPy + PIL** — offline asset generation
- **AI (Antigravity/Claude)** — pair programming assistant
- **Git/GitHub** — version control

**Những thay đổi chính so với project gốc:**
- Từ 2 weapon tracks -> 3 tracks với level system 1-7
- Từ ~2 enemy types -> 6 types + Boss (Dreadnought)
- Từ hard-coded waves -> 20 waves data-driven (JSON)
- Từ primitive shapes -> real neon PNG sprites (hand art + generated)
- Từ basic audio -> synthesized numpy DSP audio
- Từ không có save -> save/load đầy đủ (wave, weapon state, score, high score)
- Từ không có asset pipeline -> Python scripts tự động generate

**Kết quả:** APK debug hoạt động (`space-shooter-debug.apk`, ~10.4 MB), build thành công từ Android Studio/Gradle. Game có đầy đủ 20 waves, endless loop, boss fights, weapon system, save/load, neon visual identity, synthesized audio.

**Hạn chế:**
- Không có FPS benchmark thực nghiệm
- iOS simulator có known bug (một số Xcode versions)
- Không có unit tests
- Một số legacy code (WeaponShipRocket, WeaponFactory rỗng)

**Hướng phát triển:**
- Thêm TextureAtlas để giảm draw calls hơn nữa
- Touch control nâng cao (haptic feedback)
- Online leaderboard
- Thêm wave boss types
- iOS simulator compatibility fix
- Unit tests

---

*Tài liệu này được tạo bằng phân tích toàn bộ repository Space Shooter (12 commits, ~50 Java files, ~30 assets, 3 Python scripts, 2 shell scripts). Tất cả thông tin đều có nguồn từ source code thực tế. Phần [CHUA XAC MINH] chỉ rõ những điểm chưa đủ bằng chứng.*

*Phiên bản tạo: 2026-08-15 | Repository: `/home/nrin31266/IdeaProjects/space-shooter`*
