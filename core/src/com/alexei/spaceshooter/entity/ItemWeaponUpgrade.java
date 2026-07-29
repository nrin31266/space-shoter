package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class ItemWeaponUpgrade extends Item {
    public static final float ITEM_SIZE = 26;
    public static final Color ITEM_COLOR = Color.valueOf("00ffff"); // cyan
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static TextureRegion textureRegion;
    private final int rotationSpeed = MathUtils.random(60, 120);

    public ItemWeaponUpgrade(float x, float y) {
        super(x, y, ITEM_SIZE, ITEM_SIZE);
        super.setColor(ITEM_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        super.setOrientation(MathUtils.random(0, 359));
        super.setPickUpSound(PICK_UP_SOUND);
        
        super.setBounceCount(0); // Upgrade falls straight off
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
        
        // Draw an upward pointing double chevron
        pixmap.setColor(ITEM_COLOR);
        pixmap.fillTriangle(size/2, 2, 5, 20, size - 5, 20);
        pixmap.fillTriangle(size/2, 20, 5, 38, size - 5, 38);
        
        // Inner yellow for contrast
        pixmap.setColor(Color.YELLOW);
        pixmap.fillTriangle(size/2, 6, 12, 18, size - 12, 18);
        pixmap.fillTriangle(size/2, 24, 12, 36, size - 12, 36);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegion(texture);
    }
}
