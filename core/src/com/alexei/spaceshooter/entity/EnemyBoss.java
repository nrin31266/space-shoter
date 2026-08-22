package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.weapon.WeaponSpreadShot;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

public class EnemyBoss extends Unit {
    // Boss visual size (balanced for multi-boss and single-boss battles).
    private static final float UNIT_WIDTH  = 340;
    private static final float UNIT_HEIGHT = 340;
    private static final float ENTER_SPEED = 280;     
    private static final float HOVER_SPEED = 100;      
    private static final Color UNIT_COLOR  = Color.valueOf("ff0055"); 
    private static final float MAX_LIFE    = 100f;
    private static final SoundName DEATH_SOUND = SoundName.Explode5;

    private enum MoveState { ENTERING, HOVERING }
    private MoveState moveState = MoveState.ENTERING;

    private float hoverY       = -1;   
    private float screenWidth  = 1080; 
    private float hoverDir     = 1f;   
    private float minX         = 16f;
    private float maxX         = -1f;
    /** N1 fix: prevent double-drop when multiple projectiles kill the boss in the same frame. */
    private boolean hasDropped = false;
    /** Boss's aimed plasma shot (updated with the player target each frame). */
    private com.alexei.spaceshooter.weapon.WeaponSniperBeam sniperWeapon = null;

    public EnemyBoss() {
        super(0, 0, UNIT_WIDTH, UNIT_HEIGHT);
        setArrived(false); // boss doesn't fire until it's fully on screen
        super.setVelocity(270, ENTER_SPEED); 
        super.setColor(UNIT_COLOR);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.clearDeathSounds();
        super.addDeathSound(DEATH_SOUND);
        
        super.setStarCount(15);

        // Apply boss sprite
        if (TextureRegistry.boss != null) {
            this.setTextureRegion(TextureRegistry.boss);
            setOrientInDirectionOfVelocity(false);
            setOrientation(180f);
        }
        super.addWeapon(new WeaponSpreadShot(this));

        // Boss heavy shots use the menacing plasma orb.
        for (com.alexei.spaceshooter.weapon.Weapon w : getWeapons()) {
            w.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.plasmaOrb, true);
            w.setDamage(1.8f); // boss shots hit harder
        }

        // Second weapon: fast straight-down plasma volley (harder to dodge).
        com.alexei.spaceshooter.weapon.Weapon w2 = new com.alexei.spaceshooter.weapon.WeaponEnemyLaser(this);
        w2.setFireRate(650);
        w2.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.plasmaOrb, true);
        w2.setDamage(1.3f);
        super.addWeapon(w2);

        // Third weapon: aimed sniper plasma shot that tracks the player.
        com.alexei.spaceshooter.weapon.WeaponSniperBeam w3 = new com.alexei.spaceshooter.weapon.WeaponSniperBeam(this);
        w3.setFireRate(2200);
        w3.setProjectileVisual(com.alexei.spaceshooter.utils.TextureRegistry.plasmaOrb, true);
        w3.setDamage(1.5f);
        super.addWeapon(w3);
        this.sniperWeapon = w3;
    }

    public void setScreenDimensions(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        if (this.hoverY <= 0) {
            this.hoverY = screenHeight * 0.75f;
        }
        if (this.minX < 0) this.minX = 16f;
        if (this.maxX <= 0) this.maxX = screenWidth - getWidth() - 16f;
        if (this.hoverDir == 0) {
            this.hoverDir = MathUtils.randomBoolean() ? 1f : -1f;
        }
    }

    public void setHoverY(float hoverY) {
        this.hoverY = hoverY;
    }

    public void setPatrolBounds(float minX, float maxX, float hoverDir) {
        this.minX = minX;
        this.maxX = maxX;
        this.hoverDir = hoverDir;
        if (moveState == MoveState.HOVERING) {
            super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        switch (moveState) {
            case ENTERING:
                if (hoverY > 0 && getY() <= hoverY) {
                    moveState = MoveState.HOVERING;
                    setArrived(true);
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                }
                break;

            case HOVERING:
                float leftBound = (minX >= 0) ? minX : 16f;
                float rightBound = (maxX > 0) ? maxX : (screenWidth - getWidth() - 16f);
                if (getX() <= leftBound && hoverDir < 0) {
                    hoverDir = 1f;
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                } else if (getX() >= rightBound && hoverDir > 0) {
                    hoverDir = -1f;
                    super.setVelocityVector(HOVER_SPEED * hoverDir / com.alexei.spaceshooter.SpaceShooter.FPS, 0);
                }
                break;
        }
    }
    
    /**
     * Boss death: a massive multi-burst explosion fitting a boss climax.
     */
    @Override
    ArrayList<com.alexei.spaceshooter.entity.Visual> getDeathEffect(Visual visual) {
        java.util.ArrayList<com.alexei.spaceshooter.entity.Visual> effects =
                new java.util.ArrayList<com.alexei.spaceshooter.entity.Visual>();

        float cx = getCenterX();
        float cy = getCenterY();

        // Giant fireball (scaled up)
        com.alexei.spaceshooter.effect.EffectExplosion big =
                new com.alexei.spaceshooter.effect.EffectExplosion(cx, cy, this, 90, 16f, 420f);
        big.setColor(new com.badlogic.gdx.graphics.Color(1f, 0.45f, 0.05f, 1f));
        effects.add(big);

        // White-hot core flash
        com.alexei.spaceshooter.effect.EffectFlash flash =
                new com.alexei.spaceshooter.effect.EffectFlash(cx, cy, this);
        flash.setColor(new com.badlogic.gdx.graphics.Color(1f, 0.95f, 0.75f, 1f));
        effects.add(flash);

        // Second delayed burst (offscreen-scaling, all directions)
        com.alexei.spaceshooter.effect.EffectExplosion burst =
                new com.alexei.spaceshooter.effect.EffectExplosion(cx, cy, this, 60, 10f, 500f);
        burst.setColor(new com.badlogic.gdx.graphics.Color(1f, 0.7f, 0.2f, 1f));
        effects.add(burst);

        // Radial debris sparks
        effects.addAll(com.alexei.spaceshooter.effect.EffectSpawrksSpawner.makeSparks(
                this, cx, cy, new float[]{0, 60, 120, 180, 240, 300}, new float[]{20, 20, 20, 20, 20, 20}));

        return effects;
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
