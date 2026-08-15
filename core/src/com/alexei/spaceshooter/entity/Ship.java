package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.alexei.spaceshooter.weapon.WeaponExplosiveBlaster;
import com.alexei.spaceshooter.weapon.WeaponHomingLightning;
import com.alexei.spaceshooter.weapon.WeaponShipLaser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Player spaceship entity.
 * Uses a SINGLE SHARED weapon level (1 to 7) and SINGLE SHARED stockpile (0 to 3) across all 3 weapon tracks.
 * Eating ANY weapon item increases the global level by +1 and switches active weapon type to that item.
 */
public class Ship extends Unit {
    private static final float UNIT_POSITION_X = 0;
    private static final float UNIT_POSITION_Y = 0;
    // Player ship visual size — slightly trimmed for a more balanced on-screen
    // presence (was 160). Hitbox scales with it.
    private static final float UNIT_WIDTH = 150;
    private static final float UNIT_HEIGHT = 150;
    public static final float INITIAL_LIFE = 5f;
    public static final float MAX_LIFE = 10f;
    private static final Color COLOR = Color.MAROON;
    private static final SoundName DEATH_SOUND = SoundName.EndGame;
    private static final SoundName DAMAGE_SOUND = SoundName.GetDamage;

    // Weapon tracks
    public static final int WEAPON_TYPE_PLASMA    = 0;
    public static final int WEAPON_TYPE_EXPLOSIVE = 1;
    public static final int WEAPON_TYPE_HOMING    = 2;

    public static final int MAX_WEAPON_LEVEL = 7;
    public static final int MAX_STOCKPILE    = 3;

    private int activeWeaponType = WEAPON_TYPE_PLASMA;
    private int weaponLevel = 1; // Single shared global level across all weapon types
    private int stockpile = 0;   // Single shared global stockpile

    private float invulnerabilityTimer = 0f;
    private boolean isCriticalLifeActivated = false;
    private AudioManager audioManager;

    // Movement velocity (px/sec) derived from touch drag, so fired projectiles
    // can inherit lateral ship motion and stay aligned with the ship nose.
    private float lastMoveX = 0f;
    private float lastMoveY = 0f;
    private float moveVelX = 0f;
    private float moveVelY = 0f;
    private boolean moveBaseSet = false;

    private WeaponShipLaser weaponLaser;
    private WeaponExplosiveBlaster weaponExplosive;
    private WeaponHomingLightning weaponHoming;

    public Ship() {
        super(UNIT_POSITION_X, UNIT_POSITION_Y, UNIT_WIDTH, UNIT_HEIGHT);
        super.setMaxLife(MAX_LIFE);
        super.setLife(INITIAL_LIFE);
        super.setColor(COLOR);

        super.clearSounds();
        super.addDeathSound(DEATH_SOUND);
        super.addDamageSound(DAMAGE_SOUND);

        // Apply ship sprite if available
        if (TextureRegistry.ship != null) {
            this.setTextureRegion(TextureRegistry.ship);
            setOrientInDirectionOfVelocity(false); // ship faces up, not in direction of motion
        }

        // Instantiate player weapons
        weaponLaser = new WeaponShipLaser(this);
        weaponExplosive = new WeaponExplosiveBlaster(this);
        weaponHoming = new WeaponHomingLightning(this);

        updateActiveWeaponState();
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        weaponLaser.setAudioManager(audioManager);
        weaponExplosive.setAudioManager(audioManager);
        weaponHoming.setAudioManager(audioManager);
    }

    public int getActiveWeaponType() {
        return activeWeaponType;
    }

    public void setActiveWeaponType(int type) {
        this.activeWeaponType = MathUtils.clamp(type, 0, 2);
        updateActiveWeaponState();
    }

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public int getWeaponLevel(int type) {
        return weaponLevel;
    }

    public void setWeaponLevel(int level) {
        this.weaponLevel = MathUtils.clamp(level, 1, MAX_WEAPON_LEVEL);
        updateActiveWeaponState();
    }

    public void setWeaponLevel(int type, int level) {
        setWeaponLevel(level);
    }

    public int getStockpile() {
        return stockpile;
    }

    public int getStockpile(int type) {
        return stockpile;
    }

    public void setStockpile(int count) {
        this.stockpile = MathUtils.clamp(count, 0, MAX_STOCKPILE);
    }

    public void setStockpile(int type, int count) {
        setStockpile(count);
    }

    /**
     * Upgrade weapon: switches to target weapon type AND increases shared weapon level by +1.
     * If already at Max Level (7), increments shared Stockpile up to 3.
     */
    public void upgradeWeaponType(int type) {
        this.activeWeaponType = MathUtils.clamp(type, 0, 2);
        if (weaponLevel < MAX_WEAPON_LEVEL) {
            weaponLevel++;
        } else if (stockpile < MAX_STOCKPILE) {
            stockpile++;
        }
        updateActiveWeaponState();
    }

    /** Switch the active weapon track WITHOUT changing level (used by weapon-switch pickups). */
    public void switchWeaponType(int type) {
        this.activeWeaponType = MathUtils.clamp(type, 0, 2);
        updateActiveWeaponState();
    }

