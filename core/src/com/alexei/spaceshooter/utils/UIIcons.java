package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIIcons {

    public static Drawable createPauseIcon(int size, Color color) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        int barWidth = size / 4;
        pix.fillRectangle(size / 4 - barWidth / 2, size / 4, barWidth, size / 2);
        pix.fillRectangle(3 * size / 4 - barWidth / 2, size / 4, barWidth, size / 2);
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        return drawable;
    }

    public static Drawable createHeartIcon(int size, Color color) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        // simple heart shape
        pix.fillCircle(size / 3, size / 3, size / 4);
        pix.fillCircle(2 * size / 3, size / 3, size / 4);
        int[] x = {size / 12, size / 2, size - size / 12};
        int[] y = {size / 2, size - size / 10, size / 2};
        pix.fillTriangle(x[0], y[0], x[1], y[1], x[2], y[2]);
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        return drawable;
    }

    public static Drawable createCoinIcon(int size, Color color) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.drawCircle(size / 2, size / 2, size / 2 - 2);
        pix.drawCircle(size / 2, size / 2, size / 2 - 4);
        pix.fillCircle(size / 2, size / 2, size / 4);
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        return drawable;
    }

    public static Drawable createPlayIcon(int size, Color color) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fillTriangle(size / 4, size / 4, size / 4, 3 * size / 4, 3 * size / 4, size / 2);
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        return drawable;
    }

    public static Drawable createSettingsIcon(int size, Color color) {
        Pixmap pix = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.drawCircle(size / 2, size / 2, size / 3);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x = (int) (size / 2 + Math.cos(angle) * size / 2.5);
            int y = (int) (size / 2 + Math.sin(angle) * size / 2.5);
            pix.fillCircle(x, y, size / 10);
        }
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        return drawable;
    }
}
