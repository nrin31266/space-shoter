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
    /** N1 fix: prevent double-drop when multiple projectiles kill the boss in the same frame. */
    private boolean hasDropped = false;

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
        if (getLife() <= 0 && !hasDropped) {
            hasDropped = true;

            float cx = getCenterX();
            float cy = getCenterY();

            // 1. Radial Star Burst: Scatter 20 stars outward in 360° explosion
            int starCount = 20;
            for (int i = 0; i < starCount; i++) {
                float angle = (i * (360f / starCount)) + MathUtils.random(-10f, 10f);
                float speed = MathUtils.random(450f, 750f);
                ItemStar star = new ItemStar(cx - ItemStar.STAR_SIZE_OUTER / 2f, cy - ItemStar.STAR_SIZE_OUTER / 2f, 1);
                star.setScatterVelocity(angle, speed);
                SpaceShooter.items.add(star);
            }

            // 2. Scatter Weapon Upgrades & HP Items in diagonal directions
            ItemWeaponUpgrade w1 = new ItemWeaponUpgrade(cx - ItemWeaponUpgrade.ITEM_SIZE / 2f, cy - ItemWeaponUpgrade.ITEM_SIZE / 2f);
            w1.setScatterVelocity(45f, 500f);
            SpaceShooter.items.add(w1);

            ItemWeaponUpgradeExplosive w2 = new ItemWeaponUpgradeExplosive(cx - ItemWeaponUpgradeExplosive.ITEM_SIZE / 2f, cy - ItemWeaponUpgradeExplosive.ITEM_SIZE / 2f);
            w2.setScatterVelocity(135f, 500f);
            SpaceShooter.items.add(w2);

            ItemHP hp1 = new ItemHP(cx - ItemHP.ITEM_SIZE / 2f, cy - ItemHP.ITEM_SIZE / 2f);
            hp1.setScatterVelocity(225f, 500f);
            SpaceShooter.items.add(hp1);

            ItemHP hp2 = new ItemHP(cx - ItemHP.ITEM_SIZE / 2f, cy - ItemHP.ITEM_SIZE / 2f);
            hp2.setScatterVelocity(315f, 500f);
            SpaceShooter.items.add(hp2);
        }
    }
}
