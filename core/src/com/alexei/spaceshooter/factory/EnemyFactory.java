package com.alexei.spaceshooter.factory;

import com.alexei.spaceshooter.data.wave.SpawnAction;
import com.alexei.spaceshooter.entity.EnemyBoss;
import com.alexei.spaceshooter.entity.EnemyShipA;
import com.alexei.spaceshooter.entity.EnemyShipB;
import com.alexei.spaceshooter.entity.EnemyShipC;
import com.alexei.spaceshooter.entity.EnemyShipD;
import com.alexei.spaceshooter.entity.EnemyShipE;
import com.alexei.spaceshooter.entity.EnemyShipF;
import com.alexei.spaceshooter.entity.Unit;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory responsible for creating enemy units from SpawnAction data.
 *
 * Formations are proportional to screen dimensions so they fill
 * the screen correctly on any device. Supports HP scaling and action density flags.
 */
public class EnemyFactory {

    /** Enemies spawn this many pixels above the visible top of screen */
    private static final float SPAWN_Y_OFFSET = 60f;

    /**
     * Create a list of enemy units from a SpawnAction (legacy overload without waveId).
     */
    public List<Unit> createFromAction(SpawnAction action, float screenWidth, float screenHeight) {
        return createFromAction(action, screenWidth, screenHeight, 1);
    }

    /**
     * Create a list of enemy units from a SpawnAction with waveId for HP scaling and density tagging.
     */
    public List<Unit> createFromAction(SpawnAction action, float screenWidth, float screenHeight, int waveId) {
        List<Unit> enemies = new ArrayList<>();

        if (action == null || action.count <= 0) {
            return enemies;
        }

        Gdx.app.log("[EnemyFactory]",
                "Spawn " + action.enemyType + " count=" + action.count + " pattern=" + action.pattern + " waveId=" + waveId);

        // N4: guard against null / missing pattern field in JSON
        if (action.pattern == null) {
            Gdx.app.error("[EnemyFactory]", "action.pattern is null — falling back to RANDOM");
            enemies.addAll(createRandomFormation(action, screenWidth, screenHeight));
        } else {
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
                case "DIAMOND":
                    enemies.addAll(createDiamondFormation(action, screenWidth, screenHeight));
                    break;
                case "CHECKERBOARD":
                    enemies.addAll(createCheckerboardFormation(action, screenWidth, screenHeight));
                    break;
                default:
                    Gdx.app.error("[EnemyFactory]", "Unknown pattern: " + action.pattern);
                    enemies.addAll(createRandomFormation(action, screenWidth, screenHeight));
                    break;
            }
        }

        // Post-processing: Apply HP scaling and action density flag
        boolean isDense = action.count > 20;
        for (Unit u : enemies) {
            if (isDense) {
                u.setDenseAction(true);
            }
            float scaledHp = calculateScaledHP(u, waveId);
            u.setMaxLife(scaledHp);
            u.setLife(scaledHp);
        }

