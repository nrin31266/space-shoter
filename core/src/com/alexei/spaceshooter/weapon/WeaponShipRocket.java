package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by Alex on 30/06/2015.
 */
public class WeaponShipRocket extends Weapon {
    private static final int WEAPON_FIRE_RATE = 2000;
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.Rocket;
    private static final float PROJECTILE_WIDTH = 15;
    private static final float PROJECTILE_HEIGHT = 8;
    private static final float PROJECTILE_DIRECTION = 90;
    private static final float PROJECTILE_SPEED = 1000;
    private static final Color PROJECTILE_COLOR = Color.RED;
    private static final float PROJECTILE_ANGULAR_SPEED = 2f;

    public WeaponShipRocket(Unit unit) {
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
        projectiles[0].setAngularSpeed(PROJECTILE_ANGULAR_SPEED);
        projectiles[0].setIsHoming(true);

        return projectiles;
    }
}
