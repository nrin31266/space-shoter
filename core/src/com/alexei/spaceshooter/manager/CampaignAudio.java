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
    private final long[][] voices = new long[41][4];
    private final int[] voiceCursor = new int[41];
    private Music current, outgoing;
    private float clock, fade = 1, duck, musicGain;
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
            for (int i = 0; i < 4; i++) voices[id][i] = -1;
        }
    }

    public void cue(int id) {
        if (suspended || sfxVolume <= 0 || sounds[id] == null || clock < nextPlay[id]) return;
        JsonValue cue = cues[id];
        nextPlay[id] = clock + cue.getFloat("Interval");
        int slot = voiceCursor[id]++ % cue.getInt("MaxVoices");
        if (voices[id][slot] >= 0) sounds[id].stop(voices[id][slot]);
        float variation = cue.getFloat("Variation");
        voices[id][slot] = sounds[id].play(cue.getFloat("Volume") * sfxVolume,
                1 + MathUtils.random(-variation, variation), 0);
        if (cue.getInt("Tier") <= 1) duck = .35f;
    }

    public void music(String name) {
        Music target = assets.music(name);
        if (target == current) return;
        if (outgoing != null) outgoing.stop();
        outgoing = current;
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
        fade = Math.min(1, fade + seconds / .8f);
        duck = Math.max(0, duck - seconds);
        float volume = musicVolume * musicGain * (duck > 0 ? .55f : 1);
        if (current != null) current.setVolume(volume * fade);
        if (outgoing != null) {
            outgoing.setVolume(volume * (1 - fade));
            if (fade >= 1) { outgoing.stop(); outgoing = null; }
        }
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
