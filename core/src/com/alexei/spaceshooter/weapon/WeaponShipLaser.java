package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by Alex on 18/06/2015.
 *
 * Represents the main weapon on the player's ship.
 */
public class WeaponShipLaser extends Weapon {
    private static final int WEAPON_FIRE_RATE = 180;
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_WIDTH = 10;
    private static final float PROJECTILE_HEIGHT = 10;
    private static final float PROJECTILE_DIRECTION = 90;
    private static final float PROJECTILE_SPEED = 1200;
    private static final Color PROJECTILE_COLOR = Color.YELLOW;

    public WeaponShipLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    @Override
    public Projectile[] fire() throws NullPointerException {
        Unit unit = super.getUnit();
        if (unit == null) throw new NullPointerException("The weapon is not associated with any unit. Unit is null. Use Weapon.setUnit() to associate a Unit with this weapon.");

        Projectile[] projectiles = new Projectile[1];
        projectiles[0] = new Projectile(unit.getCenterX(), unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
        return projectiles;
    }
}
