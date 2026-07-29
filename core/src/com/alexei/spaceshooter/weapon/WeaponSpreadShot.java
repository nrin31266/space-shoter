package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Spread shot weapon — fires 3 projectiles in a fan pattern downward.
 * Used by EnemyShipD (Tank).
 */
public class WeaponSpreadShot extends Weapon {
    private static final int WEAPON_FIRE_RATE = 1800; // ms between shots
    private static final float WEAPON_DAMAGE = 1f;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_SPEED = 500;
    private static final float PROJECTILE_SIZE = 14;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FF8800FF"); // orange

    // Spread angles relative to straight down (270 degrees)
    private static final float[] SPREAD_OFFSETS = { -22f, 0f, 22f };

    public WeaponSpreadShot(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        Projectile[] projectiles = new Projectile[SPREAD_OFFSETS.length];
        for (int i = 0; i < SPREAD_OFFSETS.length; i++) {
            float dir = 270f + SPREAD_OFFSETS[i];
            final Color col = PROJECTILE_COLOR.cpy();
            projectiles[i] = new Projectile(
                    unit.getCenterX(), unit.getCenterY(),
                    PROJECTILE_SIZE, PROJECTILE_SIZE,
                    dir, PROJECTILE_SPEED, col, getDamage()) {
                @Override
                public void render(ShapeRenderer sr, SpriteBatch batch) {
                    // Glowing orange orb
                    sr.setColor(col.r, col.g, col.b, 0.4f);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.85f);
                    sr.setColor(col);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.5f);
                    sr.setColor(Color.YELLOW);
                    sr.circle(getCenterX(), getCenterY(), getWidth() * 0.25f);
                }
            };
        }
        return projectiles;
    }
}
