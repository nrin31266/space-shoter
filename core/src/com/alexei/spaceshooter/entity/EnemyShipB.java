package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.weapon.WeaponEnergyBallA;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 29/06/2015.
 */
public class EnemyShipB extends Unit {
    private static final float UNIT_POSITION_X = 0;
    private static final float UNIT_POSITION_Y = 0;
    private static final float UNIT_WIDTH = 70;
    private static final float UNIT_HEIGHT = 70;
    private static final float UNIT_DIRECTION = 270;
    private static final float UNIT_SPEED = SpaceShooter.GROUND_SCROLL_SPEED * 2f;
    private static final Color UNIT_COLOR = Color.CHARTREUSE;
    private static final float MAX_LIFE = 5f;
//    private static final SoundName DEATH_SOUND = SoundName.Explode8;
    private static final SoundName DEATH_SOUND = SoundName.Explode2;
    private static final int STARS_COUNT = 2;

    public EnemyShipB() {
        super(UNIT_POSITION_X, UNIT_POSITION_Y, UNIT_WIDTH, UNIT_HEIGHT);
        super.setVelocity(UNIT_DIRECTION, UNIT_SPEED + MathUtils.random(-UNIT_SPEED * 0.15f, UNIT_SPEED * 0.15f));
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);

        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);

        super.setStarCount(STARS_COUNT);

        // add weapons
        super.addWeapon(new WeaponEnergyBallA(this));

        // no weapons
    }

//    @Override
//    public void render(ShapeRenderer sr) {
//        super.render(sr);
//
//        // render damage points
//        for (DamagePoint p : super.getDamagePoints()) {
//            sr.setColor(Color.BLACK.cpy());
//            sr.getColor().a = 0.25f;
//
//            sr.rect(position.x + p.pos.x, position.y + p.pos.y, p.dim.x, p.dim.y);
//
//        }
//    }
//
//    @Override
//    public void update(float deltaTime) {
//        super.update(deltaTime);
//
//        // update all weapons
//        for(Weapon w : super.getWeapons()) {
//            w.update(deltaTime);
//        }
//    }
//
//
//    public void doDamage(Unit toUnit) {
//
//    }
//
//    /**
//     * Typically a call is first made to the Projectile.doDamage(enemyUnit) method,
//     * which simply forwards the call to this method.
//     * @param projectile The projectile that damage is being received from.
//     */
//    public void receiveDamage(Projectile projectile) {
//        super.receiveDamage(projectile);
//
//        int spots = MathUtils.random(1,5);
//        for (int i=0;i<spots;i++) {
//            int dx = MathUtils.random(0, 15);
//            int dy = MathUtils.random(0, 5);
//            int xsize = MathUtils.random(10, 20);
//            int ysize = MathUtils.random(10, 20);
//            super.getDamagePoints().add(new Unit.DamagePoint(new Vector2(projectile.getX() - position.x + dx, dy), new Vector2(xsize,ysize)));
//        }
//    }
//
//    /***
//     * Typically this is called when the player's ship collides with an enemy unit
//     * @param fromUnit The enemy unit which collided with the player's ship
//     */
//    public void receiveDamage(Unit fromUnit) {
//        super.receiveDamage(fromUnit);
//    }
}
