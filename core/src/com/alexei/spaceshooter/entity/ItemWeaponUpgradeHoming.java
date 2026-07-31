package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Homing Lightning Upgrade item (Track 2).
 * Visual: Purple ring with a solid purple/magenta 4-pointed crystal inside.
 */
public class ItemWeaponUpgradeHoming extends Item {
    public static final float ITEM_SIZE = 18f;
    public static final Color ITEM_COLOR = Color.valueOf("cc00ff");
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static final float RING_OUTER  = 22f;
    private static final float RING_INNER  = 16f;
    private static final int   SEGMENTS    = 64;

    public ItemWeaponUpgradeHoming(float x, float y) {
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
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float scale = getPickUpAnimationScale();
        if (scale == 0) scale = 1f;

        float cx = getCenterX();
        float cy = getCenterY();
        float ro  = RING_OUTER * scale;
        float ri  = RING_INNER * scale;

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Purple ring
        sr.setColor(0.8f, 0f, 1f, 1f);
        sr.circle(cx, cy, ro, SEGMENTS);

        sr.setColor(0.04f, 0.04f, 0.08f, 1f);
        sr.circle(cx, cy, ri, SEGMENTS);

        // 4-pointed star crystal inside
        float outerR = ri * 0.82f;
        float innerR = ri * 0.30f;
        sr.setColor(0.85f, 0.2f, 1f, 1f);

        for (int i = 0; i < 4; i++) {
            float a1 = i * 90f;
            float a2 = a1 + 45f;
            float a3 = a1 + 90f;

            float x1 = cx + outerR * MathUtils.cosDeg(a1);
            float y1 = cy + outerR * MathUtils.sinDeg(a1);
            float x2 = cx + innerR * MathUtils.cosDeg(a2);
            float y2 = cy + innerR * MathUtils.sinDeg(a2);
            float x3 = cx + outerR * MathUtils.cosDeg(a3);
            float y3 = cy + outerR * MathUtils.sinDeg(a3);

            sr.triangle(cx, cy, x1, y1, x2, y2);
            sr.triangle(cx, cy, x2, y2, x3, y3);
        }

        sr.end();
        batch.begin();
    }
}
