# AGENTS.md — Space Shooter

> Canonical instructions for AI coding agents.
>
> This file defines how agents must work on the project.
> It does NOT duplicate detailed gameplay specifications.
>
> Human-facing project information is defined in `README.md`.
> The actual gameplay/system behavior is defined by the source code itself.

---

## 1. PROJECT IDENTITY

Space Shooter is a vertical-scrolling arcade space shooter for Android and iOS built with Java + LibGDX.

The project is an existing game/prototype that has already accumulated substantial gameplay systems.

The current objective is NOT merely to add small features.

The project should be progressively rebuilt/polished into a:

> modern, polished, asset-driven mobile arcade space shooter.

The game should feel like a real mobile game rather than a Java/LibGDX prototype.

Agents are therefore allowed to substantially refactor or replace the presentation layer when necessary.

---

## 2. SOURCE OF TRUTH

When determining existing behavior, use this priority:

1. Actual source code
2. `AGENTS.md`
3. `README.md`
4. Historical documentation

If these disagree:

- inspect the source code
- identify the discrepancy
- do not blindly overwrite working behavior
- preserve working behavior unless the task explicitly changes it

**The authoritative specification for gameplay/system invariants is the source code itself.**

Do NOT duplicate detailed wave tables, enemy statistics, weapon statistics, save schema, formulas, or item rules into this file.

---

## 3. REQUIRED BEHAVIOR FOR AGENTS

Before modifying code:

1. Read `AGENTS.md`.
2. Read `README.md` for project context.
3. Inspect the actual source code relevant to the task.
4. Inspect existing assets before creating replacements.
5. Understand how the affected system currently works.
6. Prefer incremental, verifiable changes over blind rewrites.
7. Verify the affected build after implementation.

Do not assume documentation perfectly describes the current implementation.

---

## 4. GAMEPLAY IS PROTECTED

The existing gameplay systems are valuable and must not be accidentally destroyed during visual or architectural refactoring.

Treat the gameplay rules in the source code as protected unless the user explicitly asks to change them.

This includes, among other things:

- wave progression
- endless wave loop
- enemy behavior
- enemy scaling
- boss encounters
- weapon progression
- active weapon rules
- stockpile behavior
- HP/damage behavior
- item behavior
- boss rewards
- debug configuration
- score/star persistence
- save/load compatibility

If a presentation refactor requires touching gameplay code:

- preserve behavior
- separate presentation changes from gameplay changes where possible
- verify the affected behavior afterward

---

## 5. PRESENTATION MAY BE REBUILT

The current presentation is considered an area that may require substantial modernization.

Agents should NOT feel constrained to preserve weak visual implementations merely because they already exist.

The following may be replaced or heavily refactored when appropriate:

- primitive rendering
- placeholder graphics
- basic projectiles
- simplistic enemy visuals
- basic explosions
- particle effects
- HUD
- menus
- dialogs
- typography
- UI skin
- background presentation
- audio implementation
- asset loading
- rendering organization

The goal is quality, not preservation of outdated implementation details.

Do not confuse gameplay preservation with presentation preservation.

---

## 6. VISUAL QUALITY TARGET

The desired visual direction is:

**Modern neon arcade space shooter.**

The final presentation should have:

- real ship/enemy artwork
- coherent silhouettes
- attractive projectiles
- layered explosions
- particle effects
- muzzle flashes
- hit effects
- shield effects
- engine/exhaust effects
- polished boss presentation
- animated space background
- modern HUD
- polished menus
- coherent typography
- consistent colors and visual hierarchy

Avoid a final result dominated by:

- circles
- rectangles
- ShapeRenderer primitives
- default widgets
- debug text
- placeholder assets
- random icons
- inconsistent visual styles

Primitive rendering is acceptable as a fallback or for effects where it is genuinely appropriate, but it should not define the visual identity of the game.

---

## 7. REAL ASSETS ARE ENCOURAGED

Do not assume every visual must be procedurally drawn in Java.

When suitable assets are unavailable in the repository, agents may research and integrate legally usable external assets.

