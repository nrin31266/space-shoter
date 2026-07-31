package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Explosive Blaster Upgrade item (Track 1).
 * Visual: Orange-red ring with a solid orange diamond/rhombus inside.
 */
public class ItemWeaponUpgradeExplosive extends Item {
    public static final float ITEM_SIZE = 18f;
    public static final Color ITEM_COLOR = Color.valueOf("ff6600");
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static final float RING_OUTER  = 22f;
    private static final float RING_INNER  = 16f;
    private static final int   SEGMENTS    = 64;

    public ItemWeaponUpgradeExplosive(float x, float y) {
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

        // Orange-red ring
        sr.setColor(1f, 0.35f, 0f, 1f);
        sr.circle(cx, cy, ro, SEGMENTS);

        sr.setColor(0.04f, 0.04f, 0.08f, 1f);
        sr.circle(cx, cy, ri, SEGMENTS);

        // Diamond icon inside (4 points: top, right, bottom, left)
        float sz = ri * 0.75f;
        sr.setColor(1f, 0.45f, 0f, 1f);
        // Top triangle
        sr.triangle(cx - sz, cy, cx + sz, cy, cx, cy + sz);
        // Bottom triangle
        sr.triangle(cx - sz, cy, cx + sz, cy, cx, cy - sz);

        sr.end();
        batch.begin();
    }
}
