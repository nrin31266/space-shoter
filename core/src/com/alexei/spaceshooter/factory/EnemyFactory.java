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
            enemies.add(createEnemy(action.enemyType, x, spawnY, screenWidth, screenHeight));
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
                enemies.add(createEnemy(action.enemyType, centerX - 30f, rowY,
                        screenWidth, screenHeight));
                remaining--;
            } else {
                float spread = spreadPerRow * row;
                float leftX  = MathUtils.clamp(centerX - spread, 10f, screenWidth - 70f);
                float rightX = MathUtils.clamp(centerX + spread, 10f, screenWidth - 70f);

                if (remaining >= 2) {
                    enemies.add(createEnemy(action.enemyType, leftX, rowY, screenWidth, screenHeight));
                    enemies.add(createEnemy(action.enemyType, rightX, rowY, screenWidth, screenHeight));
                    remaining -= 2;
                } else {
                    enemies.add(createEnemy(action.enemyType, centerX - 30f, rowY,
                            screenWidth, screenHeight));
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
            enemies.add(createEnemy(action.enemyType, x,
                    screenHeight + SPAWN_Y_OFFSET + yOffset, screenWidth, screenHeight));
        }

        return enemies;
    }
}