Preferred sources include reputable free/open asset libraries such as:

- Kenney
- OpenGameArt
- itch.io free assets
- Freesound
- Pixabay
- other reputable sources with clear licensing

Do NOT:

- scrape random Google Images
- use copyrighted game assets
- download assets without checking licensing
- introduce assets with unclear redistribution rights

When external assets are introduced, record their source and license in:

`docs/ASSETS.md`

Prefer assets that can be adapted consistently to the game's visual style rather than mixing unrelated asset packs.

---

## 8. ASSET PIPELINE

Prefer a proper asset pipeline over loading many independent files at runtime.

Use appropriate LibGDX mechanisms such as:

- `AssetManager`
- `TextureAtlas`
- sprite regions
- cached fonts
- pooled effects
- reusable sounds
- reusable particle definitions

Organize assets clearly.

Do not load textures, sounds, fonts, or particle definitions repeatedly during gameplay.

Do not create expensive visual resources inside `render()` or other per-frame paths.

---

## 9. PERFORMANCE IS A REQUIREMENT

The target platforms are mobile.

Visual improvements must not casually introduce frame drops.

Agents should inspect for:

- per-frame allocations
- repeated texture creation
- repeated asset loading
- excessive SpriteBatch flushes
- excessive texture switching
- unnecessary ShapeRenderer usage
- excessive particle counts
- unnecessary Scene2D layout work
- repeated font creation
- repeated sound loading
- expensive effects executed unnecessarily

Prefer:

- batching
- atlases
- pooling
- caching
- object reuse
- efficient particle systems
- reasonable draw calls

Do not sacrifice visual quality unnecessarily.

The target is:

**polished + smooth mobile performance.**

---

## 10. AUDIO QUALITY

Audio should feel integrated with gameplay rather than being an afterthought.

The audio system should support appropriate separation between:

- music
- sound effects
- UI sounds

Gameplay should have satisfying audio feedback for important actions such as:

- shooting
- impacts
- explosions
- enemy destruction
- item pickup
- weapon upgrades
- player damage
- shield activation
- boss events
- wave transitions
- UI interactions

Use appropriate LibGDX audio APIs and cache reusable sounds.

Never repeatedly load audio during gameplay.

Prevent excessive simultaneous playback from becoming noisy or causing performance problems.

External audio must follow the same licensing/documentation rules as visual assets.

---

## 11. UI / UX

The UI should be treated as part of the game's visual identity.

Do not rely on default VisUI appearance as the final visual design.

VisUI may be retained where useful, but custom styling should be used where appropriate.

Important UI areas include:

- main menu
- HUD
- pause menu
- game over
- settings
- loading screen
- boss health
- wave transitions

UI should be:

- readable
- responsive
- consistent
- touch-friendly
- suitable for mobile aspect ratios
- visually consistent with gameplay

Avoid hard-coded layouts that only work on one screen size.

---

## 12. RESPONSIVE MOBILE DESIGN

The project targets Android and iOS.

Consider different:

- aspect ratios
- resolutions
- screen densities
- portrait/vertical layouts
- safe areas

Do not design the UI around a single fixed device.

Prefer Scene2D layout systems such as:

- `Table`
- `Stack`
- responsive containers

where appropriate.

---

## 13. RENDERING ARCHITECTURE

Keep gameplay and presentation reasonably separated.

Gameplay systems should remain responsible for:

- state
- movement
- combat
- collisions
- progression

Presentation systems should handle:

- sprites
- animations
- particles
- effects
- UI
- audio feedback

Do not introduce abstraction layers merely for architectural fashion.

Refactor when it makes the code easier to maintain, test, or evolve.

---

## 14. LIBGDX / DEPENDENCY CHANGES

Before upgrading LibGDX or another dependency:

1. Inspect the current version.
2. Check compatibility with the existing project.
3. Check Android compatibility.
4. Check iOS/MobiVM compatibility.
5. Check third-party dependencies such as VisUI.
6. Build and verify afterward.

Do not upgrade dependencies blindly just because a newer version exists.

