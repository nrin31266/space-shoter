package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.Starfield;
import com.alexei.spaceshooter.entity.Item;
import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.entity.Visual;
import com.alexei.spaceshooter.utils.ScoreTracker;

import java.util.ArrayList;

public class GameState {
    public static final float FPS = 60;
    public static final float GROUND_SCROLL_SPEED = 50;
    public static final int BUFFER_ZONE = 30;

    // star fields — kept calm & subordinate so gameplay stays readable.
    public Starfield starfield;
    public Starfield starfield2;
    public static final int STAR_COUNT = 42;
    public static final float STAR_SCROLL_ANGLE = 270;
    public static final float STAR_SCROLL_SPEED = 36;
    public static final float MIN_STAR_SIZE = 1;
    public static final float MAX_STAR_SIZE = 1.6f;

    public static final int STAR_COUNT_2 = 5;
    public static final float STAR_SCROLL_ANGLE_2 = 270;
    public static final float STAR_SCROLL_SPEED_2 = 60;
    public static final float MIN_STAR_SIZE_2 = 2.2f;
    public static final float MAX_STAR_SIZE_2 = 3.6f;

    // ship
    public Ship ship;

    // enemies
    public ArrayList<Unit> enemies = new ArrayList<>();

    // wave tracking (managed by WaveManager)
    public int currentWaveId = 1;

    // projectiles
    public ArrayList<Projectile> projectiles = new ArrayList<>();

    // items
    public ArrayList<Item> items = new ArrayList<>();

    // visual effects
    public ArrayList<Visual> visualEffects = new ArrayList<>();

    // score
    public ScoreTracker scoreTracker = new ScoreTracker();

    // game speed (slow-motion feature was removed in Phase 3 — constants below are deleted)
    // L1: gameSpeed, SLOW_MO_GAME_SPEED_LIMIT, SLOW_DOWN_PERIOD removed as dead code.

    public GameState() {
    }

    public void init(float screenWidth, float screenHeight) {
        starfield = new Starfield((int) screenWidth, (int) screenHeight,
                STAR_SCROLL_ANGLE, STAR_SCROLL_SPEED, STAR_COUNT, MIN_STAR_SIZE, MAX_STAR_SIZE);
        starfield2 = new Starfield((int) screenWidth, (int) screenHeight,
                STAR_SCROLL_ANGLE_2, STAR_SCROLL_SPEED_2, STAR_COUNT_2, MIN_STAR_SIZE_2, MAX_STAR_SIZE_2);
        // Far layer carries the nebula; it is tinted dark so gameplay stays readable.
        starfield.setNebula(com.alexei.spaceshooter.utils.TextureRegistry.nebula, 0.34f);
        ship = new Ship();
        scoreTracker = new ScoreTracker();
        currentWaveId = 1;
    }

    public void reset() {
        enemies.clear();
        projectiles.clear();
        items.clear();
        visualEffects.clear();
        scoreTracker.reset();
        currentWaveId = 1;
    }

    public void addVisualEffect(Visual effect) {
        visualEffects.add(effect);
    }

    public void addVisualEffects(ArrayList<Visual> effects) {
        visualEffects.addAll(effects);
    }
}
