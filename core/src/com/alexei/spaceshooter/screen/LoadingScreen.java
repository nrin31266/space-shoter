package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class LoadingScreen extends ScreenAdapter {
    private final MainGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private boolean queued;

    public LoadingScreen(MainGame game) { this.game = game; }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(.015f, .025f, .065f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, "SPACE SHOOTER / LOADING", 40, Gdx.graphics.getHeight() * .5f);
        batch.end();
        if (!queued) {
            queued = true;
            Gdx.app.postRunnable(() -> {
                game.loadCampaign();
                game.openInitialScreen();
            });
        }
    }

    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
