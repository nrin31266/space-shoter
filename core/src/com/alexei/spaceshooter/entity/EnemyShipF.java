package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.weapon.WeaponRingBurst;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipF — Heavy Dragoon / Ring Wave Attacker.
 * Heavy armored sub-boss enemy (12 HP base), enters slowly (150 px/s), fires 4-way diagonal ring bursts.
 * Appears starting Wave 16.
 */
public class EnemyShipF extends Unit {
    private static final float UNIT_WIDTH  = 85;
    private static final float UNIT_HEIGHT = 85;
    private static final float ENTER_SPEED = 150;
    private static final float HOVER_SPEED = 35;
    private static final Color UNIT_COLOR  = Color.valueOf("FFD700FF"); // Heavy Gold
    private static final float MAX_LIFE    = 12.0f;
    private static final SoundName DEATH_SOUND = SoundName.Explode4;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY      = -1;
    private float screenWidth = 1080;

    public EnemyShipF() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.08f, ENTER_SPEED * 0.08f);
        super.setVelocity(270, speed);
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);

        super.addWeapon(new WeaponRingBurst(this));
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.60f;
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
                float driftSpeed = MathUtils.sin(stateTime * 1.0f) * (HOVER_SPEED * 0.3f) / com.alexei.spaceshooter.SpaceShooter.FPS;
                super.setVelocityVector(driftSpeed, 0);
                break;
        }
    }
}
