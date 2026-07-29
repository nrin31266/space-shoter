package com.alexei.spaceshooter.data.wave;

/**
 * DTO representing a single spawn instruction within a wave.
 * Contains NO gameplay logic — pure data.
 */
public class SpawnAction {
    /** Delay in seconds from wave start before this action executes */
    public float delay;

    /** Enemy type identifier (e.g., "EnemyShipA", "EnemyShipB") */
    public String enemyType;

    /** Spawn pattern (e.g., "LINE", "V_SHAPE", "RANDOM") */
    public String pattern;

    /** Number of enemies to spawn in this action */
    public int count;

    public SpawnAction() {
    }

    public SpawnAction(float delay, String enemyType, String pattern, int count) {
        this.delay = delay;
        this.enemyType = enemyType;
        this.pattern = pattern;
        this.count = count;
    }

    @Override
    public String toString() {
        return "SpawnAction{delay=" + delay + ", type=" + enemyType
                + ", pattern=" + pattern + ", count=" + count + "}";
    }
}
