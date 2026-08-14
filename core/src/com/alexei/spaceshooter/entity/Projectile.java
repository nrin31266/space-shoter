package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.utils.Utils;
import com.alexei.spaceshooter.weapon.Weapon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * A projectile fired from a Weapon.
 * Renders with smooth anti-aliased GL_BLEND and vibrant 4-layer neon glow.
 */
public class Projectile extends Visual {
    protected Weapon weapon;
    protected float damage;
    protected boolean isShipProjectile = false;
    protected float angularSpeed = 0;
    protected Visual target = null;
    protected boolean isHoming = false;

    private static final int CIRCLE_SEGMENTS = 32;

    public Projectile(float x, float y, float width, float height, float direction, float speed, Color color, float damage, boolean isShipProjectile) {
        super(x, y, width, height);
        super.setVelocity(direction, speed);
        super.setColor(color);
        this.damage = damage;
        this.isShipProjectile = isShipProjectile;
    }

    public Projectile(float x, float y, float width, float height, float direction, float speed, Color color, float damage) {
        super(x, y, width, height);
        super.setVelocity(direction, speed);
        super.setColor(color);
        this.damage = damage;
    }

    @Override
    public void update(float timeDelta) {
        if (isHoming && (target == null || SpaceShooter.isTargetDead(target))) {
            target = SpaceShooter.acquireTarget();
        }

        if (isHoming && target != null && angularSpeed != 0) {
            float directionToTarget = MathUtils.radiansToDegrees * MathUtils.atan2(target.getCenterY() - this.getCenterY(), target.getCenterX() - this.getCenterX());
            float normDir = Utils.normalizeAngle360(this.getDirection());
            float separatingAngle = Utils.normalizeAngle360(Utils.normalizeAngle360(directionToTarget) - normDir);
            float sign = separatingAngle > 180 ? -1 : 1;
            float min = Math.min(angularSpeed, separatingAngle);
            this.setDirection(normDir + Math.abs(min) * sign);
        }

        super.update(timeDelta);
    }

    /**
     * Renders projectiles with sprite textures when batch is active, or shape glow fallback.
     * Visual identities: player = blue beam, enemy light = red beam, enemy heavy/boss = plasma orb.
     */
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float cx = getCenterX();
        float cy = getCenterY();

        if (batch != null && batch.isDrawing()) {
            com.badlogic.gdx.graphics.g2d.TextureRegion region;
            if (isShipProjectile) {
                region = TextureRegistry.laserBlue;
            } else {
                // Heavy/boss projectiles use the plasma orb, light enemy shots use red laser
                boolean heavy = getWidth() > 26 || getHeight() > 26;
                region = heavy ? TextureRegistry.plasmaOrb : TextureRegistry.laserRed;
            }
            if (region != null) {
                float w = getWidth() * 1.5f;
                float h = getHeight() * 2.0f;
                batch.draw(region,
                        cx - w / 2f, cy - h / 2f,
                        w / 2f, h / 2f,
                        w, h,
                        1f, 1f,
                        getDirection() - 90f);
            }
        } else if (sr != null && sr.isDrawing()) {
            float r = Math.max(getWidth(), getHeight()) * 0.5f;
            Color col = getColor();
            sr.setColor(col.r, col.g, col.b, 0.25f);
            sr.circle(cx, cy, r + 5f, CIRCLE_SEGMENTS);
            sr.setColor(col.r, col.g, col.b, 0.80f);
            sr.circle(cx, cy, r + 2f, CIRCLE_SEGMENTS);
            sr.setColor(col);
            sr.circle(cx, cy, r, CIRCLE_SEGMENTS);
            sr.setColor(Color.WHITE);
            sr.circle(cx, cy, r * 0.40f, CIRCLE_SEGMENTS);
        }
    }

    public void doDamage(Unit target) {
        target.receiveDamage(this);
    }
    public boolean isShipProjectile() { return isShipProjectile; }
    public float getDamage() { return damage; }

    public float getAngularSpeed() { return angularSpeed; }
    public void setAngularSpeed(float angularSpeed) { this.angularSpeed = angularSpeed; }

    public Visual getTarget() { return target; }
    public void setTarget(Visual target) { this.target = target; }

    public boolean isHoming() { return isHoming; }
    public void setIsHoming(boolean isHoming) { this.isHoming = isHoming; }
}
