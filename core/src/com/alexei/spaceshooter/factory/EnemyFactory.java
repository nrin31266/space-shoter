package com.alexei.spaceshooter.factory;

import com.alexei.spaceshooter.data.wave.SpawnAction;
import com.alexei.spaceshooter.entity.EnemyShipA;
import com.alexei.spaceshooter.entity.EnemyShipB;
import com.alexei.spaceshooter.entity.EnemyShipC;
import com.alexei.spaceshooter.entity.EnemyShipD;
import com.alexei.spaceshooter.entity.Unit;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory responsible for creating enemy units from SpawnAction data.
 *
 * Formations are now proportional to screen dimensions so they fill
 * the screen correctly on any device.
 */
public class EnemyFactory {

    /** Enemies spawn this many pixels above the visible top of screen */
    private static final float SPAWN_Y_OFFSET = 60f;

    /**
     * Create a list of enemy units from a SpawnAction.
     */
    public List<Unit> createFromAction(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();

        if (action == null || action.count <= 0) {
            return enemies;
        }

        Gdx.app.log("[EnemyFactory]",
                "Spawn " + action.enemyType + " count=" + action.count + " pattern=" + action.pattern);

        // N4: guard against null / missing pattern field in JSON
        if (action.pattern == null) {
            Gdx.app.error("[EnemyFactory]", "action.pattern is null — falling back to RANDOM");
            enemies.addAll(createRandomFormation(action, screenWidth, screenHeight));
            return enemies;
        }

        switch (action.pattern.toUpperCase()) {
            case "LINE":
                enemies.addAll(createLineFormation(action, screenWidth, screenHeight));
                break;
            case "V_SHAPE":
                enemies.addAll(createVShapeFormation(action, screenWidth, screenHeight));
                break;
            case "RANDOM":
                enemies.addAll(createRandomFormation(action, screenWidth, screenHeight));
                break;
            case "INTERLEAVED_ROWS":
                enemies.addAll(createInterleavedRowsFormation(action, screenWidth, screenHeight));
                break;
            case "BOSS":
                enemies.addAll(createBossFormation(action, screenWidth, screenHeight));
                break;
            case "GRID":
                enemies.addAll(createGridFormation(action, screenWidth, screenHeight));
                break;
            case "CHEVRON":
                enemies.addAll(createChevronFormation(action, screenWidth, screenHeight));
                break;
            default:
                Gdx.app.error("[EnemyFactory]", "Unknown pattern: " + action.pattern);
                enemies.addAll(createRandomFormation(action, screenWidth, screenHeight));
                break;
        }

        return enemies;
    }

    /**
     * Create a single enemy of the given type and initialise it with
     * screen dimensions for hover-position calculation.
     */
    public Unit createEnemy(String enemyType, float x, float y,
                            float screenWidth, float screenHeight) {
        Unit enemy;
        switch (enemyType) {
            case "EnemyShipB": {
                EnemyShipB b = new EnemyShipB();
                b.setScreenDimensions(screenWidth, screenHeight);
                enemy = b;
                break;
            }
            case "EnemyShipC": {
                EnemyShipC c = new EnemyShipC();
                c.setScreenDimensions(screenWidth, screenHeight);
                enemy = c;
                break;
            }
            case "EnemyShipD": {
                EnemyShipD d = new EnemyShipD();
                d.setScreenDimensions(screenWidth, screenHeight);
                enemy = d;
                break;
            }
            case "BOSS": {
                com.alexei.spaceshooter.entity.EnemyBoss boss = new com.alexei.spaceshooter.entity.EnemyBoss();
                boss.setScreenDimensions(screenWidth, screenHeight);
                enemy = boss;
                break;
            }
            case "EnemyShipA":
            default: {
                EnemyShipA a = new EnemyShipA();
                a.setScreenDimensions(screenWidth, screenHeight);
                enemy = a;
                break;
            }
        }
        enemy.setX(x);
        enemy.setY(y);
        return enemy;
    }

    // ─── Formation Methods ───────────────────────────────────────────

    /**
     * LINE — enemies spread evenly across the full screen width.
     *
     *   [E]  [E]  [E]  [E]  [E]
     */
    private List<Unit> createLineFormation(SpawnAction action,
                                           float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float spawnY = screenHeight + SPAWN_Y_OFFSET;

        // Divide screen width evenly: each slot = screenWidth / count
        float slotWidth = screenWidth / action.count;

        for (int i = 0; i < action.count; i++) {
            // Centre of each slot, with a small random jitter
            float cx = slotWidth * i + slotWidth / 2f;
            float jitter = MathUtils.random(-slotWidth * 0.12f, slotWidth * 0.12f);
            float x = MathUtils.clamp(cx + jitter, 10f, screenWidth - 70f);
            Unit u = createEnemy(action.enemyType, x, spawnY, screenWidth, screenHeight);
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }

        return enemies;
    }

