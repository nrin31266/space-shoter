package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.entity.Visual;
import com.alexei.spaceshooter.utils.Timer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Alex on 17/06/2015.
 *
 * A base class for particleEmitters. Contains a timer which marks the effect as 'dead' when
 * it expires. Effects most naturally would extend this class to include additional
 * behaviour such as custom particle motion and rendering.
 */
public class Effect extends Visual {
    private static final Color EFFECT_COLOR = Color.RED;
    private Timer timer; // times the life duration of the effect, after which is is considered dead and is removed by the game.
    private boolean isDead = false;

    /***
     * x,y represent the center of the effect
     * @param x
     * @param y
     * @param duration
     */
    public Effect(float x, float y, int duration) {
        super(x,y,0,0); // there is no corresponding visual width/height for the effect,
        super.setColor(EFFECT_COLOR);
        timer = new Timer(duration, 1);
    }

    private void updateTimer(float deltaTime) {
        timer.update(deltaTime);
        if (timer.isTimerElapsed()) isDead = true;
    }

    /***
     * Make sure to call super.update() when overriding this method if you want to update the underlying effect timer.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        //updateTimer();
    }

    /***
     * The base effect is rendered by drawing a circle at the x,y location, representing the center of the effect.
     * @param sr
     */
    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        if (sr != null && sr.isDrawing()) {
            sr.setColor(super.getColor());
            sr.circle(position.x, position.y,10);
        }
    }

    public boolean isDead() { return isDead; }
    public long getElapsedTime() { return timer.getElapsedTime(); }
    public long getDuration() { return timer.getDuration(); }
    public Timer getTimer() { return timer; }
}
