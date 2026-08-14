package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.weapon.WeaponDoublePulse;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipE — Fast Striker / Pulse Attacker.
 * High mobility, enters fast (420 px/s), hovers higher up, fires double parallel pulses.
 * Appears starting Wave 16.
 */
public class EnemyShipE extends Unit {
    private static final float UNIT_WIDTH  = 100;
    private static final float UNIT_HEIGHT = 100;
    private static final float ENTER_SPEED = 420;
    private static final float HOVER_SPEED = 85;
    private static final Color UNIT_COLOR  = Color.valueOf("AA00FFFF"); // Purple
    private static final float MAX_LIFE    = 3.0f;
    private static final SoundName DEATH_SOUND = SoundName.Explode3;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY      = -1;
    private float screenWidth = 1080;

    public EnemyShipE() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.10f, ENTER_SPEED * 0.10f);
        super.setVelocity(270, speed);
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);

        // Apply enemy sprite
        if (TextureRegistry.enemyE != null) {
            this.setTextureRegion(TextureRegistry.enemyE);
            setOrientInDirectionOfVelocity(false);
            setOrientation(180f);
        }

        super.addWeapon(new WeaponDoublePulse(this));
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.50f;
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
                float driftSpeed = MathUtils.sin(stateTime * 2.0f) * (HOVER_SPEED * 0.5f) / com.alexei.spaceshooter.SpaceShooter.FPS;
                super.setVelocityVector(driftSpeed, 0);
                break;
        }
    }
}
