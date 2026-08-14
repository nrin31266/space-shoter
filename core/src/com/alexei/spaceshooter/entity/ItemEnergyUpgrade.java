package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Pure energy / power upgrade pickup (Track-independent).
 *
 * Unlike the three weapon-switch items (plasma / explosive / homing) which
 * only change the ACTIVE weapon track, this item ONLY raises the shared
 * weapon level (+1, or stockpile at max level) WITHOUT switching weapon type.
 *
 * Visual: neon green/yellow energy cell — clearly distinct from the blue bolt
 * (plasma), orange burst (explosive) and purple crosshair (homing) icons.
 */
public class ItemEnergyUpgrade extends Item {
    public static final float ITEM_SIZE = 34f;
    public static final Color ITEM_COLOR = Color.valueOf("66ff88");
    public static final SoundName PICK_UP_SOUND = SoundName.PowerUp;

    private static final float RING_OUTER  = 22f;
    private static final float RING_INNER  = 16f;
    private static final int   SEGMENTS    = 64;

    public ItemEnergyUpgrade(float x, float y) {
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

        if (batch != null && batch.isDrawing() && TextureRegistry.itemEnergy != null) {
            batch.setColor(Color.WHITE);
            batch.draw(TextureRegistry.itemEnergy,
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

            // Green energy ring
            sr.setColor(0.25f, 1f, 0.5f, 1f);
            sr.circle(cx, cy, ro, SEGMENTS);
            sr.setColor(0.04f, 0.04f, 0.08f, 1f);
            sr.circle(cx, cy, ri, SEGMENTS);

            // 3-point spark / power symbol inside
            float s = ri * 0.8f;
            sr.setColor(0.6f, 1f, 0.75f, 1f);
            sr.triangle(cx - s, cy - s * 0.6f, cx + s, cy - s * 0.6f, cx, cy + s);
            // central dot
            sr.circle(cx, cy - s * 0.1f, ri * 0.22f, 24);
        }
    }
}
