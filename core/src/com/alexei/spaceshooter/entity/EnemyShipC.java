package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.weapon.WeaponSniperBeam;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * EnemyShipC — Sniper.
 *
 * Small, fast entry. Hovers at 55% screen height, fires precise aimed shots.
 * Hot-pink color, 1.5 HP. Appears from wave 3.
 */
public class EnemyShipC extends Unit {
    // Sniper must be readable on a phone — bumped from 44px to 60px.
    private static final float UNIT_WIDTH   = 60;
    private static final float UNIT_HEIGHT  = 60;
    private static final float ENTER_SPEED  = 450;
    private static final float HOVER_SPEED  = 95;
    private static final Color UNIT_COLOR   = Color.valueOf("FF4FF4FF");
    private static final float MAX_LIFE     = 1.5f;
    private static final SoundName DEATH_SOUND = SoundName.Explode5;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY      = -1;
    private float screenWidth = 1080;
    private float hoverDir    = 1f;

    private WeaponSniperBeam sniperWeapon;

    public EnemyShipC() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        float speed = ENTER_SPEED + MathUtils.random(-ENTER_SPEED * 0.15f, ENTER_SPEED * 0.15f);
        super.setVelocity(270, speed);
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);
        sniperWeapon = new WeaponSniperBeam(this);
        // Apply enemy sprite
        if (TextureRegistry.enemyC != null) {
            this.setTextureRegion(TextureRegistry.enemyC);
            setOrientInDirectionOfVelocity(false);
            setOrientation(180f);
        }

        super.addWeapon(sniperWeapon);
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.55f;
        hoverDir = MathUtils.randomBoolean() ? 1f : -1f;
    }
    
    public void setHoverY(float hoverY) {
        this.hoverY = hoverY;
    }

    public WeaponSniperBeam getSniperWeapon() { return sniperWeapon; }

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
