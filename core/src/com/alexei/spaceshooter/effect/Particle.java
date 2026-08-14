package com.alexei.spaceshooter.effect;

/**
 * Created by Alex on 25/06/2015.
 */

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Represents a single particle emitted by a particle emitter. X,Y is the particle center.
 */
public class Particle extends Visual {

    /** Shared 1x1 white texture so all particles batch into a single draw call. */
    private static Texture whiteTex;
    private static Texture getWhiteTex() {
        if (whiteTex == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            whiteTex = new Texture(p);
            p.dispose();
        }
        return whiteTex;
    }

    private float life; // in ms
    private float lifeElapsed; // in ms
    private float orientation;
    private ParticleEmitter.ParticleShape particleShape = ParticleEmitter.ParticleShape.Rect;

    public Particle(float x, float y, float width, float height, float life) {
        super(x, y, width, height);
        this.life = life;
    }

    @Override
    public void update(float deltaTime) {
        if (isDead()) return;
        super.update(deltaTime);
        lifeElapsed += deltaTime;
        if (lifeElapsed > life) lifeElapsed = life;
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        if (isDead()) return;

        // Batched path: all particles share one white texture -> single draw call,
        // and they fade out over their lifetime instead of popping out of existence.
        if (batch != null && batch.isDrawing()) {
            float fade = 1f - getPercentLifeElapsed();
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * fade);
            if (particleShape == ParticleEmitter.ParticleShape.Circle) {
                batch.draw(getWhiteTex(), getX() - getWidth(), getY() - getHeight(),
                        getWidth() * 2, getHeight() * 2);
            } else {
                batch.draw(getWhiteTex(), getX(), getY(), getWidth(), getHeight());
            }
            batch.setColor(Color.WHITE);
            return;
        }

        // ShapeRenderer fallback
        if (sr != null && sr.isDrawing()) {
            sr.setColor(getColor());
            if (particleShape == ParticleEmitter.ParticleShape.Rect)
                sr.rect(getX(), getY(), getWidth(), getHeight());
            else if (particleShape == ParticleEmitter.ParticleShape.Circle)
                sr.circle(getX(), getY(), getWidth());
            else if (particleShape == ParticleEmitter.ParticleShape.Texture)
                sr.rect(getX(), getY(), getWidth(), getHeight());
        }
    }

    public float getPercentLifeElapsed() { return lifeElapsed / life; }

    public boolean isDead() {
        return lifeElapsed >= life;
    }

    // TODO: release texture references
    public void dispose() { }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public ParticleEmitter.ParticleShape getParticleShape() {
        return particleShape;
    }

    public void setParticleShape(ParticleEmitter.ParticleShape particleShape) {
        this.particleShape = particleShape;
    }
}
