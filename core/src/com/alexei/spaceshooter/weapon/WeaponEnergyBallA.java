package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.entity.Visual;
import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 29/06/2015.
 */
public class WeaponEnergyBallA extends Weapon {
    private static final int WEAPON_FIRE_RATE = 8000; // ms — ShipB homing energy ball
    private static final float WEAPON_DAMAGE = 1;
    private static final SoundName WEAPON_SOUND = SoundName.Laser;
    private static final float PROJECTILE_WIDTH = 20;
    private static final float PROJECTILE_HEIGHT = 20;
    private static final float PROJECTILE_SPEED = 600;
    private static final Color PROJECTILE_COLOR = Color.valueOf("00CCFFFF");

    private Visual target;

    public WeaponEnergyBallA(Unit unit) {
        super.setUnit(unit);
        super.setFireRate(WEAPON_FIRE_RATE);
        super.setDamage(WEAPON_DAMAGE);
        super.setWeaponSound(WEAPON_SOUND);
    }

    public void setTarget(Visual target) {
        this.target = target;
    }

    @Override
    public Projectile[] fire() throws NullPointerException {
        Unit unit = super.getUnit();
        if (unit == null) throw new NullPointerException("The weapon is not associated with any unit. Unit is null. Use Weapon.setUnit() to associate a Unit with this weapon.");

        Projectile[] projectiles = new Projectile[1];

        // calculate projectile direction towards target (or straight down if no target)
        float dir = 270;
        if (target != null) {
            dir = MathUtils.radiansToDegrees * MathUtils.atan2(
                    target.getCenterY() - getUnit().getCenterY(),
                    target.getCenterX() - getUnit().getCenterX());
        }
        projectiles[0] = (new Projectile(unit.getCenterX(), unit.getCenterY(), PROJECTILE_WIDTH, PROJECTILE_HEIGHT, dir, PROJECTILE_SPEED, PROJECTILE_COLOR, getDamage()) {
            @Override
            public void render(ShapeRenderer sr, SpriteBatch batch) {
                sr.setColor(getColor());
                sr.circle(getCenterX(),getCenterY(),getWidth()/2);
                sr.setColor(Color.CYAN);
                sr.circle(getCenterX(),getCenterY(),getWidth()/2*2/3);
            }
        });
        return projectiles;
    }
}