    /**
     * V_SHAPE — enemies form a V pointing down toward the player.
     *
     *        [E]
     *     [E]    [E]
     *  [E]          [E]
     */
    private List<Unit> createVShapeFormation(SpawnAction action,
                                             float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float centerX = screenWidth / 2f;
        float apexY   = screenHeight + SPAWN_Y_OFFSET + 100f;

        // Horizontal spread scaled to screen — each row spreads 15% of screen width
        float spreadPerRow = screenWidth * 0.15f;
        // Vertical gap between V rows
        float rowGap = screenHeight * 0.06f;

        int remaining = action.count;
        int row = 0;

        while (remaining > 0) {
            float rowY = apexY - row * rowGap;

            if (row == 0) {
                // Apex: single enemy
                Unit u = createEnemy(action.enemyType, centerX - 30f, rowY,
                        screenWidth, screenHeight);
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
                remaining--;
            } else {
                float spread = spreadPerRow * row;
                float leftX  = MathUtils.clamp(centerX - spread, 10f, screenWidth - 70f);
                float rightX = MathUtils.clamp(centerX + spread, 10f, screenWidth - 70f);

                if (remaining >= 2) {
                    Unit u1 = createEnemy(action.enemyType, leftX, rowY, screenWidth, screenHeight);
                    applyHoverYPct(u1, action, screenHeight);
                    enemies.add(u1);
                    Unit u2 = createEnemy(action.enemyType, rightX, rowY, screenWidth, screenHeight);
                    applyHoverYPct(u2, action, screenHeight);
                    enemies.add(u2);
                    remaining -= 2;
                } else {
                    Unit u = createEnemy(action.enemyType, centerX - 30f, rowY,
                            screenWidth, screenHeight);
                    applyHoverYPct(u, action, screenHeight);
                    enemies.add(u);
                    remaining--;
                }
            }
            row++;
        }

        return enemies;
    }

    /**
     * RANDOM — enemies scattered across random X positions, staggered Y entry.
     */
    private List<Unit> createRandomFormation(SpawnAction action,
                                             float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();

        // Split screen into columns to avoid heavy overlap
        float colWidth = screenWidth / action.count;

        for (int i = 0; i < action.count; i++) {
            float colLeft = colWidth * i;
            float x = MathUtils.random(colLeft + 10f,
                    Math.min(colLeft + colWidth - 70f, screenWidth - 70f));
            float yOffset = MathUtils.random(0, screenHeight * 0.08f); // slight stagger
            Unit u = createEnemy(action.enemyType, x,
                    screenHeight + SPAWN_Y_OFFSET + yOffset, screenWidth, screenHeight);
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }

        return enemies;
    }
    
    /**
     * BOSS — spawns exactly action.count boss units spread evenly across the screen width.
     * Multiple bosses are staggered in Y so they don't enter at the exact same instant.
     *
     * BUG #1 fix: previously this always created exactly 1 boss regardless of action.count.
     * Now it loops action.count times and places each boss equidistant across screen width.
     */
    private List<Unit> createBossFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int bossCount = Math.max(1, action.count);
        float spawnY = screenHeight + SPAWN_Y_OFFSET;