Prefer stable compatible versions over breaking upgrades.

---

## 15. SAFE REFACTORING

When rebuilding a subsystem:

1. understand the current implementation
2. identify behavior that must remain
3. design the replacement
4. implement
5. remove obsolete code only after the replacement works
6. compile
7. test the affected behavior

Avoid leaving multiple competing implementations of the same system.

Avoid dead code.

Avoid unnecessary compatibility layers after a successful migration.

---

## 16. NO BLIND MASS REWRITES

"Rebuild" does NOT mean:

> delete everything and hope the project compiles.

Agents must preserve the working game systems and progressively replace weak implementations.

A large refactor is acceptable when justified.

A large refactor without understanding dependencies is not.

---

## 17. DOCUMENTATION

Keep documentation responsibilities separated:

### `AGENTS.md`
AI behavior, engineering rules, architecture boundaries and project conventions.

### `README.md`
Human-facing project information, setup, features, usage and credits.

### `docs/ASSETS.md`
External asset sources, licenses and attribution information.

Do not duplicate large sections between these documents.

When architecture changes materially, update `AGENTS.md`.

When gameplay behavior changes materially, update `README.md` and the relevant source code.

---

## 18. VERIFICATION

After meaningful changes:

- compile the affected module
- run the appropriate Gradle task
- verify asset loading
- verify audio loading
- verify save/load compatibility when relevant
- inspect UI on relevant screen sizes
- verify gameplay behavior affected by the change

Do not claim a build/test succeeded unless it was actually run.

---

## 19. DEFAULT DECISION RULE

When choosing between:

A. preserving an old implementation

and

B. replacing it with a cleaner, more modern implementation

prefer B when:

- gameplay behavior is preserved
- mobile performance is maintained
- the implementation is simpler or more maintainable
- the visual/audio result is materially better
- compatibility is maintained

The project is allowed to evolve significantly.

---

## 20. CORE PRINCIPLE

Preserve the GAME.

Modernize the IMPLEMENTATION.

Rebuild the PRESENTATION.

Optimize for MOBILE.

Prefer REAL ASSETS over crude placeholders.

Prefer SMOOTH AUDIO over noisy or broken audio.

Prefer POLISHED UX over default widgets.

Do not break the protected gameplay rules (see section 4).

The objective is a polished, modern mobile arcade game — not merely a technically working LibGDX prototype.

---

## 21. BUILD & RUN

The project targets **Android** (primary) and **iOS**. Desktop/web targets are intentionally disabled.

### Build the Android APK

```
./gradlew :android:assembleDebug
```

Output APK: `android/build/outputs/apk/debug/android-debug.apk`
(also mirrored at the repo root as `space-shooter-debug.apk` for easy install).

### Install on a connected device/emulator

