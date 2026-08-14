#!/usr/bin/env python3
"""
SPACE SHOOTER — AUDIO GENERATOR
===============================
Generates clean, original (public-domain, self-made) music loops and sound
effects as .wav files. No third-party samples are used — everything is
synthesized with numpy, so there are no licensing issues and no low-quality
source files.

Tracks:
  music/action_music.ogg  — upbeat but clean synthwave loop for gameplay
  music/ut.ogg            — calm ambient loop for menus
  sounds/powerup.ogg      — rising arpeggio (weapon upgrade pickup)
  sounds/pickup.ogg       — bright crystal chime (currency pickup)
  sounds/wave_start.ogg   — whoosh/announcement (wave start)
  sounds/wave_clear.ogg   — bright fanfare (wave clear)
  sounds/boss_warning.ogg — ominous horn (boss entrance)

The .wav files are then transcoded to .ogg with ffmpeg. Run:
    python3 tools/gen_audio.py
"""

import os
import subprocess
import wave

import numpy as np

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
MUSIC_DIR = os.path.join(REPO_ROOT, "assets", "music")
SOUND_DIR = os.path.join(REPO_ROOT, "assets", "sounds")
SR = 44100

os.makedirs(MUSIC_DIR, exist_ok=True)
os.makedirs(SOUND_DIR, exist_ok=True)


# ---------------------------------------------------------------------------
# Synth helpers
# ---------------------------------------------------------------------------
def osc(freq, dur, kind="sine", sr=SR):
    t = np.arange(int(dur * sr)) / sr
    ph = 2 * np.pi * freq * t
    if kind == "sine":
        return np.sin(ph)
    if kind == "tri":
        return 2 / np.pi * np.arcsin(np.sin(ph))
    if kind == "saw":
        return 2 * ((freq * t) % 1.0) - 1
    if kind == "square":
        return np.sign(np.sin(ph))
    return np.sin(ph)


def env_adsr(n, a=0.005, d=0.1, s=0.6, r=0.1, sr=SR):
    """ADSR envelope over n samples (attack, decay, sustain, release in sec)."""
    e = np.ones(n)
    a_n = int(a * sr)
    d_n = int(d * sr)
    r_n = int(r * sr)
    # attack
    if a_n > 0:
        take = min(a_n, n)
        e[:take] = np.linspace(0, 1, take)
    # decay
    start = min(a_n, n)
    rem = n - start
    take = min(d_n, rem)
    if take > 0:
        e[start:start + take] = np.linspace(1, s, take)
        # hold at sustain for the rest between decay and release
        end_hold = max(start + take, n - r_n)
        if end_hold > start + take:
            e[start + take:end_hold] = s
    # release
    if r_n > 0:
        take = min(r_n, n)
        e[-take:] *= np.linspace(1, 0, take)
    return e


def lowpass(x, alpha=0.15):
    """Simple one-pole lowpass (feedback form) — cheap and clean enough."""
    y = np.empty_like(x)
    acc = 0.0
    for i in range(len(x)):
        acc += alpha * (x[i] - acc)
        y[i] = acc
    return y


def normalize(x, peak=0.85):
    m = np.max(np.abs(x)) if len(x) else 1.0
    if m < 1e-9:
        return x
    return x * (peak / m)


def write_wav(path, data):
    """data: float32 in [-1,1], stereo interleaved (n,2) or mono (n,)."""
    data = np.asarray(data, dtype=np.float32)
    if data.ndim == 1:
        data = np.stack([data, data], axis=1)
    pcm = (np.clip(data, -1, 1) * 32767).astype(np.int16)
    with wave.open(path, "wb") as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())


def oggify(wav_path, ogg_path, bitrate="160k"):
    subprocess.run(["ffmpeg", "-y", "-v", "error", "-i", wav_path,
                    "-c:a", "libvorbis", "-q:a", "4", ogg_path], check=True)
    os.remove(wav_path)


def render_to(name, out_dir, samples, normalize_peak=0.85):
    wav = os.path.join(out_dir, name + ".wav")
    ogg = os.path.join(out_dir, name + ".ogg")
    write_wav(wav, normalize(samples, normalize_peak))
    oggify(wav, ogg)
    print("  wrote %-30s" % (os.path.relpath(ogg, REPO_ROOT)))


def mix(buf, seg, t0):
    """Add seg into buf starting at sample t0, clipping to buffer bounds."""
    end = min(t0 + len(seg), len(buf))
    if t0 < 0:
        seg = seg[-t0:]
        t0 = 0
    if t0 >= len(buf) or end <= t0:
        return
    buf[t0:end] += seg[:end - t0]


