package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * HP pickup item.
 * Visual: red ring (proper hollow ring) with a thick red "+" cross centred inside.
 */
public class ItemHP extends Item {
    public static final float ITEM_SIZE = 18f;
    public static final Color ITEM_COLOR = Color.valueOf("ff2233");
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    // Ring geometry
    private static final float RING_OUTER  = 22f; // outer radius of the ring
    private static final float RING_INNER  = 16f; // inner radius (hollow centre)
    private static final int   SEGMENTS    = 64;  // more = smoother circle

    public ItemHP(float x, float y) {
        super(x, y, ITEM_SIZE, ITEM_SIZE);
        super.setColor(ITEM_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        super.setOrientation(0);
        super.setPickUpSound(PICK_UP_SOUND);

        super.setBounceCount(0);
        super.setBaseSpeed(120);
        super.setVelocity(MathUtils.random(0, 359), 120);
        this.gravity = 280f;
        this.terminalVelocity = -320f;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float scale = getPickUpAnimationScale();
        if (scale == 0) scale = 1f;

        float cx = getCenterX();
        float cy = getCenterY();
        float ro  = RING_OUTER * scale;
        float ri  = RING_INNER * scale;

        // GamePlayScreen calls item.render() with batch open, sr ended.
        batch.end();

        // Enable blending for smooth alpha edges
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // ── Ring: outer filled circle then punch hole with background colour ──
        sr.setColor(1f, 0.13f, 0.20f, 1f);
        sr.circle(cx, cy, ro, SEGMENTS);          // full red disc

        sr.setColor(0f, 0f, 0f, 0f);              // transparent — poke hole
        // We can't truly punch holes with ShapeRenderer, so instead we
        // overdraw the centre with the scene background (near-black space).
        sr.setColor(0.04f, 0.04f, 0.08f, 1f);    // match space bg colour
        sr.circle(cx, cy, ri, SEGMENTS);           // hollow centre

        // ── Thick red "+" cross ──
        float arm = ri * 0.78f;   // half-length of each arm (stays inside ring)
        float thk = ri * 0.48f;   // arm thickness
        sr.setColor(1f, 0.13f, 0.20f, 1f);
        sr.rect(cx - thk / 2f, cy - arm, thk, arm * 2f); // vertical
        sr.rect(cx - arm,      cy - thk / 2f, arm * 2f, thk); // horizontal

        sr.end();
        batch.begin();
    }
}
