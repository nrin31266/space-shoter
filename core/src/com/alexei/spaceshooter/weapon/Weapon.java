package com.alexei.spaceshooter.weapon;

import com.alexei.spaceshooter.entity.Projectile;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.Timer;

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

    public Weapon() {
        timer = new Timer(0, 0);
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
            projectiles.addAll(Arrays.asList(fire()));
            if (audioManager != null) {
                audioManager.playSound(weaponSoundName);
            }
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
    public void setFireRate(int fireRate) { this.fireRate = fireRate; timer.setDuration(fireRate); timer.reset(); }
    public void setUnit(Unit unit) { this.unit = unit; }
    public Unit getUnit() { return unit; }
    public void setWeaponSound(SoundName name) { this.weaponSoundName = name; }
}