```
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

### Run on the Android emulator (Pixel AVD)

There are two convenience scripts at the repo root:

- `./start_pixel_emu.sh` — boots the `pixel_emu` AVD (headless, software GPU).
- `./build_run_spaceshooter.sh` — builds the debug APK, starts the emulator (if needed),
  installs and launches the game.

Manual emulator steps:

```
export ANDROID_SDK_ROOT=$HOME/Android/Sdk
nohup $HOME/Android/Sdk/emulator/emulator -avd pixel_emu -no-snapshot-save -no-audio -gpu swiftshader_indirect > /tmp/emu.log 2>&1 &
adb wait-for-device
# wait for sys.boot_completed == 1
adb install -r android/build/outputs/apk/debug/android-debug.apk
adb shell monkey -p com.alexei.spaceshooter -c android.intent.category.LAUNCHER 1
```

### iOS

```
./gradlew :ios:build     # requires Xcode + MobiVM plugin
```

> Known limitation: the iOS build may not run on the simulator for some Xcode versions.

### Verify after changes

After meaningful changes, at minimum run:

```
./gradlew :android:assembleDebug
```

Then install on the emulator and sanity-check: menu, Wave 1–5 progression, firing/effects,
HUD layout, pause, game-over, and audio (see section 18).

---

## 22. DEBUG / TEST MODE

All debug/test knobs live in ONE file: `core/src/com/alexei/spaceshooter/utils/DebugConfig.java`.

- `ENABLE_DEBUG = true` — apply the debug values below on NEW GAME.
- `DEBUG_START_WAVE` — start at a specific wave (1–20).
- `DEBUG_START_WEAPON_LEVEL` — start weapon level (1–7).
- `DEBUG_START_WEAPON_TYPE` — 0=Laser, 1=Blast, 2=Homing.
- `DEBUG_START_HP` — starting HP.
- `DEBUG_TEST_SINGLE_ENEMY` — spawn a single enemy type to study its pattern.
- `DROP_RATE_*` — weapon/energy/HP/star drop rates.

Production defaults: `ENABLE_DEBUG = false`, start Wave 1, weapon level 1.
To test a specific weapon/level, flip `ENABLE_DEBUG` and adjust the values, then revert.

---

## 23. ASSET PIPELINE (RECENT WORK)

Sprites and identity audio are **generated offline** by Python scripts in `tools/` (never at runtime):

- `tools/gen_sprites.py` — ships, boss, beams, orbs, items, nebula.
- `tools/gen_projectiles_items.py` — projectile/item/star/nebula textures (neon set incl. `item_energy.png`, `orb_pink.png`).
- `tools/gen_audio.py` — synthesized soundtrack + SFX.

Regenerate with:

```
python3 tools/gen_projectiles_items.py
python3 tools/gen_audio.py
```

Ship PNGs (`ship.png`, `enemy*.png`, `enemy_boss.png`) are **author-owned hand art** — do NOT regenerate
or overwrite them. Licensing/attribution is recorded in `assets/ATTRIBUTIONS.md`.

Textures are loaded once through `AssetManager` + `TextureRegistry` (see `LoadingScreen`), never
re-loaded per frame.

---

## 24. RECENT IMPLEMENTATION (this modernization pass)

Summary of the substantial work already done — read the source for exact behavior:

- **Visual overhaul** — neon arcade look: real ship/boss/projectile/item sprites, dark nebula
  background (calm: few small stars, faint halo rings, no falling streaks), neon HUD.
- **Player** — 3 weapon tracks (Laser fan / Blast pierce / Homing darts), single shared level 1→7,
  stockpile up to 3, invulnerability shield bubble, damage decals.
- **Weapons** — per-level tables (max 5 laser beams, max 3 blast orbs, max 3 homing darts),
  single-origin fan for the laser (never crosses at the nose), muzzle flash + smoke on all 3,
  clamped ship-motion inheritance (single shot always fires straight).
- **Enemies** — 6 distinct types + boss, each with unique projectile identity (red orb, homing
  green, sniper pink, tank gold spread, twin purple, ring burst, boss plasma volley + aimed
  plasma). Enemies only fire after reaching their hover spot (no "ngậm đạn"). HP ×2 / boss ×3,
  fly-in speed ×2.
- **Boss waves 5/10/15/20** — pity drop (energy + weapon switch) if player weapon < Lv5.
- **Items** — Star (gold currency, half-size), HP, weapon-switch (3 icons), Energy (pure level-up).
  Same-track weapon pickup also grants +1 level.
- **HUD** — clean left/centre/right layout: HP (red) + weapon left, WAVE top-centre, gold star icon
  + white SCORE right, pause top-right. No health bar. Font ×1.5. Always visible (intro & wave
  transitions included).
- **Audio** — original synthesized soundtrack + distinct SFX per event (fire/hit/explosion/pickup/
  power-up/boss/wave). No crackle/clipping.
- **Waves 1–7** — showcase progression (one new enemy type per wave, boss at 5, E at 6, F at 7),
  wave 8+ multi-squad. New formations: DIAMOND, CHECKERBOARD. Width-aware clamping so enemies
  stay on screen.
- **Performance** — batched background, cached GlyphLayout, reusable vectors, no per-frame asset
  loads, particle effects render batched (no hidden explosions).

See `README.md` for the human-facing feature/credits overview.
