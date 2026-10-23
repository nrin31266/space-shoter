package com.alexei.spaceshooter.manager;

import java.util.Arrays;

/** Pool cố định theo source: danger/critical/weapon/common/pickup/UI không tranh slot. */
public final class AudioVoices {
    private static final int[] START = {0,4,6,8,12,14}, END = {4,6,8,12,14,16};
    private static final float[] BUDGET = {.60f,.65f,.65f,.65f,.55f,.35f};
    private final int[] cue = new int[16];
    private final float[] until = new float[16], started = new float[16], level = new float[16], scale = {1,1,1,1,1,1}, total = new float[6];
    private float dangerUntil;

    public AudioVoices() { clear(); }
    private static int group(int slot) { return slot < 4 ? 0 : slot < 6 ? 1 : slot < 8 ? 2 : slot < 12 ? 3 : slot < 14 ? 4 : 5; }

    public int acquire(int id, int tier, int limit, float now, float duration, float gain) {
        int playing = 0;
        for (int i = 0; i < 16; i++) if (cue[i] == id && until[i] > now) playing++;
        if (playing >= limit) return -1;
        int g = id >= 32 && id <= 34 ? 5 : tier == 0 ? 0 : tier == 1 ? 1 : tier == 2 ? 2 : tier == 4 ? 4 : 3;
        int slot = -1;
        for (int i = START[g]; i < END[g]; i++) if (until[i] <= now) { slot = i; break; }
        if (slot < 0 && tier <= 1) {
            slot = START[g];
            for (int i = slot + 1; i < END[g]; i++) if (started[i] < started[slot]) slot = i;
        }
        if (slot < 0) return -1;
        cue[slot] = id; started[slot] = now; until[slot] = now + duration; level[slot] = gain;
        if (tier == 0) dangerUntil = Math.max(dangerUntil, now + duration);
        return slot;
    }

    public void tick(float now, float dt) {
        Arrays.fill(total, 0);
        for (int i = 0; i < 16; i++) if (until[i] > now) total[group(i)] += level[i];
        for (int i = 0; i < 6; i++) {
            float target = total[i] > BUDGET[i] ? BUDGET[i] / total[i] : 1;
            scale[i] = target < scale[i] ? target : Math.min(target, scale[i] + 2 * dt);
        }
    }

    public boolean danger(float now) { return dangerUntil > now; }
    public float gain(int slot, float now, boolean paused) {
        if (until[slot] <= now) return 0;
        int g = group(slot);
        float duck = g < 2 || g == 5 ? 1 : paused ? .12f : danger(now) ? g == 2 ? .55f : g == 3 ? .30f : .65f : 1;
        return level[slot] * scale[g] * duck;
    }
    public void clearGameplay() {
        Arrays.fill(cue, 0, 14, -1); Arrays.fill(until, 0, 14, 0);
        Arrays.fill(scale, 0, 5, 1); dangerUntil = 0;
    }
    public void clear() { Arrays.fill(cue, -1); Arrays.fill(until, 0); Arrays.fill(scale, 1); dangerUntil = 0; }
}
