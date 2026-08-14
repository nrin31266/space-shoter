package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.utils.TextureRegistry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LoadingScreen implements Screen {
    private final MainGame game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final AudioManager audioManager;
    private boolean loaded = false;

    public LoadingScreen(MainGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.audioManager = new AudioManager();

        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(3f);
        font.setUseIntegerPositions(false);
    }

    @Override
    public void show() {
        AssetManager assetManager = game.getAssetManager();
        // Ships (author-owned)
        assetManager.load("ship.png", Texture.class);
        assetManager.load("enemy1.png", Texture.class);
        assetManager.load("enemy2.png", Texture.class);
        assetManager.load("enemy_c.png", Texture.class);
        assetManager.load("enemy_d.png", Texture.class);
        assetManager.load("enemy_e.png", Texture.class);
        assetManager.load("enemy_f.png", Texture.class);
        assetManager.load("enemy_boss.png", Texture.class);
        // Items
        assetManager.load("item_star.png", Texture.class);
        assetManager.load("item_hp.png", Texture.class);
        assetManager.load("item_energy.png", Texture.class);
        assetManager.load("item_upgrade.png", Texture.class);
        assetManager.load("item_upgrade_explosive.png", Texture.class);
        assetManager.load("item_upgrade_homing.png", Texture.class);
        // Projectiles
        assetManager.load("laser_blue.png", Texture.class);
        assetManager.load("laser_red.png", Texture.class);
        assetManager.load("plasma_orb.png", Texture.class);
        assetManager.load("orb_red.png", Texture.class);
        assetManager.load("orb_green.png", Texture.class);
        assetManager.load("orb_gold.png", Texture.class);
        assetManager.load("orb_purple.png", Texture.class);
        assetManager.load("orb_pink.png", Texture.class);
        assetManager.load("shot_orb.png", Texture.class);
        assetManager.load("shot_dart.png", Texture.class);
        // Background
        assetManager.load("nebula.png", Texture.class);
        audioManager.loadSounds(assetManager);
        audioManager.loadMusic(assetManager);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        AssetManager assetManager = game.getAssetManager();
        if (assetManager.update()) {
            if (!loaded) {
                loaded = true;
                // Populate texture and audio registries from loaded AssetManager resources
                TextureRegistry.populate(assetManager);
                audioManager.populate(assetManager);
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
            return;
        }

        float progress = assetManager.getProgress();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float barWidth = screenWidth * 0.6f;
        float barHeight = 30;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight / 2f;

        // progress bar
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * progress, barHeight);
        shapeRenderer.end();

        // percentage text
        batch.begin();
        font.setColor(Color.WHITE);
        String pctText = "Loading " + (int)(progress * 100) + "%";
        font.draw(batch, pctText,
                (screenWidth - 150) / 2f, barY + barHeight + 40);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
