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
 * Features classic thin, sleek yellow/gold laser beams (up to 7 beams at Level 7) with low individual damage.
 */
public class WeaponShipLaser extends Weapon {
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot2;
    private static final float PROJECTILE_SPEED = 1500f;
    private static final Color PROJECTILE_COLOR = Color.valueOf("FFD700"); // Classic Gold-Yellow

    public WeaponShipLaser(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(150);
        super.setDamage(0.8f);
        super.setWeaponSound(WEAPON_SOUND);
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
                case 3: targetFireRate = 110; break;
                case 4: targetFireRate = 95; break;
                case 5: targetFireRate = 85; break;
                case 6: targetFireRate = 75; break;
                case 7:
                default: targetFireRate = 65; break;
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
        float totalSpread;

        switch (level) {
            case 1: count = 1; damage = 0.8f; totalSpread = 0f; break;
            case 2: count = 2; damage = 0.7f; totalSpread = 6f; break;
            case 3: count = 3; damage = 0.6f; totalSpread = 10f; break;
            case 4: count = 4; damage = 0.55f; totalSpread = 12f; break;
            case 5: count = 5; damage = 0.5f; totalSpread = 14f; break;
            case 6: count = 6; damage = 0.5f; totalSpread = 16f; break;
            case 7:
            default: count = 7; damage = 0.5f; totalSpread = 18f; break;
        }

        Projectile[] projectiles = new Projectile[count];
        float baseDir = 90f;

        for (int i = 0; i < count; i++) {
            float dir = baseDir;
            if (count > 1) {
                dir = baseDir + (i - (count - 1) / 2f) * (totalSpread / (count - 1));
            }
            float xOffset = (i - (count - 1) / 2f) * 6f;
            projectiles[i] = new Projectile(
                    unit.getCenterX() + xOffset, unit.getTop(),
                    6f, 24f,
                    dir, PROJECTILE_SPEED,
                    PROJECTILE_COLOR, damage, true) {
                @Override
                public void render(ShapeRenderer sr, SpriteBatch batch) {
                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                    float cx = getCenterX();
                    float cy = getCenterY();
                    float w  = getWidth();
                    float h  = getHeight();

                    // Thin Gold/Yellow Laser Beam
                    sr.setColor(1f, 0.85f, 0.1f, 0.40f);
                    sr.rect(cx - w, cy - h * 0.5f, w * 2f, h);
                    sr.setColor(PROJECTILE_COLOR);
                    sr.rect(cx - w * 0.5f, cy - h * 0.5f, w, h);
                    sr.setColor(Color.WHITE);
                    sr.rect(cx - w * 0.25f, cy - h * 0.4f, w * 0.5f, h * 0.8f);
                }
            };
        }
        return projectiles;
    }
}