def midi_to_freq(m):
    return 440.0 * 2 ** ((m - 69) / 12.0)


# ---------------------------------------------------------------------------
# Music: action (gameplay) — energetic synthwave loop, 128 BPM, A minor
# ---------------------------------------------------------------------------
def synth_kick(beat, sr=SR):
    n = int(0.16 * sr)
    t = np.arange(n) / sr
    f = 140 * np.exp(-t * 28) + 45
    ph = 2 * np.pi * np.cumsum(f) / sr
    body = np.sin(ph) * np.exp(-t * 22)
    click = np.random.default_rng(0).normal(0, 1, n) * np.exp(-t * 180) * 0.4
    return body + click


def synth_bass(freq, dur, sr=SR):
    n = int(dur * sr)
    t = np.arange(n) / sr
    saw = 2 * ((freq * t) % 1.0) - 1
    saw += 0.5 * (2 * ((freq * 2 * t) % 1.0) - 1)
    saw = lowpass(saw, 0.12)
    e = env_adsr(n, a=0.004, d=0.25, s=0.7, r=0.02)
    return saw * e


def synth_arp(freq, dur, sr=SR, kind="tri"):
    n = int(dur * sr)
    o = osc(freq, dur, kind, sr)
    e = env_adsr(n, a=0.003, d=0.2, s=0.8, r=0.12)
    return o * e


def synth_hat(dur, sr=SR):
    n = int(dur * sr)
    rng = np.random.default_rng(1)
    nz = rng.normal(0, 1, n)
    nz = lowpass(nz, 0.3)
    e = np.exp(-np.arange(n) / sr * 60)
    return nz * e * 0.5


def synth_pad(chords, dur, sr=SR):
    """Soft detuned pad over a chord progression."""
    total = int(dur * sr)
    out = np.zeros(total)
    for i, freqs in enumerate(chords):
        seg_n = int((dur / len(chords)) * sr)
        seg = np.zeros(seg_n)
        for f in freqs:
            seg += osc(f, seg_n / sr, "sine") * 0.25
            seg += osc(f * 1.005, seg_n / sr, "sine") * 0.25
        # slow attack/release per segment
        a = int(0.4 * sr)
        r = int(0.4 * sr)
        if seg_n > a + r:
            seg[:a] *= np.linspace(0, 1, a)
            seg[-r:] *= np.linspace(1, 0, r)
        out[i * seg_n:(i + 1) * seg_n] = seg
    return out


