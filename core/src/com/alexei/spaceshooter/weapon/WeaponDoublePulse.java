package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;

/**
 * Double pulse weapon — fires 2 parallel vertical projectiles.
 * Used by EnemyShipE (Fast Striker).
 */
public class WeaponDoublePulse extends Weapon {
    private static final int WEAPON_FIRE_RATE = 2400; // ms between shots
    private static final float WEAPON_DAMAGE = 1f;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_WIDTH = 8f;
    private static final float PROJECTILE_HEIGHT = 16f;
    private static final float PROJECTILE_DIRECTION = 270f;
    private static final float PROJECTILE_SPEED = 450f;
    private static final Color PROJECTILE_COLOR = Color.valueOf("AA00FF"); // Purple

    public WeaponDoublePulse(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[2];
        float offsetX = 12f;

        // Left pulse
        projectiles[0] = new Projectile(
                unit.getCenterX() - offsetX, unit.getCenterY(),
                PROJECTILE_WIDTH, PROJECTILE_HEIGHT,
                PROJECTILE_DIRECTION, PROJECTILE_SPEED,
                PROJECTILE_COLOR, getDamage(), false);

        // Right pulse
        projectiles[1] = new Projectile(
                unit.getCenterX() + offsetX, unit.getCenterY(),
                PROJECTILE_WIDTH, PROJECTILE_HEIGHT,
                PROJECTILE_DIRECTION, PROJECTILE_SPEED,
                PROJECTILE_COLOR, getDamage(), false);

        return projectiles;
    }
}
