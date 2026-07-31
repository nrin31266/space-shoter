package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Weapon-upgrade pickup item.
 * Visual: blue ring (proper hollow ring) with a solid blue triangle centred inside.
 */
public class ItemWeaponUpgrade extends Item {
    public static final float ITEM_SIZE = 18f;
    public static final Color ITEM_COLOR = Color.valueOf("0077ff");
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static final float RING_OUTER  = 22f;
    private static final float RING_INNER  = 16f;
    private static final int   SEGMENTS    = 64;

    public ItemWeaponUpgrade(float x, float y) {
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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // ── Ring: full blue disc, then overdraw centre with bg colour ──
        sr.setColor(0f, 0.47f, 1f, 1f);
        sr.circle(cx, cy, ro, SEGMENTS);

        sr.setColor(0.04f, 0.04f, 0.08f, 1f); // match space bg
        sr.circle(cx, cy, ri, SEGMENTS);

        // ── Blue triangle pointing up (fits inside hollow centre) ──
        float half = ri * 0.82f;
        float triH = ri * 1.45f;
        float baseY = cy - triH * 0.42f; // shift slightly downward for visual balance
        sr.setColor(0f, 0.47f, 1f, 1f);
        sr.triangle(
            cx - half, baseY,
            cx + half, baseY,
            cx,        baseY + triH
        );

        sr.end();
        batch.begin();
    }
}
