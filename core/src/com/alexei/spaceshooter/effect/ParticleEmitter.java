package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.entity.Visual;
import com.alexei.spaceshooter.utils.Timer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/**
 * Created by Alex on 19/06/2015.
 *
 * This class represents a particle emitter. It serves as a base class for many particle particleEmitters.
 * It has many configurable properties that determine the timing, speed, direction, randomization, colors and
 * rendering of particles. For example, particleEmitters like explosions, sparks, crashes, fire and smoke derive from this.
 */
public class ParticleEmitter extends Visual {
    private ArrayList<Particle> particles = new ArrayList<Particle>();

    private float particleSize;
    private float particleSizeVariation;

    private float particleDirectionAngleFrom;
    private float particleDirectionAngleTo;
    private float particleDirectionAngleVariation;

    private float particleSpeed;
    private float particleSpeedVariation;

    private float particleRotationSpeed; // degrees per second
    private float particleRotationSpeedVariation;

    private float particleColor; // TODO: animate Hue property
    private float particleColorVariation;

    private float particleAlpha;
    private float particleAlphaVariation;

    private int particleLifePeriod;
    private int particleLifePeriodVariation;

    // TODO: make it possible to attach particle emitters to every particle.
    // This is useful when we want fragments of an explosion to trail smoke particles
    //

    private ParticleShape particleShape = ParticleShape.Rect; // circle, square, texture

    // emission variables - these 3 vars describe the 'emission cycle'
    private int emissionEventsInCycle; // this is how many emissions to do in one 'emission cycle'.
    private int emissionAmountPerEvent; // this many particles are emitted in a single 'emission event'
    private int emissionEventDelay; // this is the length of time, in ms, between emissions, during one emission cycle.
    // NOTE: emitter lifetime = emissionPeriodsInCycle * emissionDelay
    // emission cycle control variables - these vars control' emission cycle count and frequency.
    // The emitter could be reset, after a delay. The number of times it could be reset is also controlled, unless it is set to reset indefinitely.
    private int emissionCycles; // this is how many 'emission cycles' the emitter can do before it sets its status to DEAD. If set to 0, emitter never dies and resets indefinitely.
    private int emissionCycleDelay; // the amount of time to wait before resetting the emitter, to perform another 'emission cycle'
    // when particles are emitted, they live a specific duration

    private int emissionEventsElapsed = 0; // the number of emission events performed while in a cycle.
    private int emissionCyclesElapsed = 0; // the number of emission cycles performed.
    private boolean isEmissionCycleRunning = true; // a flag for indicating whether the emitter is currently in the emission cycle phase. The other phase is during when the emission cycle is paused, due to a delay between cycles or when all cycles have run.
    private boolean isEmitterDead = false;

    // EXAMPLE of emission controls usage: The emission variables are such that it is easy to model useful behaviours.
    // For example: A canon that indefinitely shoots bursts of projectiles, let's say each burst consists of 5 projectiles
    // spaced out by 100ms between each projectile. After each burst, the emitter waits for 1sec and performs the burst again.
    // The values to make this happen are: (it is helpful to think of a burst as a single emission cycle.
    // emissionEventsInCycle = 5 // since it's a burst of 5, there are 5 emission events
    // emissionAmountPerEvent = 1 // one projectile is fired each emission even
    // emissionEventDelay = 100 // ms, in a single burst, a projectile is fired every 100 ms
    // emissionCycles = 0 // 0 means an unlimited number of bursts
    // emissionCycleDelay = 1000 // ms, there's a 1sec delay between each burst.

    private Timer timer;

    public ParticleEmitter(float x, float y) {
        super(x,y,1,1);
        timer = new Timer(0,1);
        //super.setSpeed(speed);
        //super.setColor(Color.MAGENTA);
    }

