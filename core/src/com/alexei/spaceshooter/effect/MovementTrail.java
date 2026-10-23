package com.alexei.spaceshooter.effect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** Giữ các điểm theo scaled time và nối thành ribbon, không rải sprite rời. */
public final class MovementTrail {
    private static final float[] RED = {1,1,1,.2f,.15f,.75f}, GREEN = {.2f,.62f,.92f,1,.85f,.35f}, BLUE = {.32f,.12f,.18f,.45f,1,1};
    private final float[] x = new float[32], y = new float[32], age = new float[32];
    private int size;
    private float previousX, previousY, clock = -1;

    public int size() { return size; }

    public void update(float px, float py, float now, float lifetime, boolean enabled) {
        if (!enabled || lifetime <= 0 || now < clock) size = 0;
        if (now == clock) return;
        while (size > 0 && now - age[0] >= lifetime) removeFirst();
        if (enabled && lifetime > 0 && clock >= 0 && now > clock
                && (px - previousX) * (px - previousX) + (py - previousY) * (py - previousY) > .000001f) {
            if (size == 0) append(previousX, previousY, Math.max(clock, now - lifetime));
            append(px, py, now);
        }
        previousX = px; previousY = py; clock = now;
    }

    private void append(float px, float py, float time) {
        if (size == x.length) removeFirst();
        x[size] = px; y[size] = py; age[size++] = time;
    }

    private void removeFirst() {
        size--;
        System.arraycopy(x, 1, x, 0, size);
        System.arraycopy(y, 1, y, 0, size);
        System.arraycopy(age, 1, age, 0, size);
    }

    public void draw(SpriteBatch batch, TextureRegion texture, float now, float lifetime, boolean rainbow, float alpha) {
        if (lifetime <= 0) return;
        for (int i = 1; i < size; i++) {
            float progress = MathUtils.clamp((now - age[i]) / lifetime, 0, 1);
            float dx = x[i] - x[i - 1], dy = y[i] - y[i - 1];
            float length = (float)Math.sqrt(dx * dx + dy * dy);
            float width = (rainbow ? .10f : .09f) * (1 - progress);
            if (length <= .00001f || width <= 0) continue;
            if (rainbow) {
                float hue = progress * 5;
                int band = Math.min(5, (int)hue);
                batch.setColor(RED[band],
                        GREEN[band],
                        BLUE[band], alpha * (1 - progress) * .95f);
            } else batch.setColor(MathUtils.lerp(.92f, .07f, progress), MathUtils.lerp(.98f, .4f, progress),
                    MathUtils.lerp(1, .55f, progress), alpha * (1 - progress) * .95f);
            batch.draw(texture, x[i - 1] - width * .5f, y[i - 1], width * .5f, 0,
                    width, length + width * .25f, 1, 1, MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees - 90);
        }
        batch.setColor(1, 1, 1, 1);
    }
}
