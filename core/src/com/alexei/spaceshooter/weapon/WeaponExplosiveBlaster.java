package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Explosive Blaster — Track 1 player weapon ("đạn xuyên thấu" / piercing).
 * Fires large neon-orange plasma spheres that PIERCE through enemies (up to 3 hits).
 * Ultra-slight spread ("tẻ siêu nhẹ") and high damage.
 */
public class WeaponExplosiveBlaster extends Weapon {
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_SPEED = 1200f;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FF7A26FF"); // Neon Orange

    public WeaponExplosiveBlaster(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(280);
        super.setDamage(2.5f);
        super.setWeaponSound(WEAPON_SOUND);
        super.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.shotOrb, true);
    }

    @Override
    public void update(float deltaTime, java.util.ArrayList<Projectile> projectiles) {
        Unit unit = getUnit();
        if (unit instanceof Ship) {
            int level = ((Ship) unit).getWeaponLevel();
            int targetFireRate;
            switch (level) {
                case 1: targetFireRate = 280; break;
                case 2: targetFireRate = 240; break;
                case 3: targetFireRate = 220; break;
                case 4: targetFireRate = 190; break;
                case 5: targetFireRate = 170; break;
                case 6: targetFireRate = 150; break;
                case 7:
                default: targetFireRate = 130; break;
            }
            if (getFireRate() != targetFireRate) {
                setFireRate(targetFireRate);
            }
        }
        super.update(deltaTime, projectiles);
    }

    @Override
    public Projectile[] fire() {
        Unit unit = super.getUnit();
        if (unit == null) return new Projectile[0];

        int level = 1;
        if (unit instanceof Ship) {
            level = ((Ship) unit).getWeaponLevel();
        }

        int count;
        float damage;
        float spread;

        switch (level) {
            case 1: count = 1; damage = 2.5f; spread = 0f; break;
            case 2: count = 2; damage = 2.8f; spread = 4f; break; // ultra-slight spread
            case 3: count = 2; damage = 3.2f; spread = 4f; break;
            case 4: count = 3; damage = 3.5f; spread = 6f; break;
            case 5: count = 3; damage = 3.8f; spread = 6f; break;
            case 6: count = 3; damage = 4.0f; spread = 8f; break;
            case 7:
            default: count = 3; damage = 4.2f; spread = 8f; break;
        }

        Projectile[] projectiles = new Projectile[count];
        float baseDir = 90f;
        float size = 22f + (level * 1.0f); // larger plasma sphere size (22px - 29px)
        // Piercing track: orbs punch through up to 3 enemies before vanishing.
        int pierce = Math.min(3, 1 + level / 3);

        for (int i = 0; i < count; i++) {
            float dir = baseDir;
            if (count > 1) {
                dir = baseDir + (i - (count - 1) / 2f) * (spread / (count - 1));
            }
            Projectile p = new Projectile(
                    unit.getCenterX(), unit.getTop(),
                    size, size,
                    dir, PROJECTILE_SPEED,
                    PROJECTILE_COLOR, damage, true);
            p.setPierce(pierce);
            applyProjectileVisual(p);
            // Clamped ship-motion inheritance: orbs track the nose but never stall.
            inheritShipMotion(p, PROJECTILE_SPEED);
            projectiles[i] = p;
        }

        // Neon-orange muzzle flash matching the blaster orb.
        spawnMuzzleFlash(new com.badlogic.gdx.graphics.Color(1f, 0.55f, 0.2f, 1f));

        return projectiles;
    }
}
