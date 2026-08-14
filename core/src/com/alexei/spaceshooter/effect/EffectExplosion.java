package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.entity.Visual;

/**
 * Created by Alex on 25/06/2015.
 */
public class EffectExplosion extends ParticleEmitter {

    public static final float PARTICLE_SIZE = 9;
    public static final float PARTICLE_SIZE_VARIATION = 5;

    public static final float PARTICLE_DIRECTION_ANGLE_FROM = 0;
    public static final float PARTICLE_DIRECTION_ANGLE_TO = 360;
    public static final float PARTICLE_DIRECTION_ANGLE_VARIATION = 0;

    public static final float PARTICLE_SPEED = 320;
    public static final float PARTICLE_SPEED_VARIATION = 140;

    public static final float PARTICLE_ROTATION_SPEED = 0;
    public static final float PARTICLE_ROTATION_SPEED_VARIATION = 0;

    /** Warm orange-gold explosion color — matches a realistic fire burst */
    public static final com.badlogic.gdx.graphics.Color PARTICLE_COLOR = new com.badlogic.gdx.graphics.Color(1.0f, 0.55f, 0.1f, 1f);
    public static final float PARTICLE_COLOR_VARIATION = 0; // TODO

    public static final float PARTICLE_ALPHA = 1;
    public static final float PARTICLE_ALPHA_VARIATION = 0;

    public static final int PARTICLE_LIFE_PERIOD = 500;
    public static final int PARTICLE_LIFE_PERIOD_VARIATION = 120;

    public static final ParticleEmitter.ParticleShape PARTICLE_SHAPE = ParticleShape.Rect;

    public static final int EMISSION_EVENTS_IN_CYCLE = 1;
    public static final int EMISSION_AMOUNT_PER_EVENT = 50;
    public static final int EMISSION_EVENT_DELAY = 0;
    public static final int EMISSION_CYCLES = 1;
    public static final int EMISSION_CYCLE_DELAY = 0;

    public EffectExplosion(float x, float y, Visual visual, int particleCount, float size, float speed) {
        super(x, y);

        // assume the passed visual's velocity
        if (visual != null) {
            this.setVelocity(visual.getVelocity());
        }

        setParticleSize(size);
        setParticleSizeVariation(PARTICLE_SIZE_VARIATION);

        setParticleDirectionAngleFrom(PARTICLE_DIRECTION_ANGLE_FROM);
        setParticleDirectionAngleTo(PARTICLE_DIRECTION_ANGLE_TO);
        setParticleDirectionAngleVariation(PARTICLE_DIRECTION_ANGLE_VARIATION);

        setParticleSpeed(speed);
        setParticleSpeedVariation(PARTICLE_SPEED_VARIATION);

        setParticleRotationSpeed(PARTICLE_ROTATION_SPEED);
        setParticleRotationSpeedVariation(PARTICLE_ROTATION_SPEED_VARIATION);

        setColor(PARTICLE_COLOR);
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
    }

    public EffectExplosion(float x, float y, Visual visual) {
        this(x, y, visual, EMISSION_AMOUNT_PER_EVENT, PARTICLE_SIZE, PARTICLE_SPEED);
    }

    public EffectExplosion(float x, float y) {
        this(x, y, null);
    }
}
