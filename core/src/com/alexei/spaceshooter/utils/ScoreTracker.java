package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Alex on 08/07/2015.
 */
public class ScoreTracker {
    public static final int POINTS_PER_KILL = 500;
    public static final int POINTS_PER_TIME_TICK = 2;
    public static final float FONT_SCALE = 1f;

    private float timePointsElapsed = 0;
    private long enemiesKilled = 0;
    private long starsCollected = 0;

    public ScoreTracker() {

    }

    public long getEnemiesKilled() {
        return enemiesKilled;
    }

    public void setEnemiesKilled(long enemiesKilled) {
        this.enemiesKilled = enemiesKilled;
    }

    public void addEnemyKilled() { enemiesKilled++; }

    public long getTotalPoints() {
        return (long)(POINTS_PER_KILL * enemiesKilled + timePointsElapsed/(1f/60*1000) * POINTS_PER_TIME_TICK);
    }

    public void update(float deltaTime) {
        timePointsElapsed+=deltaTime;
    }

    public void render(ShapeRenderer sr, Batch batch, BitmapFont font) {
        sr.end();

        batch.begin();
        float offsetBorder = font.getLineHeight();
        float offsetHeight = font.getLineHeight()*FONT_SCALE;

        // score
//        batch.getTransformMatrix().setToTranslationAndScaling(font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder, 0, FONT_SCALE, FONT_SCALE, 1);
        font.setColor(Color.GREEN);
        font.draw(batch, "Score: " + getTotalPoints() + "", font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder);
//        batch.end();

//        batch.begin();
        // enemies killed
//        batch.getTransformMatrix().setToTranslationAndScaling(font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder - offsetHeight * 1, 0, FONT_SCALE, FONT_SCALE, 1);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Killed: " + enemiesKilled, font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder - offsetHeight * 1);
//        batch.end();

//        batch.begin();
        // stars
//        batch.getTransformMatrix().setToTranslationAndScaling(font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder - offsetHeight * 2, 0, FONT_SCALE, FONT_SCALE, 1);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Stars: " + starsCollected, font.getXHeight() * (FONT_SCALE + 1), Gdx.graphics.getHeight() - offsetBorder - offsetHeight * 2);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void reset() {
        timePointsElapsed = 0;
        enemiesKilled = 0;
        starsCollected = 0;
    }

    /**
     * Restore score state from a saved game.
     * Note: enemiesKilled is NOT restored (tracked per-session),
     * but score and stars are loaded from save.
     */
    public void loadState(long savedScore, long savedStars) {
        // Derive enemiesKilled approximation from saved score
        // SavedScore = kills * POINTS_PER_KILL + timePoints
        // We set timePointsElapsed to 0 and derive kills from score
        this.enemiesKilled = savedScore / POINTS_PER_KILL;
        this.starsCollected = savedStars;
        this.timePointsElapsed = 0;
    }

    public long getStarsCollected() {
        return starsCollected;
    }

    public void setStarsCollected(long starsCollected) {
        this.starsCollected = starsCollected;
    }

    public void collectStar() {
        starsCollected++;
    }

}
