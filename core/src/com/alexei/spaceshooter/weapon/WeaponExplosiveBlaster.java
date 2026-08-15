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
        super.setFireRate(300);
        super.setDamage(1.8f);
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
                case 1: targetFireRate = 300; break;
                case 2: targetFireRate = 260; break;
                case 3: targetFireRate = 240; break;
                case 4: targetFireRate = 215; break;
                case 5: targetFireRate = 195; break;
                case 6: targetFireRate = 180; break;
                case 7:
                default: targetFireRate = 165; break;
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

        // Max 3 orbs — clear 1→2→2→3→3→3→3 progression.
        switch (level) {
            case 1: count = 1; damage = 2.0f; spread = 0f; break;
            case 2: count = 2; damage = 1.8f; spread = 5f; break;
            case 3: count = 2; damage = 2.0f; spread = 5f; break;
            case 4: count = 3; damage = 1.8f; spread = 7f; break;
            case 5: count = 3; damage = 2.0f; spread = 7f; break;
            case 6: count = 3; damage = 2.2f; spread = 9f; break;
            case 7:
            default: count = 3; damage = 2.4f; spread = 9f; break;
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
                    unit.getCenterX(), unit.getTop() + 10f,
                    size, size,
                    dir, PROJECTILE_SPEED,
                    PROJECTILE_COLOR, damage, true);
            p.setPierce(pierce);
            applyProjectileVisual(p);
            // Single orb always flies straight; multi-orbs track the nose while dodging.
            if (count > 1) {
                inheritShipMotion(p, PROJECTILE_SPEED);
            }
            projectiles[i] = p;
        }

        // Neon-orange muzzle flash matching the blaster orb.
        spawnMuzzleFlash(new com.badlogic.gdx.graphics.Color(1f, 0.55f, 0.2f, 1f));

        return projectiles;
    }
}