def gen_action_music():
    bpm = 128
    beat = 60.0 / bpm
    bars = 8
    beats_total = bars * 4
    total_dur = beats_total * beat
    n = int(total_dur * SR)
    out = np.zeros(n)
    rng = np.random.default_rng(2)

    # A minor progression: Am F C G (one chord per 2 bars)
    chords = [
        [57, 60, 64],   # A minor
        [53, 57, 60],   # F
        [48, 52, 55],   # C
        [55, 59, 62],   # G
    ]
    bass_seq = [33, 33, 33, 33, 29, 29, 29, 29,
                24, 24, 24, 24, 31, 31, 31, 31]

    pad = synth_pad([c for c in chords for _ in range(2)], total_dur)
    pad = lowpass(pad, 0.06) * 0.5
    out += pad

    for b in range(beats_total):
        t0 = int(b * beat * SR)
        # Kick on every beat (four-on-the-floor)
        mix(out, synth_kick(1, SR) * 0.9, t0)
        # Hat on offbeats
        mix(out, synth_hat(0.08, SR) * 0.5, t0 + int(0.5 * beat * SR))

        # Bass on every beat (root note pattern)
        note = bass_seq[b % len(bass_seq)]
        mix(out, synth_bass(midi_to_freq(note), beat * 0.9, SR) * 0.55, t0)

        # Arp: 4 sixteenths per beat, chord tones
        chord = chords[(b // 8) % 4]
        for s in range(4):
            t1 = t0 + int(s * 0.25 * beat * SR)
            note = chord[(b + s) % 3] + 12
            mix(out, synth_arp(midi_to_freq(note), 0.18, SR) * 0.16, t1)

    # subtle stereo width: slight detune on right channel
    left = out
    right = np.roll(out, 30)
    return np.stack([left, right], axis=1)


# ---------------------------------------------------------------------------
# Music: ut (menu) — calm ambient
# ---------------------------------------------------------------------------
def gen_ut_music():
    total_dur = 32.0
    n = int(total_dur * SR)
    out = np.zeros(n)

    chords = [
        [57, 60, 64, 67],  # Am7
        [53, 57, 60, 64],  # Fmaj7
        [48, 52, 55, 59],  # C
        [55, 59, 62, 65],  # G
    ]
    pad = synth_pad([c for c in chords for _ in range(2)], total_dur)
    pad = lowpass(pad, 0.05) * 0.6
    out += pad

    # Sparse bell arpeggio every half bar
    rng = np.random.default_rng(3)
    half_bar = 2.0  # seconds
    t = 0.0
    idx = 0
    while t < total_dur:
        chord = chords[idx % 4]
        note = chord[rng.integers(0, len(chord))] + 12
        dur = 0.9
        bell = osc(midi_to_freq(note), dur, "sine") * env_adsr(int(dur * SR), a=0.01, d=0.4, s=0.5, r=0.5)
        bell += 0.3 * osc(midi_to_freq(note) * 2.0, dur, "sine") * env_adsr(int(dur * SR), a=0.01, d=0.4, s=0.5, r=0.5)
        mix(out, bell * 0.35, int(t * SR))
        t += half_bar
        idx += 1

    return np.stack([out, np.roll(out, 40)], axis=1)


# ---------------------------------------------------------------------------
# SFX
# ---------------------------------------------------------------------------
def gen_powerup():
    """Rising major arpeggio — weapon upgrade."""
    notes = [60, 64, 67, 72, 76]
    total = np.zeros(int(0.7 * SR))
    t = 0.0
    for i, m in enumerate(notes):
        f = midi_to_freq(m)
        dur = 0.22
        seg = osc(f, dur, "tri") * env_adsr(int(dur * SR), a=0.005, d=0.12, s=0.7, r=0.06)
        seg += 0.4 * osc(f * 2, dur, "sine") * env_adsr(int(dur * SR), a=0.005, d=0.1, s=0.6, r=0.05)
        mix(total, seg * 0.5, int(t * SR))
        t += 0.12
    return total


def gen_pickup():
    """Bright short crystal chime."""
    f = midi_to_freq(88)
    n = int(0.3 * SR)
    chime = osc(f, 0.3, "sine") * env_adsr(n, a=0.002, d=0.2, s=0.3, r=0.25)
    chime += 0.5 * osc(f * 1.5, 0.3, "sine") * env_adsr(n, a=0.002, d=0.2, s=0.3, r=0.25)
    return chime


def gen_wave_start():
    """Whoosh up — wave announcement."""
    n = int(0.5 * SR)
    rng = np.random.default_rng(4)
    nz = lowpass(rng.normal(0, 1, n), 0.12)
    e = np.linspace(0, 1, n) ** 2 * np.exp(-np.arange(n) / SR * 4)
    return nz * e * 0.7


def gen_wave_clear():
    """Bright two-note fanfare."""
    seq = [(72, 0.0), (76, 0.12), (79, 0.24)]
    n = int(0.9 * SR)
    total = np.zeros(n)
    for m, t in seq:
        f = midi_to_freq(m)
        dur = 0.4
        seg = osc(f, dur, "tri") * env_adsr(int(dur * SR), a=0.01, d=0.15, s=0.8, r=0.2)
        seg += 0.3 * osc(f * 2, dur, "sine") * env_adsr(int(dur * SR), a=0.01, d=0.15, s=0.8, r=0.2)
        mix(total, seg * 0.5, int(t * SR))
    return total


def gen_boss_warning():
    """Ominous low horn."""
    n = int(1.0 * SR)
    f = midi_to_freq(38)
    horn = osc(f, 1.0, "saw") * env_adsr(n, a=0.05, d=0.3, s=0.8, r=0.3)
    horn += 0.5 * osc(f * 1.5, 1.0, "saw") * env_adsr(n, a=0.05, d=0.3, s=0.8, r=0.3)
    horn = lowpass(horn, 0.08) * 0.8
    vib = 1 + 0.01 * np.sin(2 * np.pi * 4 * np.arange(n) / SR)
    return horn * vib


def main():
    print("Generating audio ...")
    render_to("action_music", MUSIC_DIR, gen_action_music(), normalize_peak=0.8)
    render_to("ut", MUSIC_DIR, gen_ut_music(), normalize_peak=0.6)
    render_to("powerup", SOUND_DIR, gen_powerup(), normalize_peak=0.8)
    render_to("pickup", SOUND_DIR, gen_pickup(), normalize_peak=0.7)
    render_to("wave_start", SOUND_DIR, gen_wave_start(), normalize_peak=0.7)
    render_to("wave_clear", SOUND_DIR, gen_wave_clear(), normalize_peak=0.8)
    render_to("boss_warning", SOUND_DIR, gen_boss_warning(), normalize_peak=0.8)
    print("Done.")


if __name__ == "__main__":
    main()
