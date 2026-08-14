package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipA — Basic infantry enemy.
 *
 * Flies in from the top, decelerates and hovers at ~70% of screen height,
 * then slowly drifts sideways until killed. Resembles the Chicken Invaders style.
 */
public class EnemyShipA extends Unit {
    private static final float UNIT_WIDTH  = 80;
    private static final float UNIT_HEIGHT = 80;
    private static final float ENTER_SPEED = 380;     // speed while entering from top
    private static final float HOVER_SPEED = 70;      // horizontal drift speed when hovering
    private static final Color UNIT_COLOR  = Color.valueOf("5DBBFFFF"); // ice blue
    private static final float MAX_LIFE    = 1f;
    private static final SoundName DEATH_SOUND = SoundName.Explode5;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY       = -1;   // set externally by EnemyFactory
    private float screenWidth  = 1080; // default; set externally
    private float hoverDir     = 1f;   // +1 right, -1 left

    public EnemyShipA() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        // slight speed variation so rows don't arrive perfectly in sync
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.12f, ENTER_SPEED * 0.12f);
        super.setVelocity(270, speed); // 270 = straight down
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);

        // Apply enemy sprite (rotate 180° so it faces down toward player)
        if (TextureRegistry.enemy1 != null) {
            this.setTextureRegion(TextureRegistry.enemy1);
            setOrientInDirectionOfVelocity(false);
            setOrientation(180f); // sprite faces up; flip 180° to face down
        }

        super.addWeapon(new com.alexei.spaceshooter.weapon.WeaponEnemyLaser(this));
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.60f; // hover at 60% from bottom
        // randomise initial drift direction
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
                // Once we reach hover altitude, switch to hover mode
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
