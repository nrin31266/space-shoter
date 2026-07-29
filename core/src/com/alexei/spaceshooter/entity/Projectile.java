package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.Utils;
import com.alexei.spaceshooter.weapon.Weapon;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 17/06/2015.
 *
 * A class that represents a projectile that is fired from a Weapon.
 * A Projectile has damage, speed and direction of motion associated with it.
 * A projectile can do damage to a unit.
 */
public class Projectile extends Visual {
    protected Weapon weapon; // the associated weapon that fired this projectile
    protected float damage;
    protected boolean isShipProjectile = false;
    protected float angularSpeed = 0;
    protected Visual target = null; // a target that the projectile aims to hit, if not null and angular speed != 0
    protected boolean isHoming = false; // is this weapon a homing missile
    /***
     * When creating a projectile, it is important to pass the weapon's damage to the new projectile. This is
     * an important subtlety because the weapon's damage may increase from a power-up while the projectile is in flight,
     * hence, every projectile should keep track of the damage that was associated with the weapon at the time when it was fired.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param direction
     * @param speed
     * @param color
     * @param damage
     */
    public Projectile(float x, float y, float width, float height, float direction, float speed, Color color, float damage, boolean isShipProjectile) {
        super(x,y,width,height);
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

        if (isHoming && (target == null || SpaceShooter.isTargetDead(target))) { // acquire a new target if current target is dead or no target is selected
            target = SpaceShooter.acquireTarget();
        }

        // update direction based on angular speed and location of target
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
    /***
     * This method simply calls Unit.receiveDamage(this), passing this projectile as argument.
     * Unit.receiveDamage() handles the actual subtraction of life and performs any unit-related armor considerations, etc.
     * @param target The unit that should receive damage
     */
    public void doDamage(Unit target) {
        target.receiveDamage(this);
    }
    public boolean isShipProjectile() { return  isShipProjectile; }
    public float getDamage() { return damage; }

    public float getAngularSpeed() {
        return angularSpeed;
    }

    public void setAngularSpeed(float angularSpeed) {
        this.angularSpeed = angularSpeed;
    }

    public Visual getTarget() {
        return target;
    }

    public void setTarget(Visual target) {
        this.target = target;
    }

    public boolean isHoming() {
        return isHoming;
    }

    public void setIsHoming(boolean isHoming) {
        this.isHoming = isHoming;
    }
}
