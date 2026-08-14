package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;

public class WeaponEnemyLaser extends Weapon {
    private static final int WEAPON_FIRE_RATE = 8000; // ms — ShipA fires slowly (straight down, not aimed)
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_WIDTH = 16; 
    private static final float PROJECTILE_HEIGHT = 16; 
    private static final float PROJECTILE_DIRECTION = 270;
    private static final float PROJECTILE_SPEED = 400;
    private static final Color PROJECTILE_COLOR = Color.valueOf("ff5555");

    public WeaponEnemyLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
        // Round red orb — readable, distinct from beams.
        super.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.orbRed, true);
    }

    @Override
    public Projectile[] fire() throws NullPointerException {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[1];
        Projectile p = new Projectile(unit.getCenterX(), unit.getCenterY(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), false);
        applyProjectileVisual(p);
        projectiles[0] = p;
        return projectiles;
    }
}
