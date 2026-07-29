package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class ItemHP extends Item {
    public static final float ITEM_SIZE = 24;
    public static final Color ITEM_COLOR = Color.valueOf("ff3b3b"); // bright red
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static TextureRegion textureRegion;
    private final int rotationSpeed = MathUtils.random(30, 90);

    public ItemHP(float x, float y) {
        super(x, y, ITEM_SIZE, ITEM_SIZE);
        super.setColor(ITEM_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        super.setOrientation(MathUtils.random(0, 359));
        super.setPickUpSound(PICK_UP_SOUND);
        
        super.setBounceCount(0); // HP falls straight off
        super.setBaseSpeed(150); // Very slow initial scatter
        super.setVelocity(MathUtils.random(0, 359), 150); // Re-apply scatter speed
        this.gravity = 300f;
        this.terminalVelocity = -350f; // Very slow fall speed
        
        if (textureRegion == null) {
            textureRegion = createTexture();
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float d = deltaTime / 1000f * rotationSpeed;
        setOrientation(getOrientation() + d);
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        float scale = getPickUpAnimationScale();
        if (scale == 0) scale = 1;

        float texW = textureRegion.getRegionWidth();
        float texH = textureRegion.getRegionHeight();
        float w = texW * scale;
        float h = texH * scale;

        batch.setColor(ITEM_COLOR);
        batch.draw(textureRegion,
                getCenterX() - w / 2f, getCenterY() - h / 2f,
                w / 2f, h / 2f,
                w, h,
                1f, 1f,
                getOrientation());
    }

    private static TextureRegion createTexture() {
        int size = 40;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(ITEM_COLOR);
        // Draw a plus shape
        int w = 10;
        pixmap.fillRectangle(size/2 - w/2, 5, w, size - 10);
        pixmap.fillRectangle(5, size/2 - w/2, size - 10, w);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegion(texture);
    }
}
