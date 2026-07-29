package com.alexei.spaceshooter;

import com.alexei.spaceshooter.entity.*;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.*;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/**
 * Static bridge class providing backward-compatible access to shared game resources.
 * GamePlayScreen wires the actual instances via setters in its show() method.
 *
 * This class is NOT an ApplicationAdapter — MainGame is the entry point.
 */
public class SpaceShooter {

    public static final float FPS = 60;
    public static final float GROUND_SCROLL_SPEED = 50;

    // --- items bridge (used by Unit.dropStars) ---
    public static ArrayList<Item> items;

    public static void setActiveItemsList(ArrayList<Item> list) {
        items = list;
    }

    // --- enemies bridge (used by Projectile homing) ---
    private static ArrayList<Unit> activeEnemiesRef;

    public static void setActiveEnemiesList(ArrayList<Unit> list) {
        activeEnemiesRef = list;
    }

    public static Visual acquireTarget() {
        if (activeEnemiesRef == null || activeEnemiesRef.isEmpty()) return null;
        return activeEnemiesRef.get(MathUtils.random(0, activeEnemiesRef.size() - 1));
    }

    public static boolean isTargetDead(Visual visual) {
        if (activeEnemiesRef == null) return true;
        return activeEnemiesRef.indexOf(visual) == -1;
    }

    // --- audio bridge (used by Item, Unit, Ship) ---
    private static AudioManager staticAudioManager;

    public static void setStaticAudioManager(AudioManager am) {
        staticAudioManager = am;
    }

    public static void playSound(SoundName soundName) {
        if (staticAudioManager != null) staticAudioManager.playSound(soundName);
    }

    public static void playSound(SoundName soundName, boolean loop) {
        if (staticAudioManager != null) staticAudioManager.playSound(soundName, loop);
    }

    public static void stopSound(SoundName soundName) {
        if (staticAudioManager != null) staticAudioManager.stopSound(soundName);
    }

    public static void playSoundType(SoundType soundType) {
        if (staticAudioManager != null) staticAudioManager.playSoundType(soundType);
    }

    public static SoundName getRandomSoundName(SoundType soundType) {
        if (staticAudioManager != null) return staticAudioManager.getRandomSoundName(soundType);
        return null;
    }

    public static void stopAllSounds() {
        if (staticAudioManager != null) staticAudioManager.stopAllSounds();
    }

    public static void playMusic(SoundName soundName) {
        if (staticAudioManager != null) staticAudioManager.playMusic(soundName);
    }

    public static void stopMusic(SoundName soundName) {
        if (staticAudioManager != null) staticAudioManager.stopMusic(soundName);
    }

    public static void stopAllMusic() {
        if (staticAudioManager != null) staticAudioManager.stopAllMusic();
    }
}
