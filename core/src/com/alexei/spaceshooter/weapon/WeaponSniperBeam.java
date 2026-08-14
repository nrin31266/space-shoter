package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.entity.Visual;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Sniper beam weapon — fires a single fast projectile aimed directly at the player.
 * Used by EnemyShipC.
 */
public class WeaponSniperBeam extends Weapon {
    private static final int WEAPON_FIRE_RATE = 3500; // ms — ShipC sniper, aimed at player
    private static final float WEAPON_DAMAGE = 1f;
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_WIDTH = 6;
    private static final float PROJECTILE_HEIGHT = 18;
    private static final float PROJECTILE_SPEED = 900;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FF4FF4FF"); // hot pink

    private Visual target;

    public WeaponSniperBeam(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    public void setTarget(Visual target) {
        this.target = target;
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        // Aim directly at target (player ship)
        float dir = 270f; // default: straight down
        if (target != null) {
            dir = MathUtils.radiansToDegrees * MathUtils.atan2(
                    target.getCenterY() - unit.getCenterY(),
                    target.getCenterX() - unit.getCenterX());
        }

        // Narrow beam projectile with custom render (elongated rectangle)
        Projectile p = new Projectile(
                unit.getCenterX(), unit.getCenterY(),
                PROJECTILE_WIDTH, PROJECTILE_HEIGHT,
                dir, PROJECTILE_SPEED,
                PROJECTILE_COLOR, getDamage());
        return new Projectile[]{p};
    }
}
