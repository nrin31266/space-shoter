package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Centralized registry of pre-loaded game textures.
 * Populated once by LoadingScreen after the AssetManager finishes loading.
 * Entities reference these static TextureRegions directly — zero allocations,
 * no repeated Gdx.files.internal() calls during gameplay.
 */
public class TextureRegistry {

    public static TextureRegion ship    = null;
    public static TextureRegion enemy1  = null; // EnemyShipA, EnemyShipB
    public static TextureRegion enemy2  = null; // EnemyShipC, EnemyShipD
    public static TextureRegion enemyC  = null; // EnemyShipC (small fast sniper)
    public static TextureRegion enemyD  = null; // EnemyShipD (heavy tank)
    public static TextureRegion enemyE  = null; // EnemyShipE (fast striker)
    public static TextureRegion enemyF  = null; // EnemyShipF (heavy dragoon)
    public static TextureRegion boss    = null; // EnemyBoss

    public static TextureRegion itemStar    = null;
    public static TextureRegion itemHp      = null;
    public static TextureRegion itemUpgrade = null;
    public static TextureRegion laserBlue   = null;
    public static TextureRegion laserRed    = null;
    public static TextureRegion plasmaOrb   = null;

    private static boolean initialized = false;

    /**
     * Called from LoadingScreen after assetManager.finishLoading().
     * Sets up all TextureRegion references from loaded textures.
     */
    public static void populate(AssetManager assetManager) {
        if (initialized) return;

        // Core sprites (always present — loaded in LoadingScreen.show())
        if (assetManager.isLoaded("ship.png")) {
            Texture t = assetManager.get("ship.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            ship = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy1.png")) {
            Texture t = assetManager.get("enemy1.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemy1 = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy2.png")) {
            Texture t = assetManager.get("enemy2.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemy2 = new TextureRegion(t);
        }

        // Extra enemy sprites (may or may not exist yet)
        tryLoad(assetManager, "enemy_c.png");
        tryLoad(assetManager, "enemy_d.png");
        tryLoad(assetManager, "enemy_e.png");
        tryLoad(assetManager, "enemy_f.png");
        tryLoad(assetManager, "enemy_boss.png");
        tryLoad(assetManager, "item_star.png");
        tryLoad(assetManager, "item_hp.png");
        tryLoad(assetManager, "item_upgrade.png");
        tryLoad(assetManager, "laser_blue.png");
        tryLoad(assetManager, "laser_red.png");
        tryLoad(assetManager, "plasma_orb.png");

        if (assetManager.isLoaded("enemy_c.png")) {
            Texture t = assetManager.get("enemy_c.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemyC = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy_d.png")) {
            Texture t = assetManager.get("enemy_d.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemyD = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy_e.png")) {
            Texture t = assetManager.get("enemy_e.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemyE = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy_f.png")) {
            Texture t = assetManager.get("enemy_f.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            enemyF = new TextureRegion(t);
        }
        if (assetManager.isLoaded("enemy_boss.png")) {
            Texture t = assetManager.get("enemy_boss.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            boss = new TextureRegion(t);
        }

        if (assetManager.isLoaded("item_star.png")) {
            Texture t = assetManager.get("item_star.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            itemStar = new TextureRegion(t);
        }
        if (assetManager.isLoaded("item_hp.png")) {
            Texture t = assetManager.get("item_hp.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            itemHp = new TextureRegion(t);
        }
        if (assetManager.isLoaded("item_upgrade.png")) {
            Texture t = assetManager.get("item_upgrade.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            itemUpgrade = new TextureRegion(t);
        }
        if (assetManager.isLoaded("laser_blue.png")) {
            Texture t = assetManager.get("laser_blue.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            laserBlue = new TextureRegion(t);
        }
        if (assetManager.isLoaded("laser_red.png")) {
            Texture t = assetManager.get("laser_red.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            laserRed = new TextureRegion(t);
        }
        if (assetManager.isLoaded("plasma_orb.png")) {
            Texture t = assetManager.get("plasma_orb.png", Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            plasmaOrb = new TextureRegion(t);
        }

        // Fallback assignments using existing assets when dedicated ones are absent
        if (enemyC  == null) enemyC  = enemy1;
        if (enemyD  == null) enemyD  = enemy2;
        if (enemyE  == null) enemyE  = enemy1;
        if (enemyF  == null) enemyF  = enemy2;
        if (boss    == null) boss    = enemy2;

        initialized = true;
    }

    /** Queue an optional asset for loading — ignores if file doesn't exist on disk. */
    private static void tryLoad(AssetManager assetManager, String path) {
        try {
            if (com.badlogic.gdx.Gdx.files.internal(path).exists()
                    && !assetManager.isLoaded(path)) {
                assetManager.load(path, Texture.class);
                assetManager.finishLoadingAsset(path);
            }
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.log("[TextureRegistry]", "Optional asset not found: " + path);
        }
    }

    public static void reset() {
        initialized = false;
    }
}
