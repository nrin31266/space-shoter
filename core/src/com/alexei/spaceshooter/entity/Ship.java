package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.weapon.WeaponShipLaser;
import com.alexei.spaceshooter.weapon.WeaponShipRocket;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Alex on 17/06/2015.
 *
 * Represents the spaceship in the game
 */
public class Ship extends Unit {
    private static final float UNIT_POSITION_X = 0;
    private static final float UNIT_POSITION_Y = 0;
    private static final float UNIT_WIDTH = 80;
    private static final float UNIT_HEIGHT = 80;
    private static final float MAX_LIFE = 5f;
    private static final Color COLOR = Color.MAROON;
    private static final SoundName DEATH_SOUND = SoundName.EndGame;
    private static final SoundName DAMAGE_SOUND = SoundName.GetDamage;

    private boolean isCriticalLifeActivated = false;
    private AudioManager audioManager;

    public Ship() {
        super(UNIT_POSITION_X, UNIT_POSITION_Y, UNIT_WIDTH, UNIT_HEIGHT);
        super.setMaxLife(MAX_LIFE);
        super.setLife(MAX_LIFE);
        super.setColor(COLOR);

        super.clearSounds();
        super.addDeathSound(DEATH_SOUND);
        super.addDamageSound(DAMAGE_SOUND);

        // add weapons
        super.addWeapon(new WeaponShipLaser(this));
        super.addWeapon(new WeaponShipRocket(this));
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        super.render(sr, batch);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void receiveDamage(Projectile projectile) {
        receiveDamage(projectile.getDamage(), projectile);
        Gdx.input.vibrate(300);
    }

    @Override
    public void receiveDamage(Unit unit) {
        receiveDamage(1f, unit);
        Gdx.input.vibrate(300);
    }

    @Override
    public void receiveDamage(float damageAmount, Visual visual) {
        super.receiveDamage(damageAmount, visual);

        if (audioManager != null) {
            // play alarm when health is below critical
            if (!this.isCriticalLifeActivated() && this.isCriticalHealth()) {
                this.setIsCriticalLifeActivated(true);
                audioManager.playSound(SoundName.Warning);
                audioManager.playSound(SoundName.Alarm, true);
            } else if (this.isCriticalLifeActivated() && !this.isCriticalHealth()) {
                this.setIsCriticalLifeActivated(false);
                audioManager.stopSound(SoundName.Alarm);
            }
        }
    }

    @Override
    public void generateDamagePoints(Visual visual) {
        // empty on purpose because we don't want to show damage points on ship
    }

    public boolean isCriticalLifeActivated() {
        return isCriticalLifeActivated;
    }

    public void setIsCriticalLifeActivated(boolean isCriticalLifeActivated) {
        this.isCriticalLifeActivated = isCriticalLifeActivated;
    }
}
