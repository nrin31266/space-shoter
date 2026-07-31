package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Ring burst weapon — fires 4 orb projectiles radiating in 4 diagonal directions (45°, 135°, 225°, 315°).
 * Used by EnemyShipF (Heavy Dragoon).
 */
public class WeaponRingBurst extends Weapon {
    private static final int WEAPON_FIRE_RATE = 4000; // ms between shots
    private static final float WEAPON_DAMAGE = 1f;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_SPEED = 350f;
    private static final float PROJECTILE_SIZE = 14f;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FFD700"); // Gold

    private static final float[] BURST_ANGLES = { 45f, 135f, 225f, 315f };

    public WeaponRingBurst(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[BURST_ANGLES.length];
        for (int i = 0; i < BURST_ANGLES.length; i++) {
            float dir = BURST_ANGLES[i];
            final Color col = PROJECTILE_COLOR.cpy();
            projectiles[i] = new Projectile(
                    unit.getCenterX(), unit.getCenterY(),
                    PROJECTILE_SIZE, PROJECTILE_SIZE,
                    dir, PROJECTILE_SPEED, col, getDamage()) {
                @Override
                public void render(ShapeRenderer sr, SpriteBatch batch) {
                    sr.setColor(col.r, col.g, col.b, 0.4f);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.85f);
                    sr.setColor(col);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.5f);
                    sr.setColor(Color.WHITE);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.25f);
                }
            };
        }
        return projectiles;
    }
}