        // Distribute bosses evenly: split screen into bossCount slots, center each boss in its slot.
        // Boss width is 250px — slot width must accommodate it without overlap.
        float slotWidth = screenWidth / bossCount;
        for (int i = 0; i < bossCount; i++) {
            float cx = slotWidth * i + slotWidth / 2f;
            // Clamp so the boss (250px wide) stays on screen
            float x = MathUtils.clamp(cx - 125f, 10f, screenWidth - 260f);
            // Stagger Y slightly so bosses don't arrive simultaneously (cosmetic, helps audio too)
            float yStagger = i * (screenHeight * 0.05f);
            Unit u = createEnemy("BOSS", x, spawnY + yStagger, screenWidth, screenHeight);
            // Note: EnemyBoss does not expose setHoverY — it uses a fixed 75% hover defined
            // in setScreenDimensions(). hoverYPct in BOSS actions is therefore ignored.
            enemies.add(u);
        }
        return enemies;
    }

    /**
     * INTERLEAVED_ROWS — 4 rows, even rows = primaryEnemyType, odd rows = secondaryEnemyType.
     *
     * BUG #2 fix: the secondaryEnemyType for odd rows is now read from action.secondaryEnemyType
     * (optional JSON field). If absent/null, falls back to "EnemyShipA" (legacy behaviour).
     *
     * N5 fix: previously, if count was not divisible by 4, the remainder was silently discarded
     * (e.g., count=25 → perRow=6 → 24 spawned). Now we distribute the remainder across the
     * first (count % rows) rows so that exactly count enemies are always spawned.
     */
    private List<Unit> createInterleavedRowsFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int rows = 4;
        int basePerRow = Math.max(1, action.count / rows);
        int remainder  = action.count % rows; // extra enemies distributed to first N rows

        // Determine the type for odd rows: use secondaryEnemyType if provided, else "EnemyShipA".
        String secondaryType = (action.secondaryEnemyType != null && !action.secondaryEnemyType.isEmpty())
                ? action.secondaryEnemyType
                : "EnemyShipA";

        for (int r = 0; r < rows; r++) {
            int perRow = basePerRow + (r < remainder ? 1 : 0); // distribute remainder to first rows
            float slotWidth = screenWidth / perRow;
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (r * screenHeight * 0.12f);
            String typeForThisRow = (r % 2 == 0) ? action.enemyType : secondaryType;
            float rowOffsetX = (r % 2 == 0) ? 0 : slotWidth * 0.5f;
            for (int c = 0; c < perRow; c++) {
                float cx = (slotWidth * c) + (slotWidth / 2f) + rowOffsetX;
                float x = MathUtils.clamp(cx - 30f, 10f, screenWidth - 70f);
                Unit u = createEnemy(typeForThisRow, x, spawnY, screenWidth, screenHeight);
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
            }
        }
        return enemies;
    }

    private List<Unit> createGridFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int cols = (int) Math.ceil(Math.sqrt(action.count));
        int rows = (int) Math.ceil((float) action.count / cols);
        float slotWidth = screenWidth / cols;
        int spawned = 0;
        for (int r = 0; r < rows; r++) {
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (r * screenHeight * 0.1f);
            for (int c = 0; c < cols; c++) {
                if (spawned >= action.count) break;
                float cx = (slotWidth * c) + (slotWidth / 2f);
                float x = MathUtils.clamp(cx - 30f, 10f, screenWidth - 70f);
                Unit u = createEnemy(action.enemyType, x, spawnY, screenWidth, screenHeight);
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
                spawned++;
            }
        }
        return enemies;
    }

    private List<Unit> createChevronFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int half = action.count / 2;
        float slotWidth = screenWidth / action.count;
        for (int i = 0; i < action.count; i++) {
            float cx = slotWidth * i + slotWidth / 2f;
            float x = MathUtils.clamp(cx - 30f, 10f, screenWidth - 70f);
            int distFromCenter = Math.abs(i - half);
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (distFromCenter * screenHeight * 0.08f);
            Unit u = createEnemy(action.enemyType, x, spawnY, screenWidth, screenHeight);
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }
        return enemies;
    }
    
    /**
     * Apply the JSON-specified hover Y position to an enemy unit.
     *
     * BUG #3 fix: added lower-bound clamp at 35% screen height.
     * Rationale: anything below 35% of screen height (measured from bottom) means the enemy
     * would hover very close to the player's starting area (~12% sh), giving the player almost
     * no reaction time. 35% keeps a comfortable buffer while still allowing low values like
     * the hoverYPct=0.45 used in waves 5/9/12/14 to work as intended.
     */
    private void applyHoverYPct(Unit enemy, SpawnAction action, float screenHeight) {
        float spawnOffset = enemy.getY() - (screenHeight + SPAWN_Y_OFFSET);
        float baseHover = screenHeight * 0.60f;
        
        if (action != null && action.hoverYPct != -1f) {
            baseHover = screenHeight * action.hoverYPct;
        } else {
            if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipA) baseHover = screenHeight * 0.60f;
            else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipB) baseHover = screenHeight * 0.65f;
            else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipC) baseHover = screenHeight * 0.55f;
            else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipD) baseHover = screenHeight * 0.75f;
        }
        
        float finalHover = baseHover + spawnOffset;
        
        // Safety clamp upper bound: never hover above visible top of screen.
        finalHover = Math.min(finalHover, screenHeight - 120f);
        // Safety clamp lower bound (BUG #3): never hover below 35% screen height.
        // This prevents enemies from hovering dangerously close to the player spawn area.
        finalHover = Math.max(finalHover, screenHeight * 0.35f);
        
        if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipA) ((com.alexei.spaceshooter.entity.EnemyShipA) enemy).setHoverY(finalHover);
        else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipB) ((com.alexei.spaceshooter.entity.EnemyShipB) enemy).setHoverY(finalHover);
        else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipC) ((com.alexei.spaceshooter.entity.EnemyShipC) enemy).setHoverY(finalHover);
        else if (enemy instanceof com.alexei.spaceshooter.entity.EnemyShipD) ((com.alexei.spaceshooter.entity.EnemyShipD) enemy).setHoverY(finalHover);
    }
}
