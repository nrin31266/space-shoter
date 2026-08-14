package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Centralized registry of pre-loaded game textures.
 *
 * Populated once by LoadingScreen after the AssetManager finishes loading.
 * Entities reference these static TextureRegions directly — zero allocations,
 * no repeated Gdx.files.internal() calls during gameplay.
 *
 * Every sprite is stored as its own PNG (see tools/gen_sprites.py), so each
 * TextureRegion covers the full texture — no atlas UV math, no UV bleeding.
 */
public class TextureRegistry {

    public static TextureRegion ship    = null; // Player ship
    public static TextureRegion enemy1  = null; // EnemyShipA (basic grunt)
    public static TextureRegion enemy2  = null; // EnemyShipB (elite gunner)
    public static TextureRegion enemyC  = null; // EnemyShipC (sniper)
    public static TextureRegion enemyD  = null; // EnemyShipD (tank)
    public static TextureRegion enemyE  = null; // EnemyShipE (striker)
    public static TextureRegion enemyF  = null; // EnemyShipF (dragoon)
    public static TextureRegion boss    = null; // EnemyBoss

    public static TextureRegion itemStar    = null; // currency (star)
    public static TextureRegion itemHp      = null; // health pickup
    public static TextureRegion itemEnergy  = null; // pure energy / power upgrade
    public static TextureRegion itemUpgrade = null; // plasma weapon upgrade (blue bolt)
    public static TextureRegion itemUpgradeExplosive = null; // explosive weapon upgrade (orange burst)
    public static TextureRegion itemUpgradeHoming    = null; // homing weapon upgrade (purple crosshair)

    // Projectiles
    public static TextureRegion laserBlue   = null; // player beam (laser track)
    public static TextureRegion shotOrb     = null; // player explosive orb (piercing track)
    public static TextureRegion shotDart    = null; // player homing dart (homing track)
    public static TextureRegion laserRed    = null; // enemy thin beam (sniper)
    public static TextureRegion plasmaOrb   = null; // heavy/boss projectile
    public static TextureRegion orbRed      = null; // enemy round shot
    public static TextureRegion orbGreen    = null; // enemy energy ball
    public static TextureRegion orbGold     = null; // enemy ring/spread shot
    public static TextureRegion orbPurple   = null; // enemy pulse shot
    public static TextureRegion orbPink     = null; // enemy sniper shot

    public static TextureRegion nebula      = null; // space background tile

    private static boolean initialized = false;

    /**
     * Called from LoadingScreen after assetManager.finishLoading().
     * Sets up all TextureRegion references from loaded textures.
     */
    public static void populate(AssetManager assetManager) {
        if (initialized) return;

        ship    = reg(assetManager, "ship.png");
        enemy1  = reg(assetManager, "enemy1.png");
        enemy2  = reg(assetManager, "enemy2.png");
        enemyC  = reg(assetManager, "enemy_c.png");
        enemyD  = reg(assetManager, "enemy_d.png");
        enemyE  = reg(assetManager, "enemy_e.png");
        enemyF  = reg(assetManager, "enemy_f.png");
        boss    = reg(assetManager, "enemy_boss.png");

        itemStar    = reg(assetManager, "item_star.png");
        itemHp      = reg(assetManager, "item_hp.png");
        itemEnergy  = reg(assetManager, "item_energy.png");
        itemUpgrade = reg(assetManager, "item_upgrade.png");
        itemUpgradeExplosive = reg(assetManager, "item_upgrade_explosive.png");
        itemUpgradeHoming    = reg(assetManager, "item_upgrade_homing.png");

        laserBlue   = reg(assetManager, "laser_blue.png");
        shotOrb     = reg(assetManager, "shot_orb.png");
        shotDart    = reg(assetManager, "shot_dart.png");
        laserRed    = reg(assetManager, "laser_red.png");
        plasmaOrb   = reg(assetManager, "plasma_orb.png");
        orbRed      = reg(assetManager, "orb_red.png");
        orbGreen    = reg(assetManager, "orb_green.png");
        orbGold     = reg(assetManager, "orb_gold.png");
        orbPurple   = reg(assetManager, "orb_purple.png");
        orbPink     = reg(assetManager, "orb_pink.png");

        nebula      = reg(assetManager, "nebula.png");

        // Defensive fallbacks: never let an entity draw without a region.
        if (enemyC == null) enemyC = enemy1;
        if (enemyD == null) enemyD = enemy2;
        if (enemyE == null) enemyE = enemy1;
        if (enemyF == null) enemyF = enemy2;
        if (boss   == null) boss   = enemy2;

        initialized = true;
    }

    /**
     * Fetch a texture (loaded or loaded-on-demand) as a full-texture region.
     * Configures wrap + filtering once. Returns null if the file is missing.
     */
    private static TextureRegion reg(AssetManager assetManager, String path) {
        try {
            if (!assetManager.isLoaded(path)) {
                if (!com.badlogic.gdx.Gdx.files.internal(path).exists()) {
                    return null;
                }
                assetManager.load(path, Texture.class);
                assetManager.finishLoadingAsset(path);
            }
            Texture t = assetManager.get(path, Texture.class);
            t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return new TextureRegion(t);
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.log("[TextureRegistry]", "Optional asset not found: " + path);
            return null;
        }
    }

    public static void reset() {
        initialized = false;
    }
}
