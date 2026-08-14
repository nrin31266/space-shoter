package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Spread shot weapon — fires 3 projectiles in a fan pattern downward.
 * Used by EnemyShipD (Tank) and the Boss. The Boss overrides the projectile
 * visual to a plasma orb via setProjectileVisual().
 */
public class WeaponSpreadShot extends Weapon {
    private static final int WEAPON_FIRE_RATE = 3000; // ms — ShipD tank & Boss spread shot
    private static final float WEAPON_DAMAGE = 1f;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_SPEED = 500;
    private static final float PROJECTILE_SIZE = 18;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FFB020FF"); // orange-gold

    // Spread angles relative to straight down (270 degrees)
    private static final float[] SPREAD_OFFSETS = { -22f, 0f, 22f };

    public WeaponSpreadShot(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
        // Default tank visual: round gold orb.
        super.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.orbGold, true);
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[SPREAD_OFFSETS.length];
        for (int i = 0; i < SPREAD_OFFSETS.length; i++) {
            float dir = 270f + SPREAD_OFFSETS[i];
            final Color col = PROJECTILE_COLOR.cpy();
            Projectile p = new Projectile(
                    unit.getCenterX(), unit.getCenterY(),
                    PROJECTILE_SIZE, PROJECTILE_SIZE,
                    dir, PROJECTILE_SPEED, col, getDamage());
            applyProjectileVisual(p);
            projectiles[i] = p;
        }
        return projectiles;
    }
}
