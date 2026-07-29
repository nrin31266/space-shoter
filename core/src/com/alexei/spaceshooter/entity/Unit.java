package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.effect.EffectExplosion;
import com.alexei.spaceshooter.effect.EffectSpawrksSpawner;
import com.alexei.spaceshooter.effect.ParticleEmitter;
import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.Timer;
import com.alexei.spaceshooter.weapon.Weapon;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Created by Alex on 18/06/2015.
 *
 * Represents a game unit such as the player's ship or other ships, etc.
 * Anything that could give/receive damage, move and shoot projectiles is a unit.
 */
public class Unit extends Visual {
    private String name; // the name of the unit
    private float life = 99999; // the amount of life the unit currently has. when life > 0 the unit is considered alive, otherwise dead.
    private float maxLife = 99999; // the total life that the unit has
    private ArrayList<Weapon> weapons = new ArrayList<Weapon>(); // a list of weapons that this unit holds and uses. A unit may have no weapons.
    private ArrayList<SoundName> damageSounds = new ArrayList<SoundName>();
    private ArrayList<SoundName> deathSounds = new ArrayList<SoundName>();
    private ArrayList<DamagePoint> damagePoints = new ArrayList<DamagePoint>(); // holds a list of points where damage was taken, they are used to render damage decals
    private Timer timer;
    private boolean flashRunning; // indicates that a call to flash() was made and the timer is running. The timer is used to control the alpha value of a rectangle that is drawn on top of the visual.
    private Color flashColor = new Color(Color.WHITE);
    public static final int FLASH_DURATION = 80; // ms
    public static final float FLASH_OPACITY_LIMIT = 0.3f;
    public static final float CRITICAL_LIFE_LIMIT = 0.3f; // 0.0 to 1.0, represents the point bellow which life is considered critical
    private static final SoundName DAMAGE_SOUND = SoundName.Explode;
    private static final SoundName DEATH_SOUND = SoundName.Explode;


    private int starCount = 1; // the amount of stars this unit drops when destroyed

    public Unit(float x, float y, float width, float height) {
        super(x, y, width, height);

        damageSounds.add(DAMAGE_SOUND);
        deathSounds.add(DEATH_SOUND);

        timer = new Timer(0,1);
        flashColor.a = 0;
    }

    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float dx=0; // small offsets, used for rendering only, when the unit is hit, the physical location of the unit is not changed.
        float dy=0;

        if (flashRunning) { // when the unit is hit, it "flashes", and is also slightly displaced, to show the force of impact.
            dx = -3;
            dy = 4;
        }


        if (getTextureRegion() == null) {
            sr.setColor(getColor());
            sr.rect(getX() + dx, getY() + dy, getWidth(), getHeight());
        }
        else {
             super.render(sr,batch);
        }

        // draw the unit "flashing". Represented here simply by a rectangle with some alpha
        if (flashRunning) {
            sr.setColor(flashColor);
            sr.rect(getX()+dx,getY()+dy, getWidth(), getHeight());
        }

