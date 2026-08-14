package com.alexei.spaceshooter;

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/**
 * A layered space background.
 *
 * Layers (far → near):
 *  1. Nebula tile (drawn first, dark & subordinate, slowly scrolling)
 *  2. Distant small stars
 *  3. Near larger stars with motion streaks
 *
 * Rendering is SpriteBatch-based: the whole background is drawn as batched
 * quads sharing a single 1x1 white texture, so it costs a single draw call
 * instead of hundreds of ShapeRenderer primitives. This is a significant
 * mobile performance win over the old per-star ShapeRenderer rendering.
 *
 * The old render(ShapeRenderer, SpriteBatch) signature is preserved so call
 * sites stay compatible; when the SpriteBatch is active it draws batched,
 * otherwise it falls back to ShapeRenderer.
 */
public class Starfield extends Visual {

    /** Shared 1x1 white texture used to draw every star as a tinted quad. */
    private static Texture whiteTex;

    private static Texture getWhiteTex() {
        if (whiteTex == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            whiteTex = new Texture(p);
            p.dispose();
        }
        return whiteTex;
    }

    public ArrayList<Star> stars = new ArrayList<Star>();

    private int starCount;
    private float minStarSize = 1;
    private float maxStarSize = 2;

    /** Optional nebula tile drawn behind the stars. Null disables the layer. */
    private TextureRegion nebulaRegion;
    private float nebulaAlpha = 1f;
    private float nebulaSpeed = 12f;   // px/s downward scroll
    private float nebulaScroll = 0f;

    public Starfield(int width, int height, float scrollAngle, float scrollSpeed,
                     int starCount, float minStarSize, float maxStarSize) {
        super(0, 0, width, height);
        super.setVelocity(scrollAngle, scrollSpeed);
        setMinStarSize(minStarSize);
        setMaxStarSize(maxStarSize);
        initStars(starCount);
    }

    public void setNebula(TextureRegion nebula, float alpha) {
        this.nebulaRegion = nebula;
        this.nebulaAlpha = alpha;
    }

    public void initStars(int starCount) {
        this.starCount = starCount;
        stars.clear();
        for (int i = 0; i < starCount; i++) {
            float randomSize = MathUtils.random(this.minStarSize, maxStarSize);
            stars.add(new Star(
                    MathUtils.random(0, Gdx.graphics.getWidth() - randomSize),
                    MathUtils.random(0, Gdx.graphics.getHeight() - randomSize),
                    randomSize, super.getDirection(), super.getSpeed()));
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Visual star : stars) {
            star.update(deltaTime);
        }
        nebulaScroll += nebulaSpeed * (deltaTime / 1000f);
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        // ── Batch path (preferred — single draw call) ────────────────
        if (batch != null && batch.isDrawing()) {
            batch.setColor(Color.WHITE);
            drawNebula(batch);
            for (Star s : stars) {
                s.renderBatched(batch);
            }
            return;
        }

        // ── ShapeRenderer fallback (kept for compatibility) ──────────
        if (sr != null && sr.isDrawing()) {
            sr.setColor(1, 1, 1, 1);
            for (Star s : stars) {
                s.render(sr, batch);
            }
        }
    }

    private void drawNebula(SpriteBatch batch) {
        if (nebulaRegion == null) return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        // Tile two copies vertically so the nebula scrolls seamlessly.
        float tileH = sh; // cover full height per copy
        float offset = nebulaScroll % tileH;
        batch.setColor(1f, 1f, 1f, nebulaAlpha);
        batch.draw(nebulaRegion, 0, -offset, sw, tileH);
        batch.draw(nebulaRegion, 0, -offset + tileH, sw, tileH);
        batch.setColor(Color.WHITE);
    }

    // ── getters/setters (kept for compatibility) ────────────────────────
    public int getStarCount() { return starCount; }
    public void setStarCount(int starCount) { this.starCount = starCount; }
    public float getMinStarSize() { return minStarSize; }
    public void setMinStarSize(float minStarSize) { this.minStarSize = minStarSize; }
    public float getMaxStarSize() { return maxStarSize; }
    public void setMaxStarSize(float maxStarSize) { this.maxStarSize = maxStarSize; }

    public class Star extends Visual {
        private Color color;
        private float alpha;
        private float speedMultiplier;

        public Star(float x, float y, float size, float direction, float speed) {
            super(x, y, size, size);

            // Neon tint palette (kept subtle so it stays subordinate to gameplay)
            float r = MathUtils.random();
            if (r < 0.25f) this.color = new Color(0.65f, 0.85f, 1f, 1f);      // cool white-blue
            else if (r < 0.5f) this.color = new Color(1f, 0.85f, 0.65f, 1f);   // warm white
            else if (r < 0.7f) this.color = new Color(0.7f, 0.7f, 1f, 1f);     // pale blue
            else this.color = new Color(1f, 1f, 1f, 1f);                       // white

            this.alpha = MathUtils.clamp(
                    (size - minStarSize) / Math.max(0.0001f, (maxStarSize - minStarSize)) * 0.6f + 0.35f,
                    0.2f, 0.9f);

            this.speedMultiplier = MathUtils.random(0.8f, 1.2f);
            super.setVelocity(direction, speed * this.speedMultiplier);
            setSize(size);
        }

        public float getSize() { return super.getWidth(); }
        public void setSize(float size) {
            super.setWidth(size);
            super.setHeight(size);
        }

        /** Wraps the star to the top when it scrolls off the bottom. */
        private void wrapAround() {
            if (getY() < -getHeight() * 10f) {
                setY(Gdx.graphics.getHeight() + getWidth() * 10f);
                setX(MathUtils.random(0, Gdx.graphics.getWidth() - getWidth()));
            }
        }

        /** Batched draw: a tinted quad with a vertical motion streak. */
        public void renderBatched(SpriteBatch batch) {
            wrapAround();
            float size = getWidth();
            float streak = 1f + getSpeed() * 0.35f; // stretch along travel direction
            batch.setColor(color.r, color.g, color.b, alpha);
            batch.draw(getWhiteTex(), getX(), getY(), size, size * streak);
            batch.setColor(Color.WHITE);
        }

        @Override
        public void render(ShapeRenderer sr, SpriteBatch batch) {
            wrapAround();
            if (sr != null && sr.isDrawing()) {
                sr.setColor(color.r, color.g, color.b, alpha);
                float streakLength = getSpeed() * 0.15f;
                sr.rectLine(getX(), getY(), getX(), getY() + streakLength, getWidth());
            }
        }
    }
}