    /**
     * Weapon-item pickup handler: if the item is for the weapon track ALREADY
     * active, it raises the level (+1 / stockpile). Otherwise it switches track.
     */
    public void onWeaponPickup(int type) {
        if (activeWeaponType == MathUtils.clamp(type, 0, 2)) {
            upgradeEnergy();
        } else {
            switchWeaponType(type);
        }
    }

    /** Pure energy/power upgrade: raises the shared weapon level (no weapon switch). */
    public void upgradeEnergy() {
        if (weaponLevel < MAX_WEAPON_LEVEL) {
            weaponLevel++;
        } else if (stockpile < MAX_STOCKPILE) {
            stockpile++;
        }
        updateActiveWeaponState();
    }

    public void upgradeWeapon() {
        upgradeWeaponType(activeWeaponType);
    }

    /**
     * Downgrade weapon when receiving damage:
     * - If stockpile > 0, consume 1 stockpile charge (stay at Lv 7).
     * - Else if level > 1, downgrade level by 1.
     * - Grants 1.5s invulnerability flash + neon shield bubble.
     */
    public void downgradeWeapon() {
        if (invulnerabilityTimer > 0) return;

        if (stockpile > 0) {
            stockpile--; // Protects weapon level
        } else if (weaponLevel > 1) {
            weaponLevel--;
        }
        triggerInvulnerability(1.5f);
        updateActiveWeaponState();
    }

    public void triggerInvulnerability(float duration) {
        this.invulnerabilityTimer = duration;
        flash();
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    @Override
    public void render(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer, com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (isDead()) return;

        // Render subtle glowing neon shield bubble when invulnerable (only if ShapeRenderer is currently drawing)
        if (invulnerabilityTimer > 0 && shapeRenderer != null && shapeRenderer.isDrawing()) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

            float cx = getCenterX();
            float cy = getCenterY();
            float radius = getWidth() * 0.75f;
            float pulse = (MathUtils.sin(invulnerabilityTimer * 12f) + 1f) * 0.5f;

            // Outer cyan & gold glowing shield aura
            shapeRenderer.setColor(0f, 0.92f, 1f, 0.25f + pulse * 0.20f);
            shapeRenderer.circle(cx, cy, radius + 5f, 32);
            shapeRenderer.setColor(1f, 0.85f, 0.1f, 0.50f + pulse * 0.30f);
            shapeRenderer.circle(cx, cy, radius, 32);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(cx, cy, radius - 3f, 32);
        }

        // Ship visibility: flash effect - render every other phase when invulnerable
        boolean shouldRender = true;
        if (invulnerabilityTimer > 0) {
            float flashPhase = (invulnerabilityTimer * 18f) % 2f;
            shouldRender = (flashPhase > 0.9f);
        }

        if (shouldRender) {
            super.render(shapeRenderer, batch);
        }
    }

    /**
     * Guarantees that ONLY the single active weapon is in the weapons list.
     */
    private void updateActiveWeaponState() {
        getWeapons().clear();
        switch (activeWeaponType) {
            case WEAPON_TYPE_EXPLOSIVE:
                weaponExplosive.setEnabled(true);
                addWeapon(weaponExplosive);
                break;
            case WEAPON_TYPE_HOMING:
                weaponHoming.setEnabled(true);
                addWeapon(weaponHoming);
                break;
            case WEAPON_TYPE_PLASMA:
            default:
                weaponLaser.setEnabled(true);
                addWeapon(weaponLaser);
                break;
        }
    }

    @Override
    public void update(float deltaTime) {
        // Derive ship movement velocity (px/sec) from position delta so fired
        // projectiles can inherit it (keeps bullets glued to the ship's nose).
        if (!moveBaseSet) {
            lastMoveX = getX();
            lastMoveY = getY();
            moveBaseSet = true;
            moveVelX = 0f;
            moveVelY = 0f;
        } else {
            float dtSec = Math.max(0.0001f, deltaTime / 1000f);
            moveVelX = (getX() - lastMoveX) / dtSec;
            moveVelY = (getY() - lastMoveY) / dtSec;
        }
        lastMoveX = getX();
        lastMoveY = getY();

        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= deltaTime / 1000f;
            if (invulnerabilityTimer < 0) invulnerabilityTimer = 0;
        }
        super.update(deltaTime);
    }

    /** Lateral ship velocity in px/sec (from touch drag). Used to inherit motion into fired projectiles. */
    public float getMoveVelX() { return moveVelX; }
    public float getMoveVelY() { return moveVelY; }

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
        if (invulnerabilityTimer > 0) return; // Ignore damage during stockpile invulnerability

        super.receiveDamage(damageAmount, visual);

        if (damageAmount > 0) {
            downgradeWeapon();
        }

        if (audioManager != null) {
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
        // empty on purpose
    }

    public boolean isCriticalLifeActivated() {
        return isCriticalLifeActivated;
    }

    public void setIsCriticalLifeActivated(boolean isCriticalLifeActivated) {
        this.isCriticalLifeActivated = isCriticalLifeActivated;
    }
}