        // render damage points
        for (DamagePoint p : damagePoints) {
            sr.setColor(Color.BLACK.cpy());
            sr.getColor().a = 0.25f;
            sr.rect(position.x + p.pos.x + dx, position.y + p.pos.y + dy, p.dim.x, p.dim.y);
        }
    }

    /***
     * The unit update method where all unit logic is performed: how to move, aim.
     * Weapon updates are now handled externally by GamePlayScreen.
     * Make sure to call super.updatePosition() to update the position of the Unit when overriding.
     */
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (flashRunning) {
            timer.update(deltaTime);
            if (timer.isTimerElapsed()) {
                flashRunning = false;
            }
            float x = timer.getElapsedTime()/(float)timer.getDuration();
            flashColor.a = x*FLASH_OPACITY_LIMIT;
        }
    }

    /***
     * Receives damage from a given projectile.
     * @param projectile The projectile that will be doing the damage
     */
    public void receiveDamage(Projectile projectile) {
        receiveDamage(projectile.getDamage(), this);
        generateDamagePoints(projectile);
    }

    /***
     * Receives damage from a given unit. Works in the same way as receiveDamage(Projectile),
     * except that it is called when a collision between two units occurs, such a collision can only occur
     * between the player's ship and an enemy unit. In this case the enemy unit is killed.
     * This method is overridden for the player's ship so that the player is not killed, but receives some damage (TODO)
     * @param unit the unit which is doing the damage
     */
    public void receiveDamage(Unit unit) {
        receiveDamage(life, this); // kill the unit immediately by doing as much damage as there is life left
    }

    public void receiveDamage(float damageAmount, Visual visual) {
        life -= damageAmount;

        float percentLife = life/maxLife;

        // TODO: play sound and show effect depending on how much life is left
        if (percentLife > CRITICAL_LIFE_LIMIT) { // regular damage
            Visual.addVisualEffects(getRegularDamageEffect(visual));
            flash();
            SpaceShooter.playSound(getDamageSound());
        }
        else if (percentLife < CRITICAL_LIFE_LIMIT && percentLife > 0) { // critical damage
            Visual.addVisualEffects(getCriticalDamageEffect(visual));
            flash();
            SpaceShooter.playSound(getDamageSound());
        }
        else if (life <= 0) { // death
            Visual.addVisualEffects(getDeathEffect(visual));
            dropStars();
            SpaceShooter.playSound(getDeathSound());
        }
    }

    public void flash() {
        timer.setDuration(FLASH_DURATION);
        timer.reset();
        flashRunning = true;
    }

    /**
     * Called when the unit is damaged, according to the amount of life remaining, a corresponding
     * damage effect is produced. When extending Unit, it is recommended to overwrite getRegularDamageEffect(),
     * getCriticalDamageEffect(), getDeathEffect() methods.
     * @return
     */
    /*ArrayList<Visual> makeDamageEffects(Visual visual) {
        float percentLife = life/maxLife;

        if (percentLife > CRITICAL_LIFE_LIMIT) { // show regular damage effect
            return getRegularDamageEffect(visual);
        }
        else if (percentLife < CRITICAL_LIFE_LIMIT && percentLife > 0) { // show critical damage effect
            return getCriticalDamageEffect(visual);
        }
        else { // show death effect
            return getDeathEffect(visual);

        }
    }*/

    public void generateDamagePoints(Visual visual) {
        // generate a damage mark on the unit
        int spots = MathUtils.random(1, 5);
        for (int i = 0; i < spots; i++) {
            int dx = MathUtils.random(0, 15);
            int dy = MathUtils.random(0, 5);
            int xsize = MathUtils.random(10, 20);
            int ysize = MathUtils.random(10, 20);
            damagePoints.add(new DamagePoint(new Vector2(visual.getX() - position.x + dx, dy), new Vector2(xsize, ysize)));
        }
    }

    private void dropStars() {
        // Boss handles its own drops in its override.
        float rand = MathUtils.random(1f);
        if (rand < 0.15f) { // 15% chance for weapon upgrade
            SpaceShooter.items.add(new ItemWeaponUpgrade(getCenterX() - ItemWeaponUpgrade.ITEM_SIZE / 2, getCenterY() - ItemWeaponUpgrade.ITEM_SIZE / 2));
        } else if (rand < 0.20f) { // 5% chance for HP
            SpaceShooter.items.add(new ItemHP(getCenterX() - ItemHP.ITEM_SIZE / 2, getCenterY() - ItemHP.ITEM_SIZE / 2));
        }
        
        if (MathUtils.randomBoolean(0.9f)) {
            for(int i=0;i<starCount;i++) {
                SpaceShooter.items.add(new ItemStar(getCenterX()-ItemStar.STAR_SIZE_OUTER/2,getCenterY()-ItemStar.STAR_SIZE_OUTER/2,1));
            }
        }
    }

    public boolean isCriticalHealth() {
        float percentLife = life/maxLife;
        return percentLife < CRITICAL_LIFE_LIMIT && percentLife > 0;
    }

    ArrayList<Visual> getRegularDamageEffect(Visual visual) {
        return EffectSpawrksSpawner.makeSparks(this, visual.getCenterX(), this.getY(), new float[]{270}, new float[]{20});
    }

    ArrayList<Visual> getCriticalDamageEffect(Visual visual) {
        return new ArrayList<Visual>();
    }

    ArrayList<Visual> getDeathEffect(Visual visual) {
        ArrayList<Visual> effects = new ArrayList<Visual>();

        ParticleEmitter sparks = new EffectExplosion(getCenterX(), getCenterY(), this);
        sparks.setColor(getColor().cpy());
        effects.add(sparks);

        effects.addAll(EffectSpawrksSpawner.makeSparks(this, visual.getCenterX(), this.getY(), new float[]{30, 150, 270}, new float[]{18, 23, 20}));

        return effects;
    }

    public ArrayList<Weapon> getWeapons() { return weapons; }
    public void addWeapon(Weapon weapon) { weapons.add(weapon); }
    public void removeWeapon(Weapon weapon) { weapons.remove(weapon); }
    public boolean isDead() { return life <= 0; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getLife() { return life; }
    public void setLife(float life) { this.life = life; }
    public float getMaxLife() { return maxLife; }
    public void setMaxLife(float maxLife) { this.maxLife = maxLife; }

    public SoundName getDamageSound() {
        if (damageSounds.size()==0) return null;
        else return damageSounds.get(MathUtils.random(0,damageSounds.size()-1));
    }
    public void addDamageSound(SoundName damageSound) { if (damageSound!=null) this.damageSounds.add(damageSound); }

    public SoundName getDeathSound() {
        if (deathSounds.size()==0) return null;
        else return deathSounds.get(MathUtils.random(0,deathSounds.size()-1));
    }
    public void addDeathSound(SoundName deathSound) { if (deathSound!=null) this.deathSounds.add(deathSound); }

    public void clearSounds() { damageSounds.clear(); deathSounds.clear(); }
    public void clearDeathSounds() { deathSounds.clear();  }
    public void clearDamageSounds() { damageSounds.clear();}
    public ArrayList<DamagePoint> getDamagePoints() { return damagePoints; }
    public void setDamagePoints(ArrayList<DamagePoint> damagePoints) { this.damagePoints = damagePoints; }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    /***
     * Add life to the unit. The amount of life is bounded by unit's maxLife.
     * @param life the amount of life to ad to the unit
     */
    public void addLife(float life) { this.life += life; }


    /***
     * Represents a point where damage was taken (from a projectile). These points are created when the player's projectile
     * hits an enemy unit. When the unit is rendered, these points are rendered as little black marks on the
     * body of the unit. This adds a little more detail to the game and makes it look somewhat polished.
     */
    public class DamagePoint {
        public Vector2 pos; // position
        public Vector2 dim; // dimension

        public DamagePoint(Vector2 pos, Vector2 dim) {
            this.pos = pos;
            this.dim = dim;
        }
    }
}
