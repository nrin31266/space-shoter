package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.Timer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

/***
 * Created by Alex on 18/06/2015.
 *
 * An abstract class that represents a weapon. Weapons have a damage associated with them and a fire rate. The most important
 * method is the 'fire' method. It works by returning Projectiles according to a fire rate. The weapon's timer is updated every render call.
 * When the timer expires, the fire method is automatically called which creates a projectile (or projectiles), and the timer resets
 * and starts counting again.
 */
public abstract class Weapon {
    private float damage;
    private int fireRate;
    private Timer timer;
    private Unit unit;
    private SoundName weaponSoundName = null;
    private AudioManager audioManager;
    /** When false, weapon will not fire even if timer elapses. Used for touch-to-shoot. */
    private boolean enabled = true;

    /** Optional per-weapon projectile texture. Null = auto-select by projectile type. */
    protected TextureRegion projectileRegion = null;
    /** When true the projectile renders as a round orb (no direction rotation). */
    protected boolean projectileRound = false;

    public Weapon() {
        timer = new Timer(0, 0);
    }

    /** Assign the projectile visual used by this weapon. */
    public void setProjectileVisual(TextureRegion region, boolean round) {
        this.projectileRegion = region;
        this.projectileRound = round;
    }

    /** Apply this weapon's visual to a freshly created projectile. */
    protected void applyProjectileVisual(Projectile p) {
        if (projectileRegion != null) {
            p.setVisualRegion(projectileRegion);
            p.setRoundVisual(projectileRound);
        }
    }

    /**
     * Inherit a clamped fraction of the ship's lateral motion so fired shots
     * track the ship nose while dodging, WITHOUT ever stalling the shot.
     * The inherited component is clamped to a small fraction of the bullet's
     * own forward speed so a fast ship jerk can never make bullets float or
     * fly sideways.
     */
    protected void inheritShipMotion(Projectile p, float forwardSpeed) {
        Unit unit = getUnit();
        if (!(unit instanceof Ship)) return;
        Ship ship = (Ship) unit;

        float fps = SpaceShooter.FPS;
        // Max inherited drift is 30% of the shot's forward speed.
        float maxInherit = forwardSpeed * 0.30f / fps;
        float vx = MathUtils.clamp(ship.getMoveVelX() * 0.12f / fps, -maxInherit, maxInherit);
        // Only inherit upward ship motion (keeps shots always travelling up).
        float vy = MathUtils.clamp(ship.getMoveVelY() * 0.10f / fps, 0f, maxInherit);
        p.getVelocity().x += vx;
        p.getVelocity().y += vy;
    }

    /** Spawn a small muzzle flash at the ship's nose (reused shared effect list, no extra allocation). */
    protected void spawnMuzzleFlash(com.badlogic.gdx.graphics.Color color) {
        Unit unit = getUnit();
        if (unit == null) return;
        com.alexei.spaceshooter.effect.EffectFlash flash =
                new com.alexei.spaceshooter.effect.EffectFlash(unit.getCenterX(), unit.getTop(), unit);
        flash.setColor(color);
        com.alexei.spaceshooter.entity.Visual.addVisualEffect(flash);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Update the weapon timer. When it elapses, fire and add projectiles to the given list.
     * @param deltaTime
     * @param projectiles the list to add fired projectiles to
     */
    public void update(float deltaTime, ArrayList<Projectile> projectiles) {
        if (!enabled) {
            // Still advance timer so weapon doesn't fire a burst when re-enabled
            timer.update(deltaTime);
            return;
        }
        timer.update(deltaTime);

        if (timer.isTimerElapsed()) {
            // Cycle Skip Probability (Section 3.1): 20% skip chance for dense actions (>20 count)
            if (unit != null && unit.isDenseAction() && com.badlogic.gdx.math.MathUtils.random() < 0.20f) {
                float variance = fireRate * 0.25f;
                int nextDuration = fireRate + (int)com.badlogic.gdx.math.MathUtils.random(-variance, variance);
                timer.setDuration(nextDuration);
                return;
            }

            projectiles.addAll(Arrays.asList(fire()));
            if (audioManager != null) {
                audioManager.playSound(weaponSoundName);
            }
            // Add variance so enemies don't sync up over time
            float variance = fireRate * 0.25f;
            int nextDuration = fireRate + (int)com.badlogic.gdx.math.MathUtils.random(-variance, variance);
            timer.setDuration(nextDuration);
        }
    }

    /** Enable or disable this weapon. When disabled, it does not fire. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /***
     * The fire method produces a projectile according the the fire rate.
     * @return Returns a list of projectile objects
     */
    public abstract Projectile[] fire();

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public int getFireRate() { return fireRate; }
    public void setFireRate(int fireRate) { 
        this.fireRate = fireRate; 
        timer.setDuration(fireRate); 
        timer.reset(); 
        timer.setElapsedTime(com.badlogic.gdx.math.MathUtils.random(0, fireRate));
    }
    public void setUnit(Unit unit) { this.unit = unit; }
    public Unit getUnit() { return unit; }
    public void setWeaponSound(SoundName name) { this.weaponSoundName = name; }
}