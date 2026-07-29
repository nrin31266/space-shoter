package com.alexei.spaceshooter.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 30/06/2015.
 */
public class ProjectileRocket extends Projectile {

    private float angularSpeed = 0.01f;

    public ProjectileRocket(float x, float y, float width, float height, float direction, float speed, Color color, float damage, boolean isShipProjectile) {
        super(x,y,width,height, direction, speed, color, damage, isShipProjectile);
    }

    public ProjectileRocket(float x, float y, float width, float height, float direction, float speed, Color color, float damage) {
        super(x, y, width, height, direction, speed, color, damage);
    }

    @Override
    public void update(float timeDelta) {
        super.update(timeDelta);

        // update homing direction towards target if set
        if (getTarget() != null) {
            float dir = MathUtils.radiansToDegrees * MathUtils.atan2(
                    getTarget().getCenterY() - this.getCenterY(),
                    getTarget().getCenterX() - this.getCenterX());
            // TODO: smooth rotation towards target using angularSpeed
        }
    }
}
