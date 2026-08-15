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