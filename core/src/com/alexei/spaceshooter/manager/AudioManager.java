package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.SoundType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AudioManager {
    private final HashMap<SoundName, Sound> sounds = new HashMap<>();
    private final HashMap<SoundType, ArrayList<SoundName>> soundTypes = new HashMap<>();
    private final HashMap<SoundName, Music> musicMap = new HashMap<>();
    private boolean muted = false;

    public AudioManager() {
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopAllSounds();
            stopAllMusic();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /**
     * Load all sounds from the sounds/ folder. Called during LoadingScreen.
     */
    public void loadSounds(AssetManager assetManager) {
        String folder = "sounds";

        addSound(assetManager, SoundName.Alarm, folder + "/alarm.mp3");
        addSound(assetManager, SoundName.LaserShoot2, folder + "/laser_shoot2.mp3");
        addSound(assetManager, SoundName.Hit7, folder + "/hit7.mp3");
        addSound(assetManager, SoundName.Explode2, folder + "/explode2_2.mp3");
        addSound(assetManager, SoundName.Explode3, folder + "/explode3.mp3");
        addSound(assetManager, SoundName.Explode4, folder + "/explode4_4.mp3");
        addSound(assetManager, SoundName.Explode5, folder + "/explode5_5.mp3");
        addSound(assetManager, SoundName.Explode8, folder + "/explode8.mp3");
        addSound(assetManager, SoundName.LaserShoot, folder + "/laser_shoot.mp3");
        addSound(assetManager, SoundName.Rocket, folder + "/rocket.mp3");
        addSound(assetManager, SoundName.EndGame, folder + "/end_game.mp3");
        addSound(assetManager, SoundName.Laser, folder + "/laser.mp3");
        addSound(assetManager, SoundName.Explode, folder + "/explode.mp3");
        addSound(assetManager, SoundName.GetDamage, folder + "/get_damage.mp3");
        addSound(assetManager, SoundName.Tick, folder + "/tick.mp3");
        addSound(assetManager, SoundName.Ready, folder + "/ready.mp3");
        addSound(assetManager, SoundName.Go, folder + "/go.mp3");
        addSound(assetManager, SoundName.Warning, folder + "/warning.mp3");

        // sound categories
        ArrayList<SoundName> soundsHit = new ArrayList<>();
        ArrayList<SoundName> soundsExplode = new ArrayList<>();
        soundTypes.put(SoundType.Hit, soundsHit);
        soundTypes.put(SoundType.Explode, soundsExplode);
        soundsHit.add(SoundName.Hit7);
        soundsExplode.add(SoundName.Explode2);
    }

    /**
     * Load music from the music/ folder. Called during LoadingScreen.
     */
    public void loadMusic(AssetManager assetManager) {
        String musicFolder = "music";

        Music utMusic = Gdx.audio.newMusic(Gdx.files.internal(musicFolder + "/ut.mp3"));
        utMusic.setLooping(true);
        utMusic.setVolume(0.4f);
        musicMap.put(SoundName.Ut, utMusic);

        Music actionMusic = Gdx.audio.newMusic(Gdx.files.internal(musicFolder + "/action_music.mp3"));
        actionMusic.setLooping(true);
        actionMusic.setVolume(1f);
        musicMap.put(SoundName.ActionMusic, actionMusic);
    }

    private void addSound(AssetManager assetManager, SoundName name, String path) {
        sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
    }

    public void playSound(SoundName soundName) {
        playSound(soundName, false);
    }

    public void playSound(SoundName soundName, boolean loop) {
        if (muted) return;
        if (soundName != null && sounds.containsKey(soundName)) {
            Sound s = sounds.get(soundName);
            if (loop) {
                s.loop(1f);
            } else {
                s.play(1f);
            }
        }
    }

    public void stopSound(SoundName soundName) {
        if (soundName != null && sounds.containsKey(soundName)) {
            sounds.get(soundName).stop();
        }
    }

    public void playSoundType(SoundType soundType) {
        SoundName soundName = getRandomSoundName(soundType);
        playSound(soundName);
    }

    public SoundName getRandomSoundName(SoundType soundType) {
        if (soundTypes.containsKey(soundType)) {
            ArrayList<SoundName> soundNames = soundTypes.get(soundType);
            if (soundNames != null && !soundNames.isEmpty()) {
                return soundNames.get(MathUtils.random(0, soundNames.size() - 1));
            }
        }
        return null;
    }

    public void stopAllSounds() {
        for (Sound s : sounds.values()) {
            s.stop();
        }
    }

    public void playMusic(SoundName soundName) {
        if (muted) return;
        if (soundName != null && musicMap.containsKey(soundName)) {
            musicMap.get(soundName).play();
        }
    }

    public void stopMusic(SoundName soundName) {
        if (soundName != null && musicMap.containsKey(soundName)) {
            musicMap.get(soundName).stop();
        }
    }

    public void stopAllMusic() {
        for (Music m : musicMap.values()) {
            m.stop();
        }
    }

    public void dispose() {
        for (Sound s : sounds.values()) {
            s.dispose();
        }
        sounds.clear();
        for (Music m : musicMap.values()) {
            m.dispose();
        }
        musicMap.clear();
        soundTypes.clear();
    }
}
