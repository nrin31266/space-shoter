package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.Timer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Alex on 03/07/2015.
 * Represents an item that the player could pick up, such as a star or upgrade.
 * Usually dropped by enemy units when killed.
 */
public class Item extends Visual {
    public static final int PICK_UP_ANIMATION_DURATION = 250; // ms
    public float SPEED = 300f; // increased from 200 to 350 for faster drop
    public static final int VELOCITY_ANIMATION_DURATION = 400; // reduced from 1000 to 400
    public static final float PICK_UP_ANIMATION_SCALE = 1f; // ms
    private boolean isMagnetizing = false;
    private SoundName pickUpSound = SoundName.Hit7;

    private boolean pickedUp = false; // indicated whether the item was picked up by the player or not
    private Timer pickUpTimer = new Timer(PICK_UP_ANIMATION_DURATION,1); // the animation timer which is activated when pickUp() is called.
    private Timer velocityTimer = new Timer(VELOCITY_ANIMATION_DURATION,1); // times how long the item's position will be animated for. Used to simulate how a dropped-item would scatter when unit explodes.


    public Item(float x, float y, float width, float height) {
        super(x, y, width, height);

        // set random initial velocity
        super.setVelocity(MathUtils.random(0, 359), SPEED);
    }

    private int bounceCount = 2; // Default 2 bounces
    
    protected float gravity = 1200f;
    protected float terminalVelocity = -1200f;

    @Override
    public void update(float deltaTime) {
        if (pickedUp && pickUpTimer.isTimerElapsed()) return;

        if (isMagnetizing) {
            super.update(deltaTime);
        }
        else {
            velocityTimer.update(deltaTime);
            if (!velocityTimer.isTimerElapsed()) { // update item position until timer runs out, then item stops
                super.update(deltaTime);
                super.setSpeed((1-velocityTimer.getProgress()) * SPEED);
                if (velocityTimer.isTimerElapsed()) {
                    super.setVelocity(270, SPEED); // Initialize falling velocity
                }
            }
            else {
                super.update(deltaTime);
                
                if (getY() <= 0 && bounceCount > 0) {
                    setY(0);
                    Vector2 vel = getVelocity();
                    vel.y = Math.abs(vel.y) * 0.6f; // Bounce up
                    setVelocity(vel);
                    bounceCount--;
                }
                
                // apply gravity if it is bouncing or falling
                Vector2 vel = getVelocity();
                float velY_PPS = vel.y * com.alexei.spaceshooter.SpaceShooter.FPS;
                velY_PPS -= gravity * (deltaTime / 1000f); 
                if (velY_PPS < terminalVelocity) velY_PPS = terminalVelocity;
                vel.y = velY_PPS / com.alexei.spaceshooter.SpaceShooter.FPS;
                setVelocity(vel);
            }
        }

        if (isPickUpAnimationRunning()) pickUpTimer.update(deltaTime); // start to update timer when pickUp is called
    }

    public float getPickUpAnimationScale() {
        if (pickUpAnimationProgress() < 0.5f) {
            return 1 + PICK_UP_ANIMATION_SCALE * pickUpAnimationProgress() * 2;
        }
        else {
            return (1 + PICK_UP_ANIMATION_SCALE) * (1-pickUpAnimationProgress()) * 2;
        }
    }

    public void pickUp() {
        pickedUp = true;
        SpaceShooter.playSound(pickUpSound);
        // TODO: pick up action/animation.
    }

    public void magnetize(Visual toVisual) {
        isMagnetizing = true;
        setDirection(toVisual);
        setSpeed(750); // increased from 300 to 750 for faster suction
    }

    public void unmagnetize() {
        isMagnetizing = false;
        if (getSpeed() > 0 && velocityTimer.isTimerElapsed()) {
            velocityTimer.reset();
            velocityTimer.setDuration(500);
            SPEED = getSpeed();
        }
    }

    @Override
    public boolean isDead() {
        if (getY() < -100) return true; // Disappear when falling off screen
        return pickUpAnimationProgress() >= 1;
    }

    protected boolean isPickUpAnimationRunning() { return (pickedUp && !pickUpTimer.isTimerElapsed()) ; }
    protected float pickUpAnimationProgress() { return pickUpTimer.getElapsedTime()/(float)pickUpTimer.getDuration() ; }

    public SoundName getPickUpSound() {
        return pickUpSound;
    }

    public void setPickUpSound(SoundName pickUpSound) {
        this.pickUpSound = pickUpSound;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public boolean isMagnetizing() {
        return isMagnetizing;
    }

    public void setIsMagnetizing(boolean isMagnetizing) {
        this.isMagnetizing = isMagnetizing;
    }
    
    public void setBounceCount(int count) {
        this.bounceCount = count;
    }
    
    public void setBaseSpeed(float speed) {
        this.SPEED = speed;
    }
}
