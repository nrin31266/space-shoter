package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
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
     * Renders projectiles with anti-aliased neon glow edges and an energetic white core.
     */
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float cx = getCenterX();
        float cy = getCenterY();
        float r  = Math.max(getWidth(), getHeight()) * 0.5f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Color col = getColor();

        // 1. Soft Outer Neon Glow Aura
        sr.setColor(col.r, col.g, col.b, 0.25f);
        sr.circle(cx, cy, r + 5f, CIRCLE_SEGMENTS);

        // 2. Neon Ring Border
        sr.setColor(col.r, col.g, col.b, 0.80f);
        sr.circle(cx, cy, r + 2f, CIRCLE_SEGMENTS);

        // 3. Core Color
        sr.setColor(col);
        sr.circle(cx, cy, r, CIRCLE_SEGMENTS);

        // 4. White Center Core for laser energy pulse
        sr.setColor(Color.WHITE);
        sr.circle(cx, cy, r * 0.40f, CIRCLE_SEGMENTS);
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
