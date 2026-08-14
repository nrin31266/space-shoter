package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Explosive Blaster Upgrade item (Track 1).
 * Visual: orange burst icon — distinct from blue bolt (plasma) and purple crosshair (homing).
 */
public class ItemWeaponUpgradeExplosive extends Item {
    public static final float ITEM_SIZE = 34f;
    public static final Color ITEM_COLOR = Color.valueOf("ff7a26");
    public static final SoundName PICK_UP_SOUND = SoundName.PowerUp;

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

        float w = getWidth() * scale * 2.4f;
        float h = getHeight() * scale * 2.4f;

        if (batch != null && batch.isDrawing() && TextureRegistry.itemUpgradeExplosive != null) {
            batch.setColor(Color.WHITE);
            batch.draw(TextureRegistry.itemUpgradeExplosive,
                    getCenterX() - w / 2f, getCenterY() - h / 2f,
                    w / 2f, h / 2f,
                    w, h,
                    1f, 1f,
                    0f);
            batch.setColor(Color.WHITE);
        } else if (sr != null && sr.isDrawing()) {
            float cx = getCenterX();
            float cy = getCenterY();
            float ro  = RING_OUTER * scale;
            float ri  = RING_INNER * scale;

            // Orange-red ring
            sr.setColor(1f, 0.35f, 0f, 1f);
            sr.circle(cx, cy, ro, SEGMENTS);

            sr.setColor(0.04f, 0.04f, 0.08f, 1f);
            sr.circle(cx, cy, ri, SEGMENTS);

            // Diamond icon inside
            float sz = ri * 0.75f;
            sr.setColor(1f, 0.45f, 0f, 1f);
            sr.triangle(cx - sz, cy, cx + sz, cy, cx, cy + sz);
            sr.triangle(cx - sz, cy, cx + sz, cy, cx, cy - sz);
        }
    }
}
