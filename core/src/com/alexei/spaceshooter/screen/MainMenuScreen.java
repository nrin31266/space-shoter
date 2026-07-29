package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.Starfield;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.manager.GameState;
import com.alexei.spaceshooter.utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenuScreen implements Screen {
    private final MainGame game;
    private final AudioManager audioManager;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final Ship ship;
    private final Starfield starfield;
    private final Starfield starfield2;

    private Stage stage;
    private Skin skin;

    public MainMenuScreen(MainGame game, AudioManager audioManager) {
        this.game = game;
        this.audioManager = audioManager;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.ship = new Ship();

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        this.starfield = new Starfield((int) sw, (int) sh,
                GameState.STAR_SCROLL_ANGLE, GameState.STAR_SCROLL_SPEED,
                GameState.STAR_COUNT, GameState.MIN_STAR_SIZE, GameState.MAX_STAR_SIZE);
        this.starfield2 = new Starfield((int) sw, (int) sh,
                GameState.STAR_SCROLL_ANGLE_2, GameState.STAR_SCROLL_SPEED_2,
                GameState.STAR_COUNT_2, GameState.MIN_STAR_SIZE_2, GameState.MAX_STAR_SIZE_2);
    }

    @Override
    public void show() {
        audioManager.stopAllMusic();
        audioManager.playMusic(SoundName.Ut);

        createSkin();
        stage = new Stage(new ScreenViewport());
        buildUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void createSkin() {
        skin = new Skin();

        // generate a white pixel texture for button backgrounds
        Pixmap pix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        skin.add("white", new Texture(pix));
        pix.dispose();

        // font - base scale 1 to allow clean high-scale labels
        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        buttonFont.getData().setScale(1f);
        buttonFont.setUseIntegerPositions(false);
        skin.add("default-font", buttonFont);

        Label.LabelStyle labelStyle = new Label.LabelStyle(buttonFont, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.down = skin.newDrawable("white", Color.GRAY);
        btnStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        btnStyle.font = buttonFont;
        btnStyle.fontColor = Color.GREEN;
        btnStyle.downFontColor = Color.YELLOW;
        skin.add("btn", btnStyle);

        // window style for Dialog/Window (required to prevent crash)
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = buttonFont;
        windowStyle.background = skin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 0.95f));
        skin.add("default", windowStyle);
    }

    private void buildUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("SPACE SHOOTER", skin);
        title.setAlignment(Align.center);
        title.setFontScale(3f);
        title.setColor(Color.GOLD);
        table.add(title).padBottom(80).row();

        // Play Button
        TextButton.TextButtonStyle playStyle = new TextButton.TextButtonStyle(skin.get("btn", TextButton.TextButtonStyle.class));
        TextButton playBtn = new TextButton("  PLAY  ", playStyle);
        playBtn.getLabel().setFontScale(5f);
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioManager.playSound(SoundName.Go);
                audioManager.stopAllMusic();
                game.setScreen(new GamePlayScreen(game, audioManager));
            }
        });
        table.add(playBtn).width(800).height(200).padBottom(40).row();

        // Settings Button
        TextButton.TextButtonStyle settingsStyle = new TextButton.TextButtonStyle(skin.get("btn", TextButton.TextButtonStyle.class));
        TextButton settingsBtn = new TextButton(" SETTINGS ", settingsStyle);
        settingsBtn.getLabel().setFontScale(5f);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSettingsDialog();
            }
        });
        table.add(settingsBtn).width(800).height(180).row();

        stage.addActor(table);
    }

    private void showSettingsDialog() {
        VisDialog dialog = new VisDialog("");
        dialog.pad(80);
        dialog.getContentTable().defaults().center().pad(25);

        VisLabel titleLabel = new VisLabel("SETTINGS");
        titleLabel.setColor(Color.CYAN);
        titleLabel.setFontScale(5f);
        titleLabel.setAlignment(Align.center);
        dialog.getContentTable().add(titleLabel).padBottom(50).row();

        String soundLabel = audioManager.isMuted() ? "Sound: OFF" : "Sound: ON ";
        VisTextButton soundBtn = new VisTextButton(soundLabel);
        soundBtn.getLabel().setFontScale(2.5f);
        soundBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean newMuted = !audioManager.isMuted();
                audioManager.setMuted(newMuted);
                if (!newMuted) {
                    audioManager.playMusic(SoundName.Ut);
                }
                soundBtn.setText(newMuted ? "Sound: OFF" : "Sound: ON ");
            }
        });
        dialog.getContentTable().add(soundBtn).width(600).height(150).row();

        VisTextButton closeBtn = new VisTextButton("  Close  ");
        closeBtn.getLabel().setFontScale(2.5f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getContentTable().add(closeBtn).width(400).height(120).row();

        dialog.show(stage, null);
        dialog.setWidth(Gdx.graphics.getWidth() * 0.85f);
        dialog.setPosition((Gdx.graphics.getWidth() - dialog.getWidth()) / 2f, (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    @Override
    public void render(float delta) {
        float deltaTime = Gdx.graphics.getDeltaTime() * 1000;
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ship.setX((screenWidth - ship.getWidth()) / 2f);
        ship.setY(ship.getHeight() * 2);

        starfield.update(deltaTime);
        starfield2.update(deltaTime);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        starfield.render(shapeRenderer, batch);
        starfield2.render(shapeRenderer, batch);
        ship.render(shapeRenderer, batch);
        shapeRenderer.end();

        // Scene2D UI overlay
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
