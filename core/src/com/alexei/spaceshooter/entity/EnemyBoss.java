package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.weapon.WeaponSpreadShot;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class EnemyBoss extends Unit {
    private static final float UNIT_WIDTH  = 250;
    private static final float UNIT_HEIGHT = 250;
    private static final float ENTER_SPEED = 150;     
    private static final float HOVER_SPEED = 100;      
    private static final Color UNIT_COLOR  = Color.valueOf("ff0055"); 
    private static final float MAX_LIFE    = 100f;
    private static final SoundName DEATH_SOUND = SoundName.Explode5;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY       = -1;   
    private float screenWidth  = 1080; 
    private float hoverDir     = 1f;   

    public EnemyBoss() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        super.setVelocity(270, ENTER_SPEED); 
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);
        
        super.setStarCount(15);
        super.addWeapon(new WeaponSpreadShot(this));
        
        // Let's add a second weapon to make it more boss-like
        com.alexei.spaceshooter.weapon.Weapon w2 = new com.alexei.spaceshooter.weapon.WeaponEnemyLaser(this);
        w2.setFireRate(800);
        super.addWeapon(w2);
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.hoverY      = screenHeight * 0.75f; 
        hoverDir = MathUtils.randomBoolean() ? 1f : -1f;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        switch (moveState) {
            case ENTERING:
                if (hoverY > 0 && getY() <= hoverY) {
                    moveState = MoveState.HOVERING;
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                }
                break;

            case HOVERING:
                float padding = getWidth() + 20;
                if (getX() <= padding && hoverDir < 0) {
                    hoverDir = 1f;
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                } else if (getX() + getWidth() >= screenWidth - padding && hoverDir > 0) {
                    hoverDir = -1f;
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                }
                break;
        }
    }
    
    @Override
    public void receiveDamage(float damageAmount, Visual visual) {
        super.receiveDamage(damageAmount, visual);
        if (getLife() <= 0) {
            // Guarantee weapon upgrade drop
            SpaceShooter.items.add(new ItemWeaponUpgrade(getCenterX() - ItemWeaponUpgrade.ITEM_SIZE / 2, getCenterY() - ItemWeaponUpgrade.ITEM_SIZE / 2));
            SpaceShooter.items.add(new ItemHP(getCenterX() - ItemHP.ITEM_SIZE / 2, getCenterY() - ItemHP.ITEM_SIZE / 2));
        }
    }
}
