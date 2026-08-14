package com.alexei.spaceshooter;

import com.alexei.spaceshooter.entity.Visual;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Color;

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
        private Color color;
        private float alpha;
        private float speedMultiplier;

        public Star(float x, float y, float size, float direction, float speed) {
            super(x, y, size, size);
            
            // Randomize a neon color for the star
            float r = MathUtils.random();
            if (r < 0.2f) this.color = new Color(0.2f, 0.8f, 1f, 1f); // Cyan
            else if (r < 0.4f) this.color = new Color(1f, 0.2f, 0.8f, 1f); // Magenta
            else if (r < 0.6f) this.color = new Color(0.8f, 0.2f, 1f, 1f); // Purple
            else if (r < 0.8f) this.color = new Color(1f, 0.8f, 0.2f, 1f); // Yellow
            else this.color = new Color(1f, 1f, 1f, 1f); // White

            // Base alpha on size (larger = brighter, closer)
            this.alpha = MathUtils.clamp((size - minStarSize) / (maxStarSize - minStarSize) * 0.8f + 0.2f, 0.2f, 1f);
            
            // Speed variance
            this.speedMultiplier = MathUtils.random(0.8f, 1.2f);
            super.setVelocity(direction, speed * this.speedMultiplier);
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
            if (getY() < -getHeight() * 10f) {
                float x = MathUtils.random(0, Gdx.graphics.getWidth() - getWidth());
                float y = Gdx.graphics.getHeight() + getWidth() * 10f;
                setY(y);
                setX(x);
                // randomly re-assign color & alpha for variety
                float r = MathUtils.random();
                if (r < 0.2f) this.color.set(0.2f, 0.8f, 1f, 1f); 
                else if (r < 0.4f) this.color.set(1f, 0.2f, 0.8f, 1f); 
                else if (r < 0.6f) this.color.set(0.8f, 0.2f, 1f, 1f); 
                else if (r < 0.8f) this.color.set(1f, 0.8f, 0.2f, 1f); 
                else this.color.set(1f, 1f, 1f, 1f);
            }

            if (sr != null && sr.isDrawing()) {
                sr.setColor(color.r, color.g, color.b, alpha);
                // Draw a streak based on velocity to simulate motion blur
                float streakLength = getSpeed() * 0.15f; 
                sr.rectLine(getX(), getY(), getX(), getY() + streakLength, getWidth());
            }
        }
    }
}
