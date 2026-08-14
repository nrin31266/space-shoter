package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 03/07/2015.
 */
public class ItemStar extends Item {
    // Emerald crystal currency. Base size raised ~30% for mobile visibility.
    public static final float STAR_SIZE_OUTER = 26;
    public static final float STAR_SIZE_INNER = 10;
    public static final Color STAR_COLOR = Color.valueOf("28d47c"); // emerald
    public static final SoundName PICK_UP_SOUND = SoundName.Pickup;

    private final int rotationSpeed = MathUtils.random(45, 135);
    private int multiplier = 1;

    public ItemStar(float x, float y, int multiplier) {
        super(x, y, STAR_SIZE_OUTER * 2 * multiplier, STAR_SIZE_OUTER * 2 * multiplier);
        super.setColor(STAR_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        super.setOrientation(MathUtils.random(0, 359));
        super.setPickUpSound(PICK_UP_SOUND);
        this.multiplier = multiplier;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float d = deltaTime / 1000f * rotationSpeed;
        setOrientation(getOrientation() + d);
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        if (batch == null || !batch.isDrawing()) return;
        float scale = getPickUpAnimationScale();
        if (scale == 0) scale = 1;

        TextureRegion region = TextureRegistry.itemStar;
        if (region == null) return;
        float w = getWidth() * scale * 1.6f;
        float h = getHeight() * scale * 1.6f;

        // Draw at full brightness so the emerald crystal colour reads clearly.
        batch.setColor(Color.WHITE);
        batch.draw(region,
                getCenterX() - w / 2f, getCenterY() - h / 2f,
                w / 2f, h / 2f,
                w, h,
                1f, 1f,
                getOrientation());
        batch.setColor(Color.WHITE);
    }
}
