package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.weapon.WeaponEnergyBallA;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipB — Elite gunner.
 *
 * Heavier than ShipA. Enters from top, hovers higher (75% screen height),
 * fires energy balls at the player while drifting sideways.
 */
public class EnemyShipB extends Unit {
    private static final float UNIT_WIDTH   = 70;
    private static final float UNIT_HEIGHT  = 70;
    private static final float ENTER_SPEED  = 260;
    private static final float HOVER_SPEED  = 55;
    private static final Color UNIT_COLOR   = Color.CHARTREUSE;
    private static final float MAX_LIFE     = 5f;
    private static final SoundName DEATH_SOUND = SoundName.Explode2;
    private static final int STARS_COUNT = 2;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY       = -1;
    private float screenWidth  = 1080;
    private float hoverDir     = 1f;

    public EnemyShipB() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.12f, ENTER_SPEED * 0.12f);
        super.setVelocity(270, speed);
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);
        super.setStarCount(STARS_COUNT);
        // Apply enemy sprite (rotate 180° so it faces down toward player)
        com.badlogic.gdx.graphics.g2d.TextureRegion reg = TextureRegistry.enemy2 != null ? TextureRegistry.enemy2 : TextureRegistry.enemy1;
        if (reg != null) {
            this.setTextureRegion(reg.getTexture());
            setOrientInDirectionOfVelocity(false);
            setOrientation(180f);
        }

        super.addWeapon(new WeaponEnergyBallA(this));
    }

    /** Called by EnemyFactory after creation. */
    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.65f; 
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