        return enemies;
    }

    /**
     * HP Scaling formula (Section 5 Exponential Scaling):
     * HP_wave = HP_base * (1 + growthRate)^(waveId - firstAppearWave)
     */
    private float calculateScaledHP(Unit enemy, int waveId) {
        float hpBase = enemy.getMaxLife();
        int firstWave = 1;
        float growthRate = 0.04f;

        if (enemy instanceof EnemyShipA) {
            firstWave = 1; growthRate = 0.04f;
        } else if (enemy instanceof EnemyShipB) {
            firstWave = 2; growthRate = 0.06f;
        } else if (enemy instanceof EnemyShipC) {
            firstWave = 3; growthRate = 0.06f;
        } else if (enemy instanceof EnemyShipD) {
            firstWave = 4; growthRate = 0.08f;
        } else if (enemy instanceof EnemyShipE) {
            firstWave = 16; growthRate = 0.06f;
        } else if (enemy instanceof EnemyShipF) {
            firstWave = 16; growthRate = 0.08f;
        } else if (enemy instanceof EnemyBoss) {
            firstWave = 5; growthRate = 0.05f;
        }

        int waveDiff = Math.max(0, waveId - firstWave);
        return hpBase * (float) Math.pow(1.0f + growthRate, waveDiff);
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
            case "EnemyShipE": {
                EnemyShipE e = new EnemyShipE();
                e.setScreenDimensions(screenWidth, screenHeight);
                enemy = e;
                break;
            }
            case "EnemyShipF": {
                EnemyShipF f = new EnemyShipF();
                f.setScreenDimensions(screenWidth, screenHeight);
                enemy = f;
                break;
            }
            case "BOSS": {
                EnemyBoss boss = new EnemyBoss();
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

    /** Width-aware horizontal clamp so an enemy never sticks half out of the screen edge. */
    private float clampX(Unit u, float cx, float screenWidth) {
        float margin = u.getWidth() * 0.5f + 8f;
        return MathUtils.clamp(cx, margin, screenWidth - margin);
    }

    private List<Unit> createLineFormation(SpawnAction action,
                                           float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float spawnY = screenHeight + SPAWN_Y_OFFSET;
        float slotWidth = screenWidth / action.count;

        for (int i = 0; i < action.count; i++) {
            float cx = slotWidth * i + slotWidth / 2f;
            float jitter = MathUtils.random(-slotWidth * 0.10f, slotWidth * 0.10f);
            Unit u = createEnemy(action.enemyType, cx + jitter, spawnY, screenWidth, screenHeight);
            u.setX(clampX(u, u.getX(), screenWidth));
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }

        return enemies;
    }

    private List<Unit> createVShapeFormation(SpawnAction action,
                                             float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float centerX = screenWidth / 2f;
        float apexY   = screenHeight + SPAWN_Y_OFFSET + 100f;
        float spreadPerRow = screenWidth * 0.15f;
        float rowGap = screenHeight * 0.06f;

        int remaining = action.count;
        int row = 0;

        while (remaining > 0) {
            float rowY = apexY - row * rowGap;

            if (row == 0) {
                Unit u = createEnemy(action.enemyType, centerX - 30f, rowY,
                        screenWidth, screenHeight);
                u.setX(clampX(u, u.getX(), screenWidth));
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
                remaining--;
            } else {
                float spread = spreadPerRow * row;
                float leftX  = centerX - spread;
                float rightX = centerX + spread;

                if (remaining >= 2) {
                    Unit u1 = createEnemy(action.enemyType, leftX, rowY, screenWidth, screenHeight);
                    u1.setX(clampX(u1, u1.getX(), screenWidth));
                    applyHoverYPct(u1, action, screenHeight);
                    enemies.add(u1);
                    Unit u2 = createEnemy(action.enemyType, rightX, rowY, screenWidth, screenHeight);
                    u2.setX(clampX(u2, u2.getX(), screenWidth));
                    applyHoverYPct(u2, action, screenHeight);
                    enemies.add(u2);
                    remaining -= 2;
                } else {
                    Unit u = createEnemy(action.enemyType, centerX - 30f, rowY,
                            screenWidth, screenHeight);
                    u.setX(clampX(u, u.getX(), screenWidth));
                    applyHoverYPct(u, action, screenHeight);
                    enemies.add(u);
                    remaining--;
                }
            }
            row++;
        }

        return enemies;
    }

    private List<Unit> createRandomFormation(SpawnAction action,
                                             float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float colWidth = screenWidth / action.count;

        for (int i = 0; i < action.count; i++) {
            float colLeft = colWidth * i;
            float x = MathUtils.random(colLeft + 10f,
                    Math.min(colLeft + colWidth - 10f, screenWidth - 10f));
            float yOffset = MathUtils.random(0, screenHeight * 0.08f);
            Unit u = createEnemy(action.enemyType, x,
                    screenHeight + SPAWN_Y_OFFSET + yOffset, screenWidth, screenHeight);
            u.setX(clampX(u, u.getX(), screenWidth));
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }

        return enemies;
    }

    private List<Unit> createBossFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int bossCount = Math.max(1, action.count);
        float spawnY = screenHeight + SPAWN_Y_OFFSET;
        float slotWidth = screenWidth / bossCount;

        for (int i = 0; i < bossCount; i++) {
            float cx = slotWidth * i + slotWidth / 2f;
            Unit u = createEnemy("BOSS", cx, spawnY, screenWidth, screenHeight);
            u.setX(clampX(u, u.getX(), screenWidth));
            float yStagger = i * (screenHeight * 0.05f);
            u.setY(u.getY() + yStagger);
            enemies.add(u);
        }
        return enemies;
    }

    private List<Unit> createInterleavedRowsFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        int rows = 4;
        int basePerRow = Math.max(1, action.count / rows);
        int remainder  = action.count % rows;

        String secondaryType = (action.secondaryEnemyType != null && !action.secondaryEnemyType.isEmpty())
                ? action.secondaryEnemyType
                : "EnemyShipA";

        for (int r = 0; r < rows; r++) {
            int perRow = basePerRow + (r < remainder ? 1 : 0);
            float slotWidth = screenWidth / perRow;
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (r * screenHeight * 0.12f);
            String typeForThisRow = (r % 2 == 0) ? action.enemyType : secondaryType;
            float rowOffsetX = (r % 2 == 0) ? 0 : slotWidth * 0.5f;
            for (int c = 0; c < perRow; c++) {
                float cx = (slotWidth * c) + (slotWidth / 2f) + rowOffsetX;
                Unit u = createEnemy(typeForThisRow, cx, spawnY, screenWidth, screenHeight);
                u.setX(clampX(u, u.getX(), screenWidth));
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
                Unit u = createEnemy(action.enemyType, cx, spawnY, screenWidth, screenHeight);
                u.setX(clampX(u, u.getX(), screenWidth));
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
            int distFromCenter = Math.abs(i - half);
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (distFromCenter * screenHeight * 0.08f);
            Unit u = createEnemy(action.enemyType, cx, spawnY, screenWidth, screenHeight);
            u.setX(clampX(u, u.getX(), screenWidth));
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
        }
        return enemies;
    }

    /** Diamond / X layout: enemies spread symmetrically around the centre. */
    private List<Unit> createDiamondFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        float centerX = screenWidth / 2f;
        float midY = screenHeight + SPAWN_Y_OFFSET;
        // Rows: 1, 2, 2, 1 (tight, centred)
        float[] rowSpreads = { 0f, screenWidth * 0.18f, screenWidth * 0.18f, 0f };
        int[]   rowCounts  = { 1, 2, 2, 1 };
        int spawned = 0;
        for (int r = 0; r < rowCounts.length; r++) {
            if (spawned >= action.count) break;
            float rowY = midY + r * screenHeight * 0.07f;
            int perRow = Math.min(rowCounts[r], action.count - spawned);
            for (int c = 0; c < perRow; c++) {
                float x = centerX + (perRow == 1 ? 0f : (c == 0 ? -rowSpreads[r] : rowSpreads[r]));
                Unit u = createEnemy(action.enemyType, x, rowY, screenWidth, screenHeight);
                u.setX(clampX(u, u.getX(), screenWidth));
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
                spawned++;
            }
        }
        // Any leftovers fill the top row of the diamond
        while (spawned < action.count) {
            float x = centerX + MathUtils.random(-screenWidth * 0.1f, screenWidth * 0.1f);
            Unit u = createEnemy(action.enemyType, x, midY, screenWidth, screenHeight);
            u.setX(clampX(u, u.getX(), screenWidth));
            applyHoverYPct(u, action, screenHeight);
            enemies.add(u);
            spawned++;
        }
        return enemies;
    }

    /** Checkerboard: two interleaved enemy types in a compact grid (xen kẽ). */
    private List<Unit> createCheckerboardFormation(SpawnAction action, float screenWidth, float screenHeight) {
        List<Unit> enemies = new ArrayList<>();
        String secondaryType = (action.secondaryEnemyType != null && !action.secondaryEnemyType.isEmpty())
                ? action.secondaryEnemyType
                : "EnemyShipA";
        int cols = (int) Math.ceil(Math.sqrt(action.count));
        int rows = (int) Math.ceil((float) action.count / cols);
        float slotWidth = screenWidth / cols;
        int spawned = 0;
        for (int r = 0; r < rows; r++) {
            float spawnY = screenHeight + SPAWN_Y_OFFSET + (r * screenHeight * 0.09f);
            for (int c = 0; c < cols; c++) {
                if (spawned >= action.count) break;
                float cx = (slotWidth * c) + (slotWidth / 2f);
                boolean usePrimary = ((r + c) % 2 == 0);
                String type = usePrimary ? action.enemyType : secondaryType;
                Unit u = createEnemy(type, cx, spawnY, screenWidth, screenHeight);
                u.setX(clampX(u, u.getX(), screenWidth));
                applyHoverYPct(u, action, screenHeight);
                enemies.add(u);
                spawned++;
            }
        }
        return enemies;
    }

    private void applyHoverYPct(Unit enemy, SpawnAction action, float screenHeight) {
        float spawnOffset = enemy.getY() - (screenHeight + SPAWN_Y_OFFSET);
        float baseHover = screenHeight * 0.60f;

        if (action != null && action.hoverYPct != -1f) {
            baseHover = screenHeight * action.hoverYPct;
        } else {
            if (enemy instanceof EnemyShipA) baseHover = screenHeight * 0.60f;
            else if (enemy instanceof EnemyShipB) baseHover = screenHeight * 0.65f;
            else if (enemy instanceof EnemyShipC) baseHover = screenHeight * 0.55f;
            else if (enemy instanceof EnemyShipD) baseHover = screenHeight * 0.75f;
            else if (enemy instanceof EnemyShipE) baseHover = screenHeight * 0.50f;
            else if (enemy instanceof EnemyShipF) baseHover = screenHeight * 0.60f;
        }

        float finalHover = baseHover + spawnOffset;
        finalHover = Math.min(finalHover, screenHeight - 120f);
        finalHover = Math.max(finalHover, screenHeight * 0.35f);

        if (enemy instanceof EnemyShipA) ((EnemyShipA) enemy).setHoverY(finalHover);
        else if (enemy instanceof EnemyShipB) ((EnemyShipB) enemy).setHoverY(finalHover);
        else if (enemy instanceof EnemyShipC) ((EnemyShipC) enemy).setHoverY(finalHover);
        else if (enemy instanceof EnemyShipD) ((EnemyShipD) enemy).setHoverY(finalHover);
        else if (enemy instanceof EnemyShipE) ((EnemyShipE) enemy).setHoverY(finalHover);
        else if (enemy instanceof EnemyShipF) ((EnemyShipF) enemy).setHoverY(finalHover);
    }
}
