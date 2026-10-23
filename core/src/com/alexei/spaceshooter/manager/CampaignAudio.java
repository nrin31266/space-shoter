package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

public final class CampaignAudio {
    private final CampaignAssets assets;
    private final JsonValue palette;
    private final Sound[] sounds = new Sound[41];
    private final JsonValue[] cues = new JsonValue[41];
    private final float[] nextPlay = new float[41];
    private final AudioVoices pool = new AudioVoices();
    private final Sound[] voiceSounds = new Sound[16];
    private final long[] voiceHandles = new long[16];
    private final float[] durations = new float[41];
    public boolean gameplayPaused;
    public float musicDuck = 1;
    private Music current, outgoing;
    private float clock, fade = 1, duck = 1, musicGain, outgoingGain;
    private boolean suspended;
    public float musicVolume = .65f, sfxVolume = .8f;

    public CampaignAudio(CampaignAssets assets) {
        this.assets = assets;
        palette = assets.catalog.asset("Audio/AudioPalette.asset");
        for (JsonValue cue : palette.get("Cues")) {
            int id = cue.getInt("Id");
            cues[id] = cue;
            String path = assets.catalog.root.get("resources").getString(cue.get("Clip").getString("guid"));
            sounds[id] = assets.sound(path.substring(path.lastIndexOf('/') + 1, path.length() - 4));
            durations[id] = assets.catalog.root.get("audioSeconds").getFloat(path);
        }
    }

    public void cue(int id) {
        if (suspended || sfxVolume <= 0 || sounds[id] == null || clock < nextPlay[id]) return;
        JsonValue cue = cues[id];
        float variation = cue.getFloat("Variation");
        float pitch = 1 + MathUtils.random(-variation, variation);
        int slot = pool.acquire(id, cue.getInt("Tier"), cue.getInt("MaxVoices"), clock, durations[id] / pitch, cue.getFloat("Volume"));
        if (slot < 0) return;
        nextPlay[id] = clock + cue.getFloat("Interval");
        if (voiceSounds[slot] != null) voiceSounds[slot].stop(voiceHandles[slot]);
        pool.tick(clock, 0);
        voiceSounds[slot] = sounds[id];
        voiceHandles[slot] = sounds[id].play(pool.gain(slot, clock, gameplayPaused) * sfxVolume, pitch, 0);
    }

    public void music(String name) {
        Music target = assets.music(name);
        if (target == current) return;
        if (outgoing != null) outgoing.stop();
        outgoing = current; outgoingGain = musicGain;
        current = target;
        fade = 0;
        musicGain = palette.getFloat(name.equals("bgm-hub") ? "MusicLevel" : "GameplayMusicLevel");
        if (current != null) {
            current.setLooping(true);
            current.setVolume(0);
            if (!suspended) current.play();
        }
    }

    public void tick(float seconds) {
        clock += seconds;
        fade = Math.min(1, fade + seconds / .85f);
        pool.tick(clock, seconds);
        for (int i = 0; i < voiceSounds.length; i++) if (voiceSounds[i] != null)
            voiceSounds[i].setVolume(voiceHandles[i], pool.gain(i, clock, gameplayPaused) * sfxVolume);
        float targetDuck = Math.min(musicDuck, Math.min(gameplayPaused ? .3f : 1, pool.danger(clock) ? .45f : 1));
        duck += MathUtils.clamp(targetDuck - duck, -seconds * 5, seconds * 1.6f);
        if (current != null) current.setVolume(musicVolume * musicGain * duck * fade);
        if (outgoing != null) {
            outgoing.setVolume(musicVolume * outgoingGain * duck * (1 - fade));
            if (fade >= 1) { outgoing.stop(); outgoing = null; }
        }
    }

    public void clearGameplay() {
        for (int i = 0; i < 14; i++) {
            if (voiceSounds[i] != null) voiceSounds[i].stop(voiceHandles[i]);
            voiceSounds[i] = null;
        }
        pool.clearGameplay();
        for (int i = 0; i < nextPlay.length; i++) if (i < 32 || i > 34) nextPlay[i] = 0;
        gameplayPaused = false; musicDuck = 1;
    }

    public void suspend(boolean value) {
        suspended = value;
        if (value) {
            if (current != null) current.pause();
            if (outgoing != null) outgoing.pause();
            for (Sound sound : sounds) if (sound != null) sound.stop();
        } else {
            if (current != null) current.play();
            if (outgoing != null) outgoing.play();
        }
    }
}
