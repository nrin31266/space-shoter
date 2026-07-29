package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.utils.SoundName;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Alex on 03/07/2015.
 */
public class ItemStar extends Item {
    public static final float STAR_SIZE_OUTER = 20;
    public static final float STAR_SIZE_INNER = 10;
    public static final Color STAR_COLOR = Color.YELLOW;
    public static final SoundName PICK_UP_SOUND = SoundName.Hit7;

    private static TextureRegion starTextureRegion;

    private final int rotationSpeed = MathUtils.random(45, 135);
    private int multiplier = 1;

    public ItemStar(float x, float y, int multiplier) {
        super(x, y, STAR_SIZE_OUTER * 2 * multiplier, STAR_SIZE_OUTER * 2 * multiplier);
        super.setColor(STAR_COLOR);
        super.setOrientInDirectionOfVelocity(false);
        super.setOrientation(MathUtils.random(0, 359));
        super.setPickUpSound(PICK_UP_SOUND);
        this.multiplier = multiplier;

        if (starTextureRegion == null) {
            starTextureRegion = createStarTexture();
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

        float texW = starTextureRegion.getRegionWidth();
        float texH = starTextureRegion.getRegionHeight();
        float w = texW * scale * multiplier;
        float h = texH * scale * multiplier;

        batch.setColor(STAR_COLOR);
        batch.draw(starTextureRegion,
                getCenterX() - w / 2f, getCenterY() - h / 2f,
                w / 2f, h / 2f,
                w, h,
                1f, 1f,
                getOrientation());
    }

    /**
     * Generate a 5-pointed star texture using Pixmap.
     * Computes star polygon vertices (5 outer + 5 inner points)
     * and fills triangles radiating from center.
     */
    private static TextureRegion createStarTexture() {
        int size = 40;
        float cx = size / 2f;
        float cy = size / 2f;
        float outerR = size / 2f - 2;
        float innerR = outerR * 0.4f;
        int arms = 5;

        // compute all 10 vertices (alternating outer/inner)
        float[] verts = new float[arms * 4];
        double angleStep = Math.PI / arms;
        for (int i = 0; i < arms * 2; i++) {
            float r = (i % 2 == 0) ? outerR : innerR;
            double a = i * angleStep - Math.PI / 2; // start from top
            verts[i * 2] = cx + (float) Math.cos(a) * r;
            verts[i * 2 + 1] = cy + (float) Math.sin(a) * r;
        }

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(STAR_COLOR);

        // fill each triangle: center + two adjacent vertices
        for (int i = 0; i < arms * 2; i++) {
            int next = (i + 1) % (arms * 2);
            pixmap.fillTriangle(
                    (int) cx, (int) cy,
                    (int) verts[i * 2], (int) verts[i * 2 + 1],
                    (int) verts[next * 2], (int) verts[next * 2 + 1]);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return new TextureRegion(texture);
    }
}
