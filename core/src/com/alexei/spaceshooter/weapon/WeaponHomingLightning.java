package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * Homing Lightning Darts — Track 2 player weapon.
 * Fires auto-seeking purple lightning darts with focused launch spread and balanced power.
 */
public class WeaponHomingLightning extends Weapon {
    private static final SoundName WEAPON_SOUND = SoundName.LaserShoot;
    private static final Color PROJECTILE_COLOR = Color.valueOf("CC00FF"); // Neon Purple

    public WeaponHomingLightning(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(240);
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
                case 1: targetFireRate = 240; break;
                case 2: targetFireRate = 190; break;
                case 3: targetFireRate = 190; break;
                case 4: targetFireRate = 160; break;
                case 5: targetFireRate = 160; break;
                case 6: targetFireRate = 130; break;
                case 7:
                default: targetFireRate = 100; break;
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
        float speed;

        switch (level) {
            case 1: count = 2; damage = 0.8f; speed = 750f; break;
            case 2: count = 2; damage = 0.9f; speed = 850f; break;
            case 3: count = 3; damage = 1.0f; speed = 950f; break;
            case 4: count = 3; damage = 1.1f; speed = 1050f; break;
            case 5: count = 4; damage = 1.2f; speed = 1150f; break;
            case 6: count = 4; damage = 1.3f; speed = 1250f; break;
            case 7:
            default: count = 5; damage = 1.4f; speed = 1350f; break;
        }

        List<Unit> enemies = SpaceShooter.getActiveEnemiesList();

        Projectile[] projectiles = new Projectile[count];
        float spreadAngle = 25f;
        float baseDir = 90f;

        for (int i = 0; i < count; i++) {
            float dir = baseDir + (i - (count - 1) / 2f) * (spreadAngle / Math.max(1, count - 1));
            
            Unit target = null;
            if (enemies != null && !enemies.isEmpty()) {
                target = enemies.get(i % enemies.size());
            }

            final Unit finalTarget = target;
            projectiles[i] = new Projectile(
                    unit.getCenterX(), unit.getTop(),
                    15f, 15f,
                    dir, speed,
                    PROJECTILE_COLOR, damage, true) {

                @Override
                public void update(float deltaTime) {
                    if (finalTarget != null && !finalTarget.isDead()) {
                        float targetAngle = MathUtils.radiansToDegrees * MathUtils.atan2(
                                finalTarget.getCenterY() - getCenterY(),
                                finalTarget.getCenterX() - getCenterX());
                        float currentDir = getDirection();
                        float diff = MathUtils.clamp(targetAngle - currentDir, -16f, 16f);
                        setDirection(currentDir + diff);
                    }
                    super.update(deltaTime);
                }

                @Override
                public void render(ShapeRenderer sr, SpriteBatch batch) {
                    Gdx.gl.glEnable(GL20.GL_BLEND);
                    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                    float cx = getCenterX();
                    float cy = getCenterY();
                    float r  = getWidth() * 0.5f;

                    // Sleek Purple Lightning Dart
                    sr.setColor(1f, 0.4f, 1f, 0.35f);
                    sr.circle(cx, cy, r * 1.4f, 24);
                    sr.setColor(PROJECTILE_COLOR);
                    sr.circle(cx, cy, r, 24);
                    sr.setColor(Color.WHITE);
                    sr.circle(cx, cy, r * 0.40f, 24);
                }
            };
        }
        return projectiles;
    }
}
