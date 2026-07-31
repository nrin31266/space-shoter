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
    private final HashMap<SoundName, Sound>                    sounds     = new HashMap<>();
    private final HashMap<SoundType, ArrayList<SoundName>>    soundTypes = new HashMap<>();
    private final HashMap<SoundName, Music>                    musicMap   = new HashMap<>();
    private float volume = 1.0f; // 0.0f to 1.0f
    private com.badlogic.gdx.Preferences prefs;

    /**
     * Whether background music is muted. Completely independent of the volume slider.
     * Toggled via setMusicMuted(). Persisted in Preferences under key "musicMuted".
     * Sound effects (SFX) are NOT affected by this flag.
     */
    private boolean isMusicMuted = false;
    private static final String PREF_MUSIC_MUTED = "musicMuted";

    /**
     * Minimum time (ms) that must elapse between two plays of the same sound.
     * Prevents audio clipping when weapons fire rapidly.
     */
    private static final long DEFAULT_SOUND_MIN_INTERVAL_MS = 150L;
    private static final long LASER_MIN_INTERVAL_MS         = 200L; // laser fires every 180ms; don't pile up

    /** Tracks the last System.currentTimeMillis() each sound was played. */
    private final HashMap<SoundName, Long> lastPlayTime = new HashMap<>();

    /** Per-sound minimum interval overrides. */
    private final HashMap<SoundName, Long> soundMinIntervals = new HashMap<>();

    public AudioManager() {
        initPrefs();
    }

    public void initPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("SpaceShooter");
            volume = prefs.getFloat("volume", 1.0f);
            isMusicMuted = prefs.getBoolean(PREF_MUSIC_MUTED, false);
        }
    }

    public void setVolume(float volume) {
        this.volume = MathUtils.clamp(volume, 0f, 1f);
        if (prefs != null) {
            prefs.putFloat("volume", this.volume);
            prefs.flush();
        }
        // Update all running music
        for (SoundName name : musicMap.keySet()) {
            Music m = musicMap.get(name);
            if (m.isPlaying()) {
                m.setVolume(this.volume * (name == SoundName.Ut ? 0.4f : 1f));
            }
        }
    }

    public float getVolume() {
        return volume;
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
        ArrayList<SoundName> soundsHit     = new ArrayList<>();
        ArrayList<SoundName> soundsExplode = new ArrayList<>();
        soundTypes.put(SoundType.Hit, soundsHit);
        soundTypes.put(SoundType.Explode, soundsExplode);
        soundsHit.add(SoundName.Hit7);
        soundsExplode.add(SoundName.Explode2);

        // Per-sound throttle overrides (ms between plays)
        // Laser weapons fire 5+ times/second; without throttle they create audio clipping.
        // Intervals are set generously to prevent simultaneous instance pileup across
        // multiple enemies firing the same sound in the same frame.
        soundMinIntervals.put(SoundName.LaserShoot2, 250L);  // sniper beam (ShipC)
        soundMinIntervals.put(SoundName.LaserShoot,  LASER_MIN_INTERVAL_MS);
        soundMinIntervals.put(SoundName.Laser,        350L);  // enemy energy ball & spread (ShipB/D/Boss) — many enemies fire simultaneously
        soundMinIntervals.put(SoundName.Explode5,     150L);  // small explosion (EnemyShipA) — many can die at once
        soundMinIntervals.put(SoundName.Explode2,     180L);  // medium explosion
        soundMinIntervals.put(SoundName.Hit7,         120L);  // item pick-up
        soundMinIntervals.put(SoundName.Explode,      200L);  // generic explosion
        soundMinIntervals.put(SoundName.Explode3,     180L);
        soundMinIntervals.put(SoundName.Explode4,     180L);
        soundMinIntervals.put(SoundName.Explode8,     180L);
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
        if (volume <= 0f) return;
        if (soundName == null || !sounds.containsKey(soundName)) return;

        // Throttle: skip if this sound played too recently
        long now      = System.currentTimeMillis();
        long minGap   = soundMinIntervals.containsKey(soundName)
                        ? soundMinIntervals.get(soundName)
                        : DEFAULT_SOUND_MIN_INTERVAL_MS;
        Long lastTime = lastPlayTime.get(soundName);
        if (lastTime != null && (now - lastTime) < minGap) return; // too soon — skip

        lastPlayTime.put(soundName, now);
        Sound s = sounds.get(soundName);
        if (loop) s.loop(0.75f * volume);
        else      s.play(0.75f * volume);
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
        if (volume <= 0f) return;
        if (isMusicMuted) return; // Part B: honour music mute flag regardless of volume
        if (soundName != null && musicMap.containsKey(soundName)) {
            Music m = musicMap.get(soundName);
            m.setVolume(this.volume * (soundName == SoundName.Ut ? 0.4f : 1f));
            m.play();
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

    // ─── Music Mute API (Part B) ──────────────────────────────────────

    /**
     * Toggle background music on or off. Completely independent of the volume slider.
     * When muting, immediately stops any playing music track.
     * When unmuting, does NOT auto-restart music — caller must call playMusic() if needed.
     * State is persisted in Preferences under key "musicMuted".
     *
     * @param muted true to silence music, false to allow music
     */
    public void setMusicMuted(boolean muted) {
        this.isMusicMuted = muted;
        if (prefs != null) {
            prefs.putBoolean(PREF_MUSIC_MUTED, muted);
            prefs.flush();
        }
        if (muted) {
            stopAllMusic();
        }
    }

    /** @return true if background music is currently muted. */
    public boolean isMusicMuted() {
        return isMusicMuted;
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
