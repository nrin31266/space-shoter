# Space Shooter

A modern, polished **vertical-scrolling arcade space shooter** for Android & iOS, built with
[LibGDX](https://libgdx.com/). You pilot a neon fighter, fight waves of increasingly varied alien
ships, collect stars/currency, upgrade weapons, and take down bosses.

> Inspired by classic mobile shooters such as Sky Force.

---

## Install Android app

- [Download and install the Android APK](space-shooter-debug.apk)
- Or clone the repo and build from source:

```
./gradlew :android:assembleDebug
```

The resulting APK is written to `android/build/outputs/apk/debug/android-debug.apk`.

## Install iOS app

- Clone the repo and build from source.
- > There's a known bug preventing the iOS build from running on the simulator for some Xcode versions.

## Desktop / Web

- Although LibGDX supports desktop & web targets, this project intentionally targets mobile only.

## Requirements

- **Android Studio** — building & running the Android app
- **Xcode** — building & running the iOS app
- **MobiVM plugin** for Android Studio — AOT compiler for Java → iOS

---

# Game Features

## Controls

- **Drag** anywhere on the screen to move the ship
- **Hold** the screen to shoot continuously

## Weapons (3 tracks, single shared level 1→7)

| Track | Style | Max shots | Identity |
|-------|-------|-----------|----------|
| **Laser** | Neon blue beam fan | 5 beams | Fast, spread, low per-hit damage |
| **Blast** | Neon orange orbs (pierce) | 3 orbs | Pierces through enemies |
| **Homing** | Neon purple darts | 3 darts | Auto-tracks the nearest enemy |

- Eating a **weapon-switch** item changes your active track (eating the SAME track also +1 level).
- Eating a **power-up (energy)** item raises your weapon level without switching tracks.
- At boss waves (5/10/15/20) a pity drop guarantees an energy + weapon item if you're under Lv5.

## Enemies (6 types + boss)

Every enemy has a distinct silhouette, fire pattern, HP, and speed:

- **EnemyShipA** — basic grunt, slow red orb
- **EnemyShipB** — elite gunner, homing green orb
- **EnemyShipC** — sniper, aimed hot-pink orb
- **EnemyShipD** — tank, 3-way gold spread
- **EnemyShipE** — fast striker, twin purple pulses
- **EnemyShipF** — heavy dragoon, 4-way ring burst
- **Boss (Dreadnought)** — huge, 3 weapons: plasma fan + fast volley + aimed plasma sniper

## Items

- **Star** (gold) — currency; magnetizes toward you
- **HP** (red cross) — heals +1 (only magnetizes when very close)
- **Weapon switch** (blue bolt / orange burst / purple crosshair) — change active track
- **Energy** (green cell) — raise weapon level

## Waves

- Data-driven from `assets/data/waves.json` (20 waves + endless loop).
- Wave 1–5 is a showcase progression: one new enemy per wave, boss climax at Wave 5.
- Enemy HP scales exponentially per wave/loop.

## Visuals & Feedback

- Real neon sprites (ships, projectiles, items), dark nebula space background.
- Muzzle flashes, layered explosions, hit sparks, screen shake, damage decals.
- Clean neon HUD: HP (red) + weapon left, WAVE top-center, star + score right, pause top-right.

## Audio

- Original synthesized soundtrack + distinct SFX for firing, hits, explosions, pickups,
  power-ups, boss warnings, wave start/clear — see `assets/ATTRIBUTIONS.md`.

---

# Development

## Build & run

### Android APK

```
./gradlew :android:assembleDebug
```

Output: `android/build/outputs/apk/debug/android-debug.apk` (also mirrored at repo root as
`space-shooter-debug.apk`).

### Emulator (Pixel AVD)

Convenience scripts at the repo root:

- `./start_pixel_emu.sh` — boots the `pixel_emu` AVD (headless, software GPU).
- `./build_run_spaceshooter.sh` — builds + installs + launches the game (starts the emulator if needed).

Manual:

```
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
nohup $HOME/Android/Sdk/emulator/emulator -avd pixel_emu -no-snapshot-save -no-audio -gpu swiftshader_indirect > /tmp/emu.log 2>&1 &
adb wait-for-device
adb install -r android/build/outputs/apk/debug/android-debug.apk
adb shell monkey -p com.alexei.spaceshooter -c android.intent.category.LAUNCHER 1
```

### iOS

```
./gradlew :ios:build
```

> Known limitation: may not run on the simulator for some Xcode versions.

## Debug / test mode

All knobs live in `core/src/com/alexei/spaceshooter/utils/DebugConfig.java`:

- `ENABLE_DEBUG = true` — apply debug values on NEW GAME
- `DEBUG_START_WAVE` / `DEBUG_START_WEAPON_LEVEL` / `DEBUG_START_WEAPON_TYPE` / `DEBUG_START_HP`
- `DEBUG_TEST_SINGLE_ENEMY` — spawn one enemy type to study it
- `DROP_RATE_*` — weapon/energy/HP/star drop rates

Production defaults: `ENABLE_DEBUG = false`, Wave 1, weapon Lv1.

## Asset pipeline

Sprites & identity audio are **generated offline** by Python scripts in `tools/`:

```
python3 tools/gen_projectiles_items.py
python3 tools/gen_audio.py
```

- `tools/gen_sprites.py` — ships, boss, beams, orbs, items, nebula
- `tools/gen_projectiles_items.py` — neon projectile/item/star set (`item_energy.png`, `orb_pink.png`, …)
- `tools/gen_audio.py` — synthesized soundtrack + SFX

> Ship PNGs (`ship.png`, `enemy*.png`, `enemy_boss.png`) are **author hand art** — never regenerate.
> Licensing: `assets/ATTRIBUTIONS.md`.

---

# What was built (modernization pass)

This repository started as a basic LibGDX prototype and has been rebuilt into a polished neon
arcade shooter. Highlights:

- **Visuals** — real neon sprites (ships, boss, projectiles, items), dark nebula background
  (calm: few small stars, faint halo rings, no falling streaks), layered explosions, muzzle
  flashes, hit sparks, damage decals.
- **Player** — 3 weapon tracks (Laser fan / Blast pierce / Homing darts), single shared level 1→7,
  stockpile up to 3, invulnerability shield, damage decals.
- **Weapons** — per-level tables (max 5 laser beams, max 3 blast orbs, max 3 homing darts);
  single-origin laser fan that never crosses at the nose; muzzle smoke on all 3; clamped
  ship-motion inheritance (a single shot always fires straight).
- **Enemies** — 6 distinct types + boss, each with a unique projectile identity (red orb, homing
  green, sniper pink, tank gold spread, twin purple, ring burst, boss plasma volley + aimed
  plasma). Enemies fire only after reaching their hover spot; HP ×2 / boss ×3; fly-in speed ×2.
- **Boss waves 5/10/15/20** — pity drop (energy + weapon switch) if weapon < Lv5.
- **Items** — Star (gold currency), HP, weapon-switch (3 icons), Energy (pure level-up);
  eating the same-track weapon also grants +1 level.
- **HUD** — clean left/centre/right layout (HP red + weapon left, WAVE top-centre, gold star +
  white score right, pause top-right), font ×1.5, always visible (intro & wave transitions).
- **Audio** — original synthesized soundtrack + distinct SFX per event, no crackle/clipping.
- **Waves 1–7** — showcase progression (one new enemy per wave, boss at 5, E at 6, F at 7);
  wave 8+ multi-squad. New formations: DIAMOND, CHECKERBOARD. Width-aware enemy clamping.
- **Performance** — batched background, cached GlyphLayout, reusable vectors, no per-frame asset
  loads, batched particle effects.

---

# Credits

- **Author / Developer:** [Nguyễn Văn Rin](mailto:nrin31266@gmail.com) (Rin Nguyen)
- Original prototype & engine work; this repository has been progressively modernized into a
  polished neon arcade shooter (visuals, audio, assets, HUD, wave design, combat feedback).

All sprites, music and identity SFX are original/synthesized assets generated by the scripts in
`tools/` (see `assets/ATTRIBUTIONS.md` for full details).

---

# Building / Recording gameplay

## Record Android screen (Android 11+)

Use the system **Screen Record** in the quick-settings tray (enable *screen touches* if desired).

## Extract frames from video with VLC

Install VLC → Settings → `Show All` → `Video → Filters` → `Scene filter` → set prefix/output dir
and a recording ratio → enable the filter → play the video. Frames are extracted while playing.

[<img src="assets/vlc/vlc-settings.png" width="500"/>](assets/vlc/vlc-settings.png)

## Make a GIF from a set of images with ffmpeg

```
ffmpeg -framerate 12 -pattern_type glob -i '*.png' -filter_complex "[0:v]scale=640:-2,split[x][z];[x]palettegen[y];[z][y]paletteuse" output.gif
```

---

# Screenshots

[<img src="assets/screenshots/space-shooter.gif" width="200"/>](assets/screenshots/space-shooter.gif)
[<img src="assets/screenshots/space-shooter-01.png" width="200"/>](assets/screenshots/space-shooter-01.png)
[<img src="assets/screenshots/space-shooter-02.png" width="200"/>](assets/screenshots/space-shooter-02.png)
[<img src="assets/screenshots/space-shooter-03.png" width="200"/>](assets/screenshots/space-shooter-03.png)
[<img src="assets/screenshots/space-shooter-04.png" width="200"/>](assets/screenshots/space-shooter-04.png)
[<img src="assets/screenshots/space-shooter-05.png" width="200"/>](assets/screenshots/space-shooter-05.png)

