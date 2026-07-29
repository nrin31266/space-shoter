package com.alexei.spaceshooter.effect;

/**
 * Created by Alex on 25/06/2015.
 */

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Represents a single particle emitted by a particle emitter. X,Y is the particle center.
 */
public class Particle extends Visual {

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
        sr.setColor(getColor());
        // render the particle oriented around x,y
//        sr.getTransformMatrix().idt();
//        sr.getTransformMatrix().setToRotation(0, 0, 1, orientation);
//        sr.getTransformMatrix().setTranslation(getX() - getWidth() / 2, getX() - getHeight() / 2, 0);
//
//        sr.rect(0, 0, getWidth(), getHeight());
        if (particleShape == ParticleEmitter.ParticleShape.Rect)
            sr.rect(getX(), getY(), getWidth(), getHeight());
        else if (particleShape == ParticleEmitter.ParticleShape.Circle)
            sr.circle(getX(), getY(), getWidth());
        else if (particleShape == ParticleEmitter.ParticleShape.Texture)
            sr.rect(getX(), getY(), getWidth(), getHeight()); // TODO: implement textures

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
