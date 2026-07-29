package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.weapon.WeaponSpreadShot;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipD — Tank.
 *
 * Large, slow entry. Hovers very high (80% screen height), fires 3-shot spread.
 * Orange color, 8 HP, drops 3 stars. Appears from wave 4.
 */
public class EnemyShipD extends Unit {
    private static final float UNIT_WIDTH   = 90;
    private static final float UNIT_HEIGHT  = 90;
    private static final float ENTER_SPEED  = 170;
    private static final float HOVER_SPEED  = 40;
    private static final Color UNIT_COLOR   = Color.valueOf("FF7700FF");
    private static final float MAX_LIFE     = 8f;
    private static final SoundName DEATH_SOUND = SoundName.Explode2;
    private static final int STARS_COUNT = 3;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY      = -1;
    private float screenWidth = 1080;
    private float hoverDir    = 1f;

    public EnemyShipD() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.10f, ENTER_SPEED * 0.10f);
        super.setVelocity(270, speed);
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);
        super.setStarCount(STARS_COUNT);
        super.addWeapon(new WeaponSpreadShot(this));
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.75f; // very high — tank stays near top
        hoverDir = MathUtils.randomBoolean() ? 1f : -1f;
    }

    public void setHoverY(float hoverY) {
        this.hoverY = hoverY;
    }

    private float stateTime = 0f;

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        stateTime += deltaTime / 1000f;

        switch (moveState) {
            case ENTERING:
                if (hoverY > 0 && getY() <= hoverY) {
                    moveState = MoveState.HOVERING;
                }
                break;

            case HOVERING:
                float driftSpeed = MathUtils.sin(stateTime * 1.5f) * (HOVER_SPEED * 0.4f) / com.alexei.spaceshooter.SpaceShooter.FPS;
                super.setVelocityVector(driftSpeed, 0);
                break;
        }
    }
}
