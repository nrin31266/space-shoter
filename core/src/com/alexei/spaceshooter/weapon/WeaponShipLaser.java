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
 * Main Plasma Laser weapon (Track 0).
 * Fires thin blue laser beams in a FAN (up to 5 beams at Level 7).
 * ALL beams originate from a SINGLE point at the ship's muzzle and fan
 * outward — a classic single-origin spread.
 */
public class WeaponShipLaser extends Weapon {
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_SPEED = 1500f;
    private static final Color PROJECTILE_COLOR = Color.valueOf("00CCFFFF"); // Neon Cyan

    public WeaponShipLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(150);
        super.setDamage(0.7f);
        super.setWeaponSound(WEAPON_SOUND);
        super.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.laserBlue, false);
    }
    
    @Override
    public void update(float deltaTime, java.util.ArrayList<Projectile> projectiles) {
        Unit unit = getUnit();
        if (unit instanceof Ship) {
            int level = ((Ship) unit).getWeaponLevel();
            int targetFireRate;
            switch (level) {
                case 1: targetFireRate = 150; break;
                case 2: targetFireRate = 130; break;
                case 3: targetFireRate = 115; break;
                case 4: targetFireRate = 100; break;
                case 5: targetFireRate = 90; break;
                case 6: targetFireRate = 80; break;
                case 7:
                default: targetFireRate = 70; break;
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
        if (unit instanceof Ship) {
            level = ((Ship) unit).getWeaponLevel();
        }

        int count;
        float damage;
        float totalFan; // total angle (deg) swept by the whole fan

        // Max 5 beams (Level 7). All beams share ONE origin at the muzzle.
        // The fan is kept TIGHT so beams don't spread too far apart.
        switch (level) {
            case 1: count = 1; damage = 0.7f;  totalFan = 0f;  break;
            case 2: count = 2; damage = 0.65f; totalFan = 12f; break;
            case 3: count = 3; damage = 0.6f;  totalFan = 18f; break;
            case 4: count = 3; damage = 0.6f;  totalFan = 20f; break;
            case 5: count = 4; damage = 0.55f; totalFan = 24f; break;
            case 6: count = 4; damage = 0.55f; totalFan = 26f; break;
            case 7:
            default: count = 5; damage = 0.5f;  totalFan = 30f; break;
        }

        Projectile[] projectiles = new Projectile[count];
        // ONE shared origin point — the muzzle at the nose of the ship.
        float originX = unit.getCenterX();
        float originY = unit.getTop() + 12f;

        for (int i = 0; i < count; i++) {
            // Evenly spaced angles sweeping around straight-up (90°).
            float t = (count == 1) ? 0f : (i - (count - 1) / 2f) / ((count - 1) / 2f);
            float dir = 90f + t * (totalFan / 2f);
            Projectile p = new Projectile(
                    originX, originY,
                    6f, 20f,
                    dir, PROJECTILE_SPEED,
                    PROJECTILE_COLOR, damage, true);
            applyProjectileVisual(p);
            // A SINGLE beam always fires perfectly straight up (no inherited
            // ship drift). Only the multi-beam fan tracks the nose while dodging.
            if (count > 1) {
                inheritShipMotion(p, PROJECTILE_SPEED);
            }
            projectiles[i] = p;
        }

        // Muzzle flash at the ship nose — neon cyan (matches the beam).
        spawnMuzzleFlash(new com.badlogic.gdx.graphics.Color(0.4f, 0.85f, 1f, 1f));

        return projectiles;
    }
}
