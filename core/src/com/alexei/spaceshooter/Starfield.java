package com.alexei.spaceshooter;

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/**
 * Created by Alex on 16/06/2015.

    A background star field that scrolls as we fly.
    We can control which way the stars scroll, how many stars comprise the field, and its dimensions.
    The rendering of the stars occurs here as well.
 */
public class Starfield extends Visual {
    public ArrayList<Star> stars = new ArrayList<Star>();
    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    private int starCount; // the amount of stars in the star field


    public float getMinStarSize() {
        return minStarSize;
    }

    public void setMinStarSize(float minStarSize) {
        this.minStarSize = minStarSize;
    }

    private float minStarSize = 1;

    public float getMaxStarSize() {
        return maxStarSize;
    }

    public void setMaxStarSize(float maxStarSize) {
        this.maxStarSize = maxStarSize;
    }

    private float maxStarSize = 2;

    public Starfield(int width, int height, float scrollAngle, float scrollSpeed, int starCount, float minStarSize, float maxStarSize) {
        super(0, 0, width, height);
        super.setVelocity(scrollAngle, scrollSpeed);
        setMinStarSize(minStarSize);
        setMaxStarSize(maxStarSize);
        initStars(starCount);
    }

    public void initStars(int starCount) {
        this.starCount = starCount;
        float randomSize;

        // randomize star locations
        for (int i=0; i<starCount;i++){
            randomSize = MathUtils.random(this.minStarSize, maxStarSize); // randomize star size
            stars.add(new Star(MathUtils.random(0, Gdx.graphics.getWidth()-randomSize), MathUtils.random(0, Gdx.graphics.getHeight()-randomSize), randomSize, super.getDirection(), super.getSpeed()));
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Visual star : stars) {
            star.update(deltaTime);
        }
    }

    @Override
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        sr.setColor(1, 1, 1, 1);
        for(Star s : stars) {
            s.render(sr,batch);
        }
    }

    public class Star extends Visual {
        public Star(float x, float y, float size, float direction, float speed) {
            super(x, y, size, size);
            super.setVelocity(direction,speed);
            setSize(size);
        }

        public float getSize() {
            return super.getWidth();
        }

        public void setSize(float size) {
            super.setWidth(size);
            super.setHeight(size);
        }

        @Override
        public void render(ShapeRenderer sr, SpriteBatch batch) {

            // check if star is out of bounds, in which case relocate it somewhere on the bottom screen edge
            if (getY() < -getHeight()) {
                float x = MathUtils.random(0, Gdx.graphics.getWidth() - getWidth());
                float y = Gdx.graphics.getHeight() + getWidth();
                setY(y);
                setX(x);
            }

            sr.rect(getX(), getY(), getWidth(), getHeight());
        }
    }
}
