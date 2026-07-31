package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;

public class WeaponEnemyLaser extends Weapon {
    private static final int WEAPON_FIRE_RATE = 8000; // ms — ShipA fires slowly (straight down, not aimed)
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_WIDTH = 12; 
    private static final float PROJECTILE_HEIGHT = 12; 
    private static final float PROJECTILE_DIRECTION = 270;
    private static final float PROJECTILE_SPEED = 400;
    private static final Color PROJECTILE_COLOR = Color.valueOf("ff5555");

    public WeaponEnemyLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    @Override
    public Projectile[] fire() throws NullPointerException {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[1];
        projectiles[0] = new Projectile(unit.getCenterX(), unit.getCenterY(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), false);
        return projectiles;
    }
}
