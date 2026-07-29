package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Alex on 26/06/2015.
 *
 * This spawner simply returns multiple instances of SparkEffect.
 * Each spark effect is oriented according to the given directions array and has the given particle spread angle.
 */
public class EffectSpawrksSpawner {
    public static ArrayList<Visual> makeSparks(Visual visual, float x, float y, float[] sparksDirections, float[] spreadAngles) {
        ArrayList<Visual> effects = new ArrayList<Visual>();

        for (int i = 0; i < sparksDirections.length; i++) {
            EffectSparks s = new EffectSparks(x,y,
                    sparksDirections[i % sparksDirections.length],
                    spreadAngles[i % spreadAngles.length]);

            effects.add(s);
            if (visual != null) s.setVelocityFromVisual(visual);
        }

        return effects;
    }

    public static ArrayList<Visual> makeSparks(Visual visual, float x, float y, float[] sparksDirections, float[] spreadAngles, int[] particleCount, Color[] colors, float[] speed) {
        ArrayList<Visual> effects = new ArrayList<Visual>();

        for (int i = 0; i < sparksDirections.length; i++) {
            EffectSparks s = new EffectSparks(x,y,
                    sparksDirections[i % sparksDirections.length],
                    spreadAngles[i % spreadAngles.length],
                    particleCount[i % particleCount.length],
                    colors[i % colors.length],
                    speed[i % speed.length]);

            effects.add(s);
            if (visual != null) s.setVelocityFromVisual(visual);
        }

        return effects;
    }
}
