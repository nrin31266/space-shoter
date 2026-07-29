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
    private static final int WEAPON_FIRE_RATE = 200; // Default slow fire rate
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_WIDTH = 20; // Will be height when rotated 90 degrees
    private static final float PROJECTILE_HEIGHT = 5; // Slightly thicker
    private static final float PROJECTILE_DIRECTION = 90;
    private static final float PROJECTILE_SPEED = 1400; // Faster bullets
    private static final Color PROJECTILE_COLOR = Color.YELLOW; // Default yellow color

    public WeaponShipLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }
    
    @Override
    public void update(float deltaTime, java.util.ArrayList<Projectile> projectiles) {
        Unit unit = getUnit();
        if (unit instanceof com.alexei.spaceshooter.entity.Ship) {
            int level = ((com.alexei.spaceshooter.entity.Ship) unit).getWeaponLevel();
            int targetFireRate = 200;
            switch (level) {
                case 1: targetFireRate = 200; break; // 1 beam, slow
                case 2: targetFireRate = 120; break; // 1 beam, fast
                case 3: targetFireRate = 200; break; // 3 beams, slow
                case 4: targetFireRate = 120; break; // 3 beams, fast
                case 5:
                default: targetFireRate = 120; break; // 5 beams, fast
            }
            if (getFireRate() != targetFireRate) {
                setFireRate(targetFireRate);
            }
        }
        super.update(deltaTime, projectiles);
    }

    @Override
    public Projectile[] fire() throws NullPointerException {
        Unit unit = super.getUnit();
        if (unit == null) throw new NullPointerException("The weapon is not associated with any unit.");

        int level = 1;
        if (unit instanceof com.alexei.spaceshooter.entity.Ship) {
            level = ((com.alexei.spaceshooter.entity.Ship) unit).getWeaponLevel();
        }

        Projectile[] projectiles;
        if (level == 1 || level == 2) {
            projectiles = new Projectile[1];
            projectiles[0] = new Projectile(unit.getCenterX(), unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
        } else if (level == 3 || level == 4) {
            projectiles = new Projectile[3];
            projectiles[0] = new Projectile(unit.getCenterX(), unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[1] = new Projectile(unit.getCenterX() - 20, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION + 8, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[2] = new Projectile(unit.getCenterX() + 20, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION - 8, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
        } else {
            // Level 5 (max)
            projectiles = new Projectile[5];
            projectiles[0] = new Projectile(unit.getCenterX(), unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[1] = new Projectile(unit.getCenterX() - 20, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION + 8, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[2] = new Projectile(unit.getCenterX() + 20, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION - 8, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[3] = new Projectile(unit.getCenterX() - 40, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION + 16, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
            projectiles[4] = new Projectile(unit.getCenterX() + 40, unit.getTop(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, PROJECTILE_DIRECTION - 16, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage(), true);
        }
        return projectiles;
    }
}