    public void doEmission() {

        for(int i=0;i<emissionAmountPerEvent;i++) {

            // randomize particle properties according to variation parameters specified for the emitter.
            Particle particle;

            // life
            if (particleLifePeriodVariation > particleLifePeriod) particleLifePeriod = particleLifePeriodVariation;
            int life = MathUtils.random(particleLifePeriod - particleLifePeriodVariation, particleLifePeriod + particleLifePeriodVariation);

            // size
            if (particleSizeVariation > particleSize) particleSize = particleSizeVariation;
            float size = MathUtils.random(particleSize - particleSizeVariation, particleSize + particleSizeVariation);

            particle = new Particle(getX(), getY(), size, size, life); // the initial particle position is x,y, which is the center of the emitter

            // angle of orientation
            // TODO

            // speed/direction
            if (particleSpeedVariation > particleSpeed) particleSpeed = particleSpeedVariation;
            float speed = MathUtils.random(particleSpeed - particleSpeedVariation, particleSpeed + particleSpeedVariation);
            particle.setVelocity(MathUtils.random(particleDirectionAngleFrom - particleDirectionAngleVariation, particleDirectionAngleTo + particleDirectionAngleVariation), speed);

            // color hue TODO
            particle.setColor(super.getColor());

            // color alpha

            // shape
            particle.setParticleShape(particleShape);

            particles.add(particle);

//            private float particleRotationSpeed; // degrees per second
//            private float particleRotationSpeedVariation;
//
//            private float particleColor; // TODO: animate Hue property
//            private float particleColorVariation;
//
//            private float particleAlpha;
//            private float particleAlphaVariation;
        }
    }

    /**
     * Essentially the update method controls the logic behind manipulation of all 'emission variables' and timing.
     */
    @Override
    public void update(float deltaTime) {
        if (isEmitterDead) return;

        super.update(deltaTime); // update the underlying visual
        timer.update(deltaTime);

        if (timer.isTimerElapsed()) {
            if (isEmissionCycleRunning && (emissionCyclesElapsed < emissionCycles || emissionCycles == 0)) { // emission event cycles are running and we haven't completed all cycles

                doEmission();
                if (emissionEventsElapsed < emissionEventsInCycle - 1) {
                    emissionEventsElapsed++;
                    // reset emitter to pause between emission events
                    timer.setDuration(emissionEventDelay);
                    timer.reset();
                } else if (emissionEventsElapsed == emissionEventsInCycle - 1) {
                    emissionEventsElapsed++;
                    // the last emission event of the cycle has finished.
                    // we're at the end of a cycle and have 2 options:
                    //  1. if there are more cycles, we set timer to delay between the next cycle,
                    //  2. if this was the last cycle, we set the emitter to 'dead'.
                    emissionCyclesElapsed++;
                    if (emissionCyclesElapsed < emissionCycles || emissionCycles == 0) {
                        // reset emitter to pause between emission cycles
                        isEmissionCycleRunning = false;
                        timer.setDuration(emissionCycleDelay);
                        timer.reset();
                    } else { // last cycle elapsed
                        //isEmitterDead = true;
                    }
                }
            } else if (!isEmissionCycleRunning) { // the cycles are paused and because we're here, we know that we need to start a new cycle
                isEmissionCycleRunning = true;
                doEmission();
                timer.setDuration(emissionEventDelay);
                timer.reset();
            }
        }

        // update particles
        for (int i=particles.size()-1;i>=0;i--) {
            Particle p = particles.get(i);

            p.update(deltaTime); // update particle position and life

            if (p.isDead()) {
                particles.remove(i);
                continue;
            }

            // TODO: update the particle's properties according to the emitter's properties: directionChange, speedChange, sizeChange, hueChange, alphaChange, orientationChange

            // animate the particle alpha by fading it out as the timer elapses
            //Color c = p.getColor();
           // c.a = 1 - (super.getElapsedTime() / (float) super.getDuration());

            // update the particles position relative to the emitter (in case the emitter is moving, we want the particles to move with it)
            //p.setX(p.getX() + getVelocity().x);
            //p.setY(p.getY() + getVelocity().y);
        }

        // check if we need to mark emitter as dead (we do so when there are no more emission cycles left and all particles have died.
        if (emissionCycles != 0 && emissionCyclesElapsed == emissionCycles && particles.size() == 0) {
            isEmitterDead = true;
        }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        if (isEmitterDead) return;
        // render the particles comprising this explosion
        for(Particle p : particles) {
            // draw the particle
            p.render(sr, batch);
        }
    }

    public boolean isDead() {
        return isEmitterDead;
    }

    public void reset() {
        isEmitterDead = false;
        isEmissionCycleRunning = true;
        emissionCyclesElapsed = 0;
        emissionEventsElapsed = 0;
        timer.reset();
        timer.setDuration(0);
    }

    // GETTERS AND SETTERS

    public ArrayList<Particle> getParticles() { return particles; }

    public float getParticleSize() {
        return particleSize;
    }

    public void setParticleSize(float particleSize) {
        this.particleSize = particleSize;
    }

    public float getParticleSizeVariation() {
        return particleSizeVariation;
    }

    public void setParticleSizeVariation(float particleSizeVariation) {
        this.particleSizeVariation = particleSizeVariation;
    }

