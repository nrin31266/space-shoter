package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.Starfield;
import com.alexei.spaceshooter.entity.Ship;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.manager.GameState;
import com.alexei.spaceshooter.manager.SaveManager;
import com.alexei.spaceshooter.utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.alexei.spaceshooter.utils.CustomUI;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
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
    private BitmapFont titleFont;
    private BitmapFont fontAwesome;

    // Title animation
    private float titlePulse = 0f;

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

        // White pixel for drawable backgrounds
        Pixmap pix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        skin.add("white", new Texture(pix));
        pix.dispose();

        BitmapFont buttonFont = FontUtil.generateRoboto(48);
        buttonFont.getData().setScale(1f);
        buttonFont.setUseIntegerPositions(false);
        skin.add("default-font", buttonFont);
        
        fontAwesome = FontUtil.generateFontAwesome(48);
        
        titleFont = FontUtil.generateRoboto(65);
        titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        titleFont.getData().setScale(1.0f);

        Label.LabelStyle labelStyle = new Label.LabelStyle(buttonFont, Color.WHITE);
        skin.add("default", labelStyle);

        // Primary button — dark navy with cyan text
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle btnStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        btnStyle.up   = skin.newDrawable("white", new Color(0.06f, 0.10f, 0.20f, 0.95f));
        btnStyle.down = skin.newDrawable("white", new Color(0.00f, 0.55f, 0.75f, 0.95f));
        btnStyle.over = skin.newDrawable("white", new Color(0.10f, 0.20f, 0.35f, 0.95f));
        btnStyle.font = buttonFont;
        btnStyle.fontColor = new Color(0f, 0.9f, 1f, 1f);
        btnStyle.downFontColor = Color.WHITE;
        skin.add("btn", btnStyle);

        // Continue button — slightly teal-ish
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle continueStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        continueStyle.up   = skin.newDrawable("white", new Color(0.04f, 0.18f, 0.18f, 0.95f));
        continueStyle.down = skin.newDrawable("white", new Color(0.00f, 0.65f, 0.55f, 0.95f));
        continueStyle.over = skin.newDrawable("white", new Color(0.06f, 0.28f, 0.25f, 0.95f));
        continueStyle.font = buttonFont;
        continueStyle.fontColor = new Color(0f, 1f, 0.75f, 1f);
        continueStyle.downFontColor = Color.WHITE;
        skin.add("continue", continueStyle);

        // Settings button — muted purple
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle settingsStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        settingsStyle.up   = skin.newDrawable("white", new Color(0.12f, 0.08f, 0.22f, 0.95f));
        settingsStyle.down = skin.newDrawable("white", new Color(0.50f, 0.30f, 0.80f, 0.95f));
        settingsStyle.over = skin.newDrawable("white", new Color(0.20f, 0.12f, 0.35f, 0.95f));
        settingsStyle.font = buttonFont;
        settingsStyle.fontColor = new Color(0.7f, 0.5f, 1f, 1f);
        settingsStyle.downFontColor = Color.WHITE;
        skin.add("settings", settingsStyle);

        // Dialog style buttons
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle dialogBtnStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        dialogBtnStyle.up   = skin.newDrawable("white", new Color(0.06f, 0.10f, 0.20f, 0.95f));
        dialogBtnStyle.down = skin.newDrawable("white", new Color(0.00f, 0.55f, 0.75f, 0.95f));
        dialogBtnStyle.over = skin.newDrawable("white", new Color(0.10f, 0.20f, 0.35f, 0.95f));
        dialogBtnStyle.font = buttonFont;
        dialogBtnStyle.fontColor = new Color(0f, 0.9f, 1f, 1f);
        dialogBtnStyle.downFontColor = Color.WHITE;
        skin.add("dialog-btn", dialogBtnStyle);

        // Window / dialog background
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = buttonFont;
        windowStyle.background = skin.newDrawable("white", new Color(0.04f, 0.06f, 0.12f, 0.97f));
        skin.add("default", windowStyle);
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label subtitle = new Label("DEFEND THE GALAXY", CustomUI.getSkin());
        subtitle.setAlignment(Align.center);
        subtitle.setFontScale(1.3f);
        subtitle.setColor(new Color(0.4f, 0.6f, 0.9f, 0.8f));
        root.add(subtitle).padBottom(15).row();

        SaveManager smStats = new SaveManager();
        long highScore  = smStats.getHighScore();
        long totalStars = smStats.getTotalStars();

        Label highScoreLabel = new Label("HIGH SCORE: " + highScore, CustomUI.getSkin());
        highScoreLabel.setAlignment(Align.center);
        highScoreLabel.setFontScale(0.85f);
        highScoreLabel.setColor(new Color(1f, 0.88f, 0.1f, 0.95f));
        root.add(highScoreLabel).padBottom(6).row();

        Label totalStarsLabel = new Label("TOTAL STARS: " + totalStars, CustomUI.getSkin());
        totalStarsLabel.setAlignment(Align.center);
        totalStarsLabel.setFontScale(0.85f);
        totalStarsLabel.setColor(new Color(0f, 0.92f, 1f, 0.95f));
        root.add(totalStarsLabel).padBottom(20).row();

        // Spacer
        root.add().height(100).row();

        // ─── NEW GAME Button ──────────────────────────────────────
        Button newGameBtn = CustomUI.createButton("\uf04b", "NEW GAME", false);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audioManager.playSound(SoundName.Go);
                audioManager.stopAllMusic();
                SaveManager sm = new SaveManager();
                sm.clear();
                game.setScreen(new GamePlayScreen(game, audioManager, false, 1));
            }
        });
        root.add(newGameBtn).width(1000).height(150).padBottom(35).row();

        // ─── CONTINUE Button (only if save exists) ────────────────
        SaveManager saveCheck = new SaveManager();
        if (saveCheck.hasSavedGame()) {
            Button continueBtn = CustomUI.createButton("\uf04b", "CONTINUE", false);
            continueBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    audioManager.playSound(SoundName.Go);
                    audioManager.stopAllMusic();
                    game.setScreen(new GamePlayScreen(game, audioManager, true, 1));
                }
            });
            root.add(continueBtn).width(1000).height(150).padBottom(35).row();
        }

        // ─── SETTINGS Button ──────────────────────────────────────
        Button settingsBtn = CustomUI.createButton("\uf013", "SETTINGS", false);
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSettingsDialog();
            }
        });
        root.add(settingsBtn).width(1000).height(150).row();

        stage.addActor(root);
    }

    private void showSettingsDialog() {
        VisDialog dialog = new VisDialog("");
        dialog.pad(80);
        dialog.getContentTable().defaults().center().pad(20);

        // Title
        Label titleLabel = CustomUI.createTitle("\uf013 SETTINGS", new Color(0.7f, 0.5f, 1f, 1f));
        dialog.getContentTable().add(titleLabel).padBottom(30).row();

        // Divider
        addDialogDivider(dialog, new Color(0.5f, 0.3f, 0.9f, 0.6f));

        // Volume Slider
        final Label volumeLabel = CustomUI.createTitle("Volume: " + (int)(audioManager.getVolume() * 100f) + "%", Color.WHITE);
        volumeLabel.setFontScale(1.0f);
        dialog.getContentTable().add(volumeLabel).padBottom(10).row();

        final VisSlider volumeSlider = new VisSlider(0f, 100f, 1f, false);
        com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle(volumeSlider.getStyle());
        style.background = CustomUI.getSkin().newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 1f));
        style.background.setMinHeight(15f);
        style.knob = CustomUI.getSkin().newDrawable("white", new Color(0f, 0.9f, 1f, 1f));
        style.knob.setMinHeight(50f);
        style.knob.setMinWidth(30f);
        volumeSlider.setStyle(style);

        volumeSlider.setValue(audioManager.getVolume() * 100f);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = volumeSlider.getValue() / 100f;
                audioManager.setVolume(vol);
                volumeLabel.setText("Volume: " + (int)(vol * 100f) + "%");
            }
        });
        dialog.getContentTable().add(volumeSlider).width(600).padBottom(30).row();

        // ─── Music Mute Toggle (Part B) ───────────────────────────────
        addDialogDivider(dialog, new Color(0.5f, 0.3f, 0.9f, 0.4f));

        // Current state read once at dialog-open time
        final boolean musicCurrentlyMuted = audioManager.isMusicMuted();
        // \uf001 = FontAwesome music note; \uf026 = volume-off (muted)
        String musicIcon  = musicCurrentlyMuted ? "\uf026" : "\uf001";
        String musicBtnLabel = musicCurrentlyMuted ? "Music: OFF" : "Music: ON";

        final Button musicToggleBtn = CustomUI.createButton(musicIcon, musicBtnLabel, musicCurrentlyMuted);
        musicToggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean nowMuted = !audioManager.isMusicMuted();
                audioManager.setMusicMuted(nowMuted);
                if (!nowMuted) {
                    // Unmuting: restart menu music that was stopped by setMusicMuted(true)
                    audioManager.playMusic(SoundName.Ut);
                }
                
                // Update button style & labels in-place (no dialog reload flicker)
                musicToggleBtn.setStyle(CustomUI.getSkin().get(nowMuted ? "danger" : "default", Button.ButtonStyle.class));
                if (musicToggleBtn.getChildren().size >= 2) {
                    if (musicToggleBtn.getChildren().get(0) instanceof Label) {
                        Label iconLbl = (Label) musicToggleBtn.getChildren().get(0);
                        iconLbl.setText(nowMuted ? "\uf026" : "\uf001");
                        iconLbl.setColor(nowMuted ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0f, 0.9f, 1f, 1f));
                    }
                    if (musicToggleBtn.getChildren().get(1) instanceof Label) {
                        Label textLbl = (Label) musicToggleBtn.getChildren().get(1);
                        textLbl.setText(nowMuted ? "Music: OFF" : "Music: ON");
                        textLbl.setColor(nowMuted ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0f, 0.9f, 1f, 1f));
                    }
                }
            }
        });
        dialog.getContentTable().add(musicToggleBtn).width(600).height(130).padTop(10).padBottom(25).row();

        // Close — \uf00d is FontAwesome × (times/close icon)
        Button closeBtn = CustomUI.createButton("\uf00d", "Close", false);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getContentTable().add(closeBtn).width(400).height(120).padTop(10).row();

        dialog.show(stage, Actions.sequence(Actions.alpha(0f), Actions.alpha(1f, 0.2f)));
        dialog.pack();
        dialog.setPosition((Gdx.graphics.getWidth() - dialog.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    /** Thin horizontal divider inside a dialog */
    private void addDialogDivider(VisDialog dialog, Color color) {
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(color);
        px.fill();
        Texture tex = new Texture(px);
        px.dispose();
        com.badlogic.gdx.scenes.scene2d.ui.Image line =
                new com.badlogic.gdx.scenes.scene2d.ui.Image(tex);
        dialog.getContentTable().add(line).width(600).height(2).padTop(10).padBottom(10).row();
    }

    private void showDialogCentered(VisDialog dialog) {
        dialog.show(stage, Actions.sequence(
                Actions.alpha(0f),
                Actions.alpha(1f, 0.2f)));
        dialog.pack();
        dialog.setWidth(Math.max(dialog.getWidth(), Gdx.graphics.getWidth() * 0.85f));
        dialog.setPosition(
                (Gdx.graphics.getWidth() - dialog.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    @Override
    public void render(float delta) {
        float deltaTime = Gdx.graphics.getDeltaTime() * 1000;
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0, 0, 0.04f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        titlePulse += delta;

        ship.setX((screenWidth - ship.getWidth()) / 2f);
        ship.setY(ship.getHeight() * 2);

        starfield.update(deltaTime);
        starfield2.update(deltaTime);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        starfield.render(shapeRenderer, batch);
        starfield2.render(shapeRenderer, batch);
        ship.render(shapeRenderer, batch);
        shapeRenderer.end();

        // ─── Animated title (drawn manually for glow effect) ─────
        renderTitle(screenWidth, screenHeight);

        // Scene2D overlay
        stage.act(delta);
        stage.draw();
    }

    /**
     * Renders the "SPACE SHOOTER" title with a pulsing multi-layer glow.
     * We render 3 passes: outer glow → mid → core, each with different color/alpha.
     */
    private void renderTitle(float sw, float sh) {
        float pulse = (MathUtils.sin(titlePulse * 2.2f) + 1f) / 2f; // 0..1

        batch.begin();

        String title = "SPACE SHOOTER";
        com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(titleFont, title);
        float tx = (sw - gl.width) / 2f;
        float ty = sh * 0.88f;

        // Outer glow layer (cyan, semi-transparent, offset)
        float glowAlpha = 0.25f + pulse * 0.25f;
        for (int ox = -3; ox <= 3; ox += 3) {
            for (int oy = -3; oy <= 3; oy += 3) {
                if (ox == 0 && oy == 0) continue;
                titleFont.setColor(0f, 0.8f, 1f, glowAlpha);
                titleFont.draw(batch, title, tx + ox, ty + oy);
            }
        }
        // Mid layer — white
        titleFont.setColor(new Color(0.7f, 0.9f, 1f, 0.85f + pulse * 0.15f));
        titleFont.draw(batch, title, tx, ty);
        // Core layer — pure white highlight (slightly smaller offset upward)
        titleFont.setColor(new Color(1f, 1f, 1f, 0.95f));
        titleFont.draw(batch, title, tx, ty + 1);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (titleFont != null) titleFont.dispose();
        if (fontAwesome != null) fontAwesome.dispose();
    }
}
