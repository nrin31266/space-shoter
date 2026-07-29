package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Alex on 25/06/2015.
 */
public class EffectSparks extends ParticleEmitter {

    public static final float PARTICLE_SIZE = 2;
    public static final float PARTICLE_SIZE_VARIATION = 1;

//    public static final float PARTICLE_DIRECTION_ANGLE_FROM = 0; //  247
//    public static final float PARTICLE_DIRECTION_ANGLE_TO = 0; // 293
//    public static final float PARTICLE_DIRECTION_ANGLE_VARIATION = 25;

    public static final float PARTICLE_SPEED = 350;
    public static final float PARTICLE_SPEED_VARIATION = 50;

    public static final float PARTICLE_ROTATION_SPEED = 0;
    public static final float PARTICLE_ROTATION_SPEED_VARIATION = 0;

    public static final Color PARTICLE_COLOR = new Color(Color.ORANGE);
    public static final float PARTICLE_COLOR_VARIATION = 0; // TODO

    public static final float PARTICLE_ALPHA = 1;
    public static final float PARTICLE_ALPHA_VARIATION = 0;

    public static final int PARTICLE_LIFE_PERIOD = 350;
    public static final int PARTICLE_LIFE_PERIOD_VARIATION = 50;

    public static final ParticleEmitter.ParticleShape PARTICLE_SHAPE = ParticleShape.Rect;

    public static final int EMISSION_EVENTS_IN_CYCLE = 1;
    public static final int EMISSION_AMOUNT_PER_EVENT = 20;
    public static final int EMISSION_EVENT_DELAY = 0;
    public static final int EMISSION_CYCLES = 1;
    public static final int EMISSION_CYCLE_DELAY = 0;

    public EffectSparks(float x, float y, float sparksDirection, float spreadAngle, int particleCount, Color color, float speed) {
        super(x, y);

        setParticleSize(PARTICLE_SIZE);
        setParticleSizeVariation(PARTICLE_SIZE_VARIATION);

        setParticleDirectionAngleFrom(sparksDirection);
        setParticleDirectionAngleTo(sparksDirection);
        setParticleDirectionAngleVariation(spreadAngle);

        setParticleSpeed(speed);
        setParticleSpeedVariation(PARTICLE_SPEED_VARIATION);

        setParticleRotationSpeed(PARTICLE_ROTATION_SPEED);
        setParticleRotationSpeedVariation(PARTICLE_ROTATION_SPEED_VARIATION);

        setColor(PARTICLE_COLOR);
        setParticleSize(PARTICLE_SIZE);

        getColor().a = PARTICLE_ALPHA;

        // particle life
        setParticleLifePeriod(PARTICLE_LIFE_PERIOD);
        setParticleLifePeriodVariation(PARTICLE_LIFE_PERIOD_VARIATION);

        // shape
        setParticleShape(PARTICLE_SHAPE);

        // emission timing variables
        setEmissionEventsInCycle(EMISSION_EVENTS_IN_CYCLE);
        setEmissionAmountPerEvent(particleCount);
        setEmissionEventDelay(EMISSION_EVENT_DELAY);
        setEmissionCycles(EMISSION_CYCLES);
        setEmissionCycleDelay(EMISSION_CYCLE_DELAY);

        // color
        setColor(color);
    }

    // assume the passed visual's velocity
    public void setVelocityFromVisual(Visual visual) {
        if (visual != null) this.setVelocity(visual.getVelocity());
    }

    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch){
        super.render(sr, batch);
    }

    public EffectSparks(float x, float y, float sparksDirection, float spreadAngle) {
        this(x, y, sparksDirection, spreadAngle, EMISSION_AMOUNT_PER_EVENT, PARTICLE_COLOR, PARTICLE_SPEED);
    }
}