    public float getParticleDirectionAngleFrom() {
        return particleDirectionAngleFrom;
    }

    public void setParticleDirectionAngleFrom(float particleDirectionAngleFrom) {
        this.particleDirectionAngleFrom = particleDirectionAngleFrom;
    }

    public float getParticleDirectionAngleTo() {
        return particleDirectionAngleTo;
    }

    public void setParticleDirectionAngleTo(float particleDirectionAngleTo) {
        this.particleDirectionAngleTo = particleDirectionAngleTo;
    }

    public float getParticleDirectionAngleVariation() {
        return particleDirectionAngleVariation;
    }

    public void setParticleDirectionAngleVariation(float particleDirectionAngleVariation) {
        this.particleDirectionAngleVariation = particleDirectionAngleVariation;
    }

    public float getParticleSpeed() {
        return particleSpeed;
    }

    public void setParticleSpeed(float particleSpeed) {
        this.particleSpeed = particleSpeed;
    }

    public float getParticleSpeedVariation() {
        return particleSpeedVariation;
    }

    public void setParticleSpeedVariation(float particleSpeedVariation) {
        this.particleSpeedVariation = particleSpeedVariation;
    }

    public float getParticleRotationSpeed() {
        return particleRotationSpeed;
    }

    public void setParticleRotationSpeed(float particleRotationSpeed) {
        this.particleRotationSpeed = particleRotationSpeed;
    }

    public float getParticleRotationSpeedVariation() {
        return particleRotationSpeedVariation;
    }

    public void setParticleRotationSpeedVariation(float particleRotationSpeedVariation) {
        this.particleRotationSpeedVariation = particleRotationSpeedVariation;
    }

    public float getParticleColor() {
        return particleColor;
    }

    public void setParticleColor(float particleColor) {
        this.particleColor = particleColor;
    }

    public float getParticleColorVariation() {
        return particleColorVariation;
    }

    public void setParticleColorVariation(float particleColorVariation) {
        this.particleColorVariation = particleColorVariation;
    }

    public float getParticleAlpha() {
        return particleAlpha;
    }

    public void setParticleAlpha(float particleAlpha) {
        this.particleAlpha = particleAlpha;
    }

    public float getParticleAlphaVariation() {
        return particleAlphaVariation;
    }

    public void setParticleAlphaVariation(float particleAlphaVariation) {
        this.particleAlphaVariation = particleAlphaVariation;
    }

    public int getEmissionEventsInCycle() {
        return emissionEventsInCycle;
    }

    public void setEmissionEventsInCycle(int emissionEventsInCycle) {
        this.emissionEventsInCycle = emissionEventsInCycle;
    }

    public int getEmissionAmountPerEvent() {
        return emissionAmountPerEvent;
    }

    public void setEmissionAmountPerEvent(int emissionAmountPerEvent) {
        this.emissionAmountPerEvent = emissionAmountPerEvent;
    }

    public int getEmissionEventDelay() {
        return emissionEventDelay;
    }

    public void setEmissionEventDelay(int emissionEventDelay) {
        this.emissionEventDelay = emissionEventDelay;
    }

    public int getEmissionCycles() {
        return emissionCycles;
    }

    public void setEmissionCycles(int emissionCycles) {
        this.emissionCycles = emissionCycles;
    }

    public int getEmissionCycleDelay() {
        return emissionCycleDelay;
    }

    public void setEmissionCycleDelay(int emissionCycleDelay) {
        this.emissionCycleDelay = emissionCycleDelay;
    }

    public int getParticleLifePeriod() {
        return particleLifePeriod;
    }

    public void setParticleLifePeriod(int particleLifePeriod) {
        this.particleLifePeriod = particleLifePeriod;
    }

    public int getParticleLifePeriodVariation() {
        return particleLifePeriodVariation;
    }

    public void setParticleLifePeriodVariation(int particleLifePeriodVariation) {
        this.particleLifePeriodVariation = particleLifePeriodVariation;
    }

    public boolean isEmitterDead() {
        return isEmitterDead;
    }

    public boolean isEmissionCycleRunning() {
        return isEmissionCycleRunning;
    }

    public int getEmissionCyclesElapsed() {
        return emissionCyclesElapsed;
    }

    public int getEmissionEventsElapsed() {
        return emissionEventsElapsed;
    }

    public ParticleShape getParticleShape() {
        return particleShape;
    }

    public void setParticleShape(ParticleShape particleShape) {
        this.particleShape = particleShape;
    }

    public enum ParticleShape {
        Circle, Rect, Texture
    }
}
