package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.data.wave.WaveConfig;
import com.alexei.spaceshooter.effect.*;
import com.alexei.spaceshooter.entity.*;
import com.alexei.spaceshooter.factory.EnemyFactory;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.manager.GameState;
import com.alexei.spaceshooter.manager.SaveManager;
import com.alexei.spaceshooter.manager.WaveManager;
import com.alexei.spaceshooter.utils.*;
import com.alexei.spaceshooter.weapon.*;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.alexei.spaceshooter.utils.CustomUI;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

import space.earlygrey.shapedrawer.ShapeDrawer;

public class GamePlayScreen implements Screen {

    // ─── Core references ──────────────────────────────────────────────
    private final MainGame     game;
    private final AudioManager audioManager;
    private final GameState    state;

    private WaveManager  waveManager;
    private EnemyFactory enemyFactory;
    private SaveManager  saveManager;

    private boolean continueFromSave = false;
    private int     startWaveId     = 1;

    private ShapeRenderer shapeRenderer;
    private ShapeDrawer   drawer;
    private SpriteBatch   batch;
    private BitmapFont    hudFont;
    private BitmapFont    overlayFont;
    private Texture       textureSolid;
    /** Cached GlyphLayout to avoid per-frame allocation in renderBossHealthBar, renderWaveAnnouncement etc. */
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout cachedLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    /** Track whether batch is currently drawing (to avoid nested begin/end) */
    private boolean batchDrawing = false;

    // ─── Input ────────────────────────────────────────────────────────
    private final TouchData touch            = new TouchData();
    private final Vector2   touchDisplacement = new Vector2();
    private boolean screenTouched = false;

    // ─── Game flow ────────────────────────────────────────────────────
    private boolean isGameOver         = false;
    private boolean isPaused           = false;
    private boolean gameOverPopupShown = false;
    private boolean pausePopupShown    = false;

    // ─── Intro sequence (shown once on New Game) ──────────────────────
    /** True while the intro countdown is running before Wave 1 starts */
    private boolean introActive        = false;
    private float   introTimer         = 0f;
    /** Total intro duration before enemies are allowed to spawn */
    private static final float INTRO_DURATION = 3.5f;
    /** Within the intro, enemies start spawning after this many seconds */
    private static final float INTRO_SPAWN_DELAY = 2.8f;

    // ─── Wave announcement / clear overlay ───────────────────────────
    private float   waveAnnouncementTimer  = 0f;
    private float   waveClearTimer         = 0f;
    private boolean showWaveAnnouncement   = false;
    private boolean showWaveClear          = false;
    private static final float WAVE_ANNOUNCE_DURATION = 2.0f;
    private static final float WAVE_CLEAR_DURATION    = 1.5f;

    /**
     * Delay between all enemies dying and the NEXT wave actually starting to spawn.
     * waveManager.startWave() is called after this delay expires.
     * During the delay, "WAVE CLEAR!" overlay is visible.
     */
    private float   nextWaveDelay      = 0f;
    private boolean pendingNextWave    = false;
    private static final float NEXT_WAVE_DELAY = 2.2f; // seconds between waves

    // ─── Scene2D ─────────────────────────────────────────────────────
    private Stage  uiStage;
    private Skin   uiSkin;
    // HUD text is drawn manually (absolute coords) for reliable layout.
    private String hudScoreText  = "SCORE 0";
    private String hudItemsText  = "0";
    private String hudWeaponText = "LASER Lv:1";

    // ─────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────
    public GamePlayScreen(MainGame game, AudioManager audioManager) {
        this(game, audioManager, false, 1);
    }

    public GamePlayScreen(MainGame game, AudioManager audioManager,
                          boolean continueFromSave, int startWaveId) {
        this.game             = game;
        this.audioManager     = audioManager;
        this.state            = new GameState();
        this.continueFromSave = continueFromSave;
        this.startWaveId      = startWaveId;
    }

    // ─────────────────────────────────────────────────────────────────
    // show()
    // ─────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // ── Rendering setup ──────────────────────────────────────
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0xDEADBEFF);
        pix.fill();
        textureSolid = new Texture(pix);
        pix.dispose();

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        drawer        = new ShapeDrawer(batch, new TextureRegion(textureSolid));

        hudFont = FontUtil.generateRoboto(60);
        hudFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        hudFont.getData().setScale(0.68f);

        overlayFont = FontUtil.generateRoboto(60);
        overlayFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        overlayFont.getData().setScale(1f);

        // ── Wave system ───────────────────────────────────────────
        enemyFactory = new EnemyFactory();
        waveManager  = new WaveManager(enemyFactory);
        WaveConfig cfg = WaveConfig.loadFromFile("data/waves.json");
        waveManager.loadConfig(cfg);

        saveManager = new SaveManager();

        // ── Game state ────────────────────────────────────────────
        state.init(sw, sh);
        state.ship.setAudioManager(audioManager);

        Ship ship = state.ship;
        ship.setX((sw - ship.getWidth()) / 2f);
        ship.setY(sh * 0.12f);
        ship.setLife(Ship.INITIAL_LIFE);

        if (continueFromSave) {
            SaveManager.SaveData saveData = saveManager.load();
            if (saveData != null) {
                if (saveData.maxLife > 0) ship.setMaxLife(saveData.maxLife);
                ship.setLife(saveData.life);
                ship.setActiveWeaponType(saveData.activeWeaponType);
                ship.setWeaponLevel(saveData.weaponLevel0);
                ship.setStockpile(saveData.stockpile0);
                state.scoreTracker.loadState(saveData.score, saveData.stars);
                startWaveId = saveData.savedWave;
                state.currentWaveId = saveData.savedWave;
            }
        } else {
            saveManager.clear();
            state.currentWaveId = startWaveId;
        }

        // Apply DebugConfig if enabled
        if (com.alexei.spaceshooter.utils.DebugConfig.ENABLE_DEBUG) {
            if (!continueFromSave) {
                startWaveId = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WAVE;
                state.currentWaveId = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WAVE;
                waveLoopCount = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WAVE_LOOP_COUNT;
            }
            ship.setLife(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_HP);
            ship.setActiveWeaponType(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WEAPON_TYPE);
            ship.setWeaponLevel(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WEAPON_LEVEL);

            if (com.alexei.spaceshooter.utils.DebugConfig.DEBUG_TEST_SINGLE_ENEMY) {
                startSingleEnemyTestMode(sw, sh);
            }
        }

        for (Weapon w : ship.getWeapons()) w.setAudioManager(audioManager);

        Visual.setVisualEffectsList(state.visualEffects);
        SpaceShooter.setActiveEnemiesList(state.enemies);
        SpaceShooter.setStaticAudioManager(audioManager);
        SpaceShooter.setActiveItemsList(state.items);

        // ── Sequence start ────────────────────────────────────────
        if (continueFromSave) {
            // Continue: skip intro, go straight to wave
            launchWave(state.currentWaveId, false);
        } else {
            // New game: show intro first
            introActive = true;
            introTimer  = 0f;
        }

        // ── Scene2D UI ────────────────────────────────────────────
        createUISkin();
        uiStage = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());
        buildHUD();

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(uiStage);
        mux.addProcessor(new InputAdapter() {
            @Override public boolean touchDown(int x, int y, int pointer, int button) {
                touch.set(x, y, false);
                screenTouched = true;
                Ship s = state.ship;
                touchDisplacement.set(x - s.getX(), -y - s.getY());
                return true;
            }
            @Override public boolean touchUp(int x, int y, int pointer, int button) {
                touch.set(x, y, true);
                screenTouched = false;
                return true;
            }
            @Override public boolean touchDragged(int x, int y, int pointer) {
                if (!screenTouched) return false;
                Ship s = state.ship;
                float newX = x - touchDisplacement.x;
                float newY = -y - touchDisplacement.y;
                if (newX >= 0 && newX <= Gdx.graphics.getWidth() - s.getWidth())
                    s.setX(newX);
                else touchDisplacement.x = x - s.getX();
                if (newY >= 0 && newY <= Gdx.graphics.getHeight() - s.getHeight())
                    s.setY(newY);
                else touchDisplacement.y = -y - s.getY();
                return true;
            }
        });
        Gdx.input.setInputProcessor(mux);

        audioManager.stopAllMusic();
        audioManager.playMusic(SoundName.ActionMusic);
    }

    /**
     * Helper: calls waveManager.startWave() and shows announcement overlay.
     * @param showAnnounce  true = show "WAVE X" text
     */
    private int waveLoopCount = 0;

    private void launchWave(int waveId, boolean showAnnounce) {
        state.currentWaveId = waveId;
        int effectiveWaveId = (waveLoopCount * 20) + waveId;
        waveManager.reset();
        waveManager.startWave(waveId, effectiveWaveId);
        pendingNextWave = false;
        showWaveClear = false;
        if (showAnnounce) {
            showWaveAnnouncement = true;
            waveAnnouncementTimer = 0f;
        }
        audioManager.playSound(SoundName.WaveStart);
        Gdx.app.log("[GamePlay]", "Launched Wave " + waveId + " (Loop " + waveLoopCount + ", Effective " + effectiveWaveId + ")");

        // Boss-wave pity (Section: demo-first design). On boss showcase waves
        // (5, 10, 15, 20) if the player's weapon is too weak (< Lv5), drop a
        // guaranteed energy power-up + a weapon-switch so they can keep up.
        boolean isBossWave = (waveId % 5 == 0);
        if (isBossWave && state.ship.getWeaponLevel() < 5) {
            float sw = Gdx.graphics.getWidth();
            float sh = Gdx.graphics.getHeight();
            float bx = sw / 2f;
            float by = sh * 0.78f;
            com.alexei.spaceshooter.entity.ItemEnergyUpgrade energy =
                    new com.alexei.spaceshooter.entity.ItemEnergyUpgrade(bx - com.alexei.spaceshooter.entity.ItemEnergyUpgrade.ITEM_SIZE / 2f, by);
            energy.setScatterVelocity(90f, 160f);
            state.items.add(energy);

            int activeType = state.ship.getActiveWeaponType();
            com.alexei.spaceshooter.entity.Item weaponItem;
            if (activeType == com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_EXPLOSIVE) {
                weaponItem = new com.alexei.spaceshooter.entity.ItemWeaponUpgradeExplosive(bx - com.alexei.spaceshooter.entity.ItemWeaponUpgradeExplosive.ITEM_SIZE / 2f, by - 90f);
            } else if (activeType == com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_HOMING) {
                weaponItem = new com.alexei.spaceshooter.entity.ItemWeaponUpgradeHoming(bx - com.alexei.spaceshooter.entity.ItemWeaponUpgradeHoming.ITEM_SIZE / 2f, by - 90f);
            } else {
                weaponItem = new com.alexei.spaceshooter.entity.ItemWeaponUpgrade(bx - com.alexei.spaceshooter.entity.ItemWeaponUpgrade.ITEM_SIZE / 2f, by - 90f);
            }
            weaponItem.setScatterVelocity(90f, 160f);
            state.items.add(weaponItem);
            Gdx.app.log("[GamePlay]", "Boss-wave pity: dropped energy + weapon switch for Lv" + state.ship.getWeaponLevel());
        }
    }

    /** Wire weapons, add to the active list, and announce boss entrances. */
    private void addSpawnedEnemies(java.util.List<Unit> newEnemies) {
        for (Unit e : newEnemies) {
            wireEnemyWeapons(e);
            state.enemies.add(e);
            if (e instanceof com.alexei.spaceshooter.entity.EnemyBoss) {
                audioManager.playSound(SoundName.BossWarning);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // UI Skin
    // ─────────────────────────────────────────────────────────────────
    private void createUISkin() {
        uiSkin = CustomUI.getSkin();
    }



    // ─────────────────────────────────────────────────────────────────
    // HUD
    // ─────────────────────────────────────────────────────────────────
    private void buildHUD() {
        // ── Top-right: ONLY the pause button (explicit absolute position) ──
        Button pauseBtn = CustomUI.createButton("\uf04c", null, false);
        pauseBtn.setSize(84, 76);
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        pauseBtn.setPosition(sw - 84 - 14f, sh - 76 - 12f);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                isPaused = true;
                saveCurrentGame();
            }
        });
        uiStage.addActor(pauseBtn);

        // All other HUD text (HP, weapon, WAVE, star, score) is drawn in
        // renderPlayerHudTop() with absolute coordinates for a reliable
        // left / centre / right layout on any device.
    }

    // ─────────────────────────────────────────────────────────────────
    // Dialogs
    // ─────────────────────────────────────────────────────────────────
    private VisDialog createBaseDialog() {
        VisDialog d = new VisDialog("");
        d.pad(80);
        d.getContentTable().defaults().center().pad(18);
        return d;
    }

    private Button primaryBtn(String text) {
        return CustomUI.createButton(null, text, false);
    }

    private Button dangerBtn(String text) {
        return CustomUI.createButton(null, text, true);
    }

    private Label dialogTitle(String text, Color color) {
        return CustomUI.createTitle(text, color);
    }

    private void addDivider(VisDialog dialog, Color color) {
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(color); px.fill();
        Texture t = new Texture(px);
        px.dispose();
        dialog.getContentTable()
              .add(new com.badlogic.gdx.scenes.scene2d.ui.Image(t))
              .width(650).height(2).padTop(8).padBottom(8).row();
    }

    private void showCentered(VisDialog dialog) {
        dialog.show(uiStage, Actions.sequence(Actions.alpha(0f), Actions.alpha(1f, 0.2f)));
        dialog.pack();
        float w = Math.max(dialog.getWidth(), Gdx.graphics.getWidth() * 0.85f);
        dialog.setWidth(w);
        dialog.setPosition(
                (Gdx.graphics.getWidth()  - w) / 2f,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    private void showPauseDialog() {
        if (pausePopupShown) return;
        pausePopupShown = true;

        VisDialog d = createBaseDialog();
        d.getContentTable().add(dialogTitle("PAUSED", new Color(0f, 0.92f, 1f, 1f)))
                           .padBottom(50).row();
        addDivider(d, new Color(0f, 0.6f, 0.8f, 0.6f));

        Button resumeBtn = CustomUI.createButton("\uf04b", "Resume", false);
        resumeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                pausePopupShown = false;
                d.hide(Actions.sequence(Actions.alpha(0f, 0.15f), Actions.removeActor()));
            }
        });
        d.getContentTable().add(resumeBtn).width(720).height(160).padTop(22).row();

        Button menuBtn = dangerBtn("<  Menu");
        menuBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                saveCurrentGame();
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
        });
        d.getContentTable().add(menuBtn).width(720).height(160).padTop(18).row();
        showCentered(d);
    }

    private void showGameOverPopup() {
        if (gameOverPopupShown) return;
        gameOverPopupShown = true;

        long score  = state.scoreTracker.getTotalPoints();
        long killed = state.scoreTracker.getEnemiesKilled();
        long stars  = state.scoreTracker.getStarsCollected();

        // Commit stats ONLY when game session ends (player death on Game Over)
        saveManager.commitGameStats(score, stars);
        long bestScore = saveManager.getHighScore();

        VisDialog d = createBaseDialog();
        d.getContentTable().add(dialogTitle("GAME OVER", new Color(1f, 0.25f, 0.25f, 1f)))
                           .padBottom(24).row();
        addDivider(d, new Color(0.8f, 0.2f, 0.2f, 0.5f));

        Label waveLabel = CustomUI.createTitle("WAVE " + state.currentWaveId + " REACHED", new Color(1f, 0.45f, 0.45f, 1f));
        waveLabel.setFontScale(1.1f);
        d.getContentTable().add(waveLabel).padBottom(26).row();

        // Stats block
        Table stats = new Table();
        stats.defaults().pad(4);
        Label scoreLbl = CustomUI.createTitle("SCORE", new Color(0.55f, 0.6f, 0.8f, 1f));
        scoreLbl.setFontScale(0.9f);
        stats.add(scoreLbl).left().row();
        Label scoreVal = CustomUI.createTitle("" + score, new Color(1f, 0.85f, 0.3f, 1f));
        scoreVal.setFontScale(1.4f);
        stats.add(scoreVal).left().padBottom(10).row();

        Label bestLbl = CustomUI.createTitle("BEST SCORE", new Color(0.55f, 0.6f, 0.8f, 1f));
        bestLbl.setFontScale(0.9f);
        stats.add(bestLbl).left().row();
        Label bestVal = CustomUI.createTitle("" + bestScore, new Color(0.8f, 0.85f, 1f, 1f));
        bestVal.setFontScale(1.2f);
        stats.add(bestVal).left().padBottom(10).row();

        Label crystalsLbl = CustomUI.createTitle("STARS", new Color(0.55f, 0.6f, 0.8f, 1f));
        crystalsLbl.setFontScale(0.9f);
        stats.add(crystalsLbl).left().row();
        Label crystalsVal = CustomUI.createTitle("" + stars, new Color(1f, 0.85f, 0.3f, 1f));
        crystalsVal.setFontScale(1.2f);
        stats.add(crystalsVal).left().padBottom(10).row();

        Label killsLbl = CustomUI.createTitle("ENEMIES DESTROYED", new Color(0.55f, 0.6f, 0.8f, 1f));
        killsLbl.setFontScale(0.9f);
        stats.add(killsLbl).left().row();
        Label killsVal = CustomUI.createTitle("" + killed, new Color(0.8f, 0.85f, 1f, 1f));
        killsVal.setFontScale(1.2f);
        stats.add(killsVal).left().row();

        d.getContentTable().add(stats).padBottom(22).row();

        addDivider(d, new Color(0.8f, 0.2f, 0.2f, 0.4f));

        Button restartBtn = CustomUI.createButton("\uf01e", "RESTART", false);
        restartBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                d.hide(Actions.sequence(Actions.alpha(0f, 0.15f), Actions.removeActor()));
                resetGame();
            }
        });
        d.getContentTable().add(restartBtn).width(720).height(150).padTop(20).row();

        Button menuBtn = dangerBtn("<  Menu");
        menuBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
        });
        d.getContentTable().add(menuBtn).width(720).height(150).padTop(16).row();
        showCentered(d);
    }

    // ─────────────────────────────────────────────────────────────────
    // Reset
    // ─────────────────────────────────────────────────────────────────
    private void resetGame() {
        isGameOver         = false;
        isPaused           = false;
        gameOverPopupShown = false;
        pausePopupShown    = false;

        state.reset();
        waveManager.reset();

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        state.init(sw, sh);
        state.ship.setAudioManager(audioManager);
        state.currentWaveId = 1;

        Ship ship = state.ship;
        ship.setX((sw - ship.getWidth()) / 2f);
        ship.setY(sh * 0.12f);
        ship.setLife(Ship.INITIAL_LIFE);

        if (com.alexei.spaceshooter.utils.DebugConfig.ENABLE_DEBUG) {
            startWaveId = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WAVE;
            state.currentWaveId = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WAVE;
            ship.setLife(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_HP);
            ship.setWeaponLevel(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WEAPON_LEVEL);
        }

        for (Weapon w : ship.getWeapons()) w.setAudioManager(audioManager);

        Visual.setVisualEffectsList(state.visualEffects);
        SpaceShooter.setActiveEnemiesList(state.enemies);
        SpaceShooter.setStaticAudioManager(audioManager);
        SpaceShooter.setActiveItemsList(state.items);
        saveManager.clear();

        uiStage.clear();
        buildHUD();

        showWaveAnnouncement = false;
        showWaveClear        = false;
        pendingNextWave      = false;

        // Show intro for new game
        introActive = true;
        introTimer  = 0f;

        audioManager.stopAllMusic();
        audioManager.playMusic(SoundName.ActionMusic);
    }

    // ─────────────────────────────────────────────────────────────────
    // render()
    // ─────────────────────────────────────────────────────────────────

    /** Batched background pass: nebula + both starfields in a single SpriteBatch. */
    private void renderBackground() {
        batch.begin();
        state.starfield.render(shapeRenderer, batch);
        state.starfield2.render(shapeRenderer, batch);
        batch.end();
    }

    /** ShapeRenderer pass for primitives such as the invulnerability shield. */
    private void renderShapePass() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        state.ship.render(shapeRenderer, batch); // shield circle if invulnerable
        if (shapeRenderer.isDrawing()) shapeRenderer.end();
    }

    /** Single SpriteBatch pass for all textured game entities. */
    private void renderEntityPass() {
        Ship ship = state.ship;
        batch.begin();
        ship.render(shapeRenderer, batch);
        for (Unit      e : state.enemies)      e.render(shapeRenderer, batch);
        for (Item      i : state.items)        i.render(shapeRenderer, batch);
        for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
        for (Visual    v : state.visualEffects) v.render(shapeRenderer, batch);
        batch.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.02f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float dt   = Gdx.graphics.getDeltaTime() * 1000; // ms
        float sw   = Gdx.graphics.getWidth();
        float sh   = Gdx.graphics.getHeight();
        Ship  ship = state.ship;

        // ── Game Over ─────────────────────────────────────────────
        if (isGameOver) {
            state.starfield.update(dt);
            state.starfield2.update(dt);
            renderBackground();
            showGameOverPopup();
            updateHUD();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        // ── Paused ────────────────────────────────────────────────
        if (isPaused) {
            renderBackground();
            renderShapePass();
            showPauseDialog();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        // ── Ship died ─────────────────────────────────────────────
        if (ship.isDead()) {
            touch.handle();
            audioManager.stopSound(SoundName.Alarm);
            audioManager.stopAllMusic();
            isGameOver = true;
            saveCurrentGame();
            if (shapeRenderer.isDrawing()) shapeRenderer.end();
            return;
        }

        // ── Player ALWAYS updates and shoots ──────────────────────
        for (Weapon w : ship.getWeapons()) w.setEnabled(screenTouched);
        ship.update(dt);
        for (Weapon w : ship.getWeapons()) w.update(dt, state.projectiles);
        for (Projectile p : state.projectiles) p.update(dt);

        // ══ INTRO SEQUENCE ════════════════════════════════════════
        if (introActive) {
            introTimer += delta;

            // Still update starfields for visual life
            state.starfield.update(dt);
            state.starfield2.update(dt);

            // Update visual effects (muzzle flashes etc.) so the smoke/shock
            // effects emit particles even during the intro/wave-1 roll-in.
            for (int i = state.visualEffects.size() - 1; i >= 0; i--) {
                Visual v = state.visualEffects.get(i);
                v.update(dt);
                if (v.isDead()) state.visualEffects.remove(i);
            }

            // After spawn delay, actually start the wave (enemies begin spawning)
            if (introTimer >= INTRO_SPAWN_DELAY && !waveManager.isWaveStarted()) {
                waveManager.startWave(state.currentWaveId);
                showWaveAnnouncement = true;
                waveAnnouncementTimer = 0f;
            }

            // Spawn enemies if wave is started (even during intro's tail)
            if (waveManager.isWaveStarted()) {
                addSpawnedEnemies(waveManager.update(dt, sw, sh));
            }

            if (introTimer >= INTRO_DURATION) {
                introActive = false;
            }

            // Render
            renderBackground();
            renderShapePass();

            batch.begin();
            ship.render(shapeRenderer, batch);
            for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
            for (Visual     v : state.visualEffects) v.render(shapeRenderer, batch);
            renderIntroOverlay(sw, sh);
            if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
            batch.end();

            // HUD stays visible during the intro too.
            renderPlayerHudTop(sw, sh);

            updateHUD();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        // ══ BETWEEN-WAVE DELAY ════════════════════════════════════
        if (pendingNextWave) {
            nextWaveDelay -= delta;

            // Update world during delay (items, effects, starfield)
            state.starfield.update(dt);
            state.starfield2.update(dt);
            for (int i = state.visualEffects.size() - 1; i >= 0; i--) {
                Visual v = state.visualEffects.get(i);
                v.update(dt);
                if (v.isDead()) state.visualEffects.remove(i);
            }
            for (int i = state.items.size() - 1; i >= 0; i--) {
                Item item = state.items.get(i);
                item.update(dt);
                if (item.isDead()) { state.items.remove(i); continue; }
                if (item.isColliding(ship) && !item.isPickedUp()) {
                    item.pickUp(); 
                    if (item instanceof com.alexei.spaceshooter.entity.ItemHP) {
                        ship.addLife(1f);
                    } else if (item instanceof com.alexei.spaceshooter.entity.ItemEnergyUpgrade) {
                        ship.upgradeEnergy();
                    } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgradeExplosive) {
                        ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_EXPLOSIVE);
                    } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgradeHoming) {
                        ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_HOMING);
                    } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgrade) {
                        ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_PLASMA);
                    } else {
                        state.scoreTracker.collectStar();
                    }
                }
            }

            if (waveClearTimer < WAVE_CLEAR_DURATION) {
                waveClearTimer += delta;
            }

            if (nextWaveDelay <= 0f) {
                // Launch next wave (endless loop back to wave 1 after completing wave 20)
                boolean hasMore = waveManager.hasMoreWaves();
                int nextId = hasMore ? state.currentWaveId + 1 : 1;
                if (!hasMore) {
                    waveLoopCount++;
                }
                launchWave(nextId, true);
                pendingNextWave = false;
            }

            // Render everything that's still around
            renderBackground();
            renderShapePass();

            batch.begin();
            ship.render(shapeRenderer, batch);
            for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
            for (Item       i : state.items)       i.render(shapeRenderer, batch);
            for (Visual     v : state.visualEffects) v.render(shapeRenderer, batch);
            if (showWaveClear)        renderWaveClear(sw, sh);
            if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
            batch.end();

            // HUD stays visible between waves.
            renderPlayerHudTop(sw, sh);

            updateHUD();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        // ══ PLAYING ═══════════════════════════════════════════════

        // Announcement timer
        if (showWaveAnnouncement) {
            waveAnnouncementTimer += delta;
            if (waveAnnouncementTimer >= WAVE_ANNOUNCE_DURATION) showWaveAnnouncement = false;
        }

        // Spawn
        addSpawnedEnemies(waveManager.update(dt, sw, sh));

        // Score
        state.scoreTracker.update(dt);

        // Starfields
        state.starfield.update(dt);
        state.starfield2.update(dt);

        // Visual effects
        for (int i = state.visualEffects.size() - 1; i >= 0; i--) {
            Visual v = state.visualEffects.get(i);
            v.update(dt);
            if (v.isDead()) state.visualEffects.remove(i);
        }

        // Projectiles already updated at the top

        // Enemies
        for (Unit e : state.enemies) {
            e.update(dt);
            if (!e.hasArrived()) continue; // don't fire while still flying in ("ngậm đạn")
            for (Weapon w : e.getWeapons()) {
                if (w instanceof WeaponEnergyBallA) ((WeaponEnergyBallA) w).setTarget(ship);
                if (w instanceof WeaponSniperBeam)  ((WeaponSniperBeam)  w).setTarget(ship);
                w.update(dt, state.projectiles);
            }
        }

        // Ship already updated at the top

        // Items
        for (int i = state.items.size() - 1; i >= 0; i--) {
            Item item = state.items.get(i);
            item.update(dt);
            if (item.isDead()) { state.items.remove(i); continue; }
            if (item.isColliding(ship) && !item.isPickedUp()) {
                item.pickUp(); 
                if (item instanceof com.alexei.spaceshooter.entity.ItemHP) {
                    ship.addLife(1f);
                } else if (item instanceof com.alexei.spaceshooter.entity.ItemEnergyUpgrade) {
                    ship.upgradeEnergy();
                } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgradeExplosive) {
                    ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_EXPLOSIVE);
                } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgradeHoming) {
                    ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_HOMING);
                } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgrade) {
                    ship.onWeaponPickup(com.alexei.spaceshooter.entity.Ship.WEAPON_TYPE_PLASMA);
                } else {
                    state.scoreTracker.collectStar();
                }
            }
            // Magnetization Rules:
            // - Stars (ItemStar): magnetize from 300px away (d2 <= 90000) at fast 1200f speed.
            // - HP Items (ItemHP): magnetize ONLY when VERY CLOSE within 90px (d2 <= 8100) ("cho sát mới hút được máu").
            // - Weapon Upgrades (PLASMA, EXPLOSIVE, HOMING): NEVER magnetized so players can dodge them!
            if (item instanceof com.alexei.spaceshooter.entity.ItemStar) {
                float d2 = item.squareDistanceToCenter(ship);
                if (d2 <= 90000) {
                    if (item.isMagnetizing()) item.setDirection(ship);
                    else item.magnetize(ship);
                    item.setSpeed(1200f);
                } else if (item.isMagnetizing()) {
                    item.unmagnetize();
                }
            } else if (item instanceof com.alexei.spaceshooter.entity.ItemHP) {
                float d2 = item.squareDistanceToCenter(ship);
                if (d2 <= 8100) { // 90px radius
                    if (item.isMagnetizing()) item.setDirection(ship);
                    else item.magnetize(ship);
                    item.setSpeed(800f);
                } else if (item.isMagnetizing()) {
                    item.unmagnetize();
                }
            } else {
                if (item.isMagnetizing()) item.unmagnetize();
            }
        }

        doCollisionDetection();

        // ── Wave clear check ──────────────────────────────────────
        if (waveManager.isWaveFinished()
                && state.enemies.isEmpty()
                && waveManager.getTotalEnemiesSpawned() > 0
                && !pendingNextWave) {

            waveManager.markWaveCleared();
            saveCurrentGame();
            showWaveClear  = true;
            waveClearTimer = 0f;
            audioManager.playSound(SoundName.WaveClear);
            pendingNextWave = true;
            nextWaveDelay   = NEXT_WAVE_DELAY;
            Gdx.app.log("[GamePlay]", "Wave " + state.currentWaveId + " cleared!");
        }

        // ══ RENDER ════════════════════════════════════════════════

        // Pass 1: Background (batched nebula + starfield)
        renderBackground();

        // Pass 2: Shape primitives (shield circle, etc.)
        renderShapePass();

        // Pass 3: All game entity sprites (Single SpriteBatch pass!)
        renderEntityPass();

        // Pass 4: Overlays & UI
        renderBossHealthBar(sw, sh);
        renderPlayerHudTop(sw, sh);

        batch.begin();
        if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
        if (showWaveClear)        renderWaveClear(sw, sh);
        batch.end();

        updateHUD();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void renderBossHealthBar(float sw, float sh) {
        float totalHp = 0f;
        float totalMaxHp = 0f;
        int bossCount = 0;

        for (Unit enemy : state.enemies) {
            if (enemy instanceof com.alexei.spaceshooter.entity.EnemyBoss && !enemy.isDead()) {
                totalHp += enemy.getLife();
                totalMaxHp += enemy.getMaxLife();
                bossCount++;
            }
        }

        if (bossCount == 0 || totalMaxHp <= 0) return;

        float pct = MathUtils.clamp(totalHp / totalMaxHp, 0f, 1f);

        float barW = sw * 0.70f;
        float barH = 18f;
        float barX = (sw - barW) / 2f;
        float barY = sh - 92f;

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.06f, 0.02f, 0.05f, 0.9f);
        shapeRenderer.rect(barX - 4, barY - 4, barW + 8, barH + 8);
        shapeRenderer.setColor(0.35f, 0.02f, 0.10f, 1.0f);
        shapeRenderer.rect(barX, barY, barW, barH);
        shapeRenderer.setColor(1.0f, 0.15f, 0.35f, 1.0f);
        shapeRenderer.rect(barX, barY, barW * pct, barH);
        shapeRenderer.setColor(1f, 1f, 1f, 0.3f);
        shapeRenderer.rect(barX, barY + barH - 4f, barW * pct, 4f);
        shapeRenderer.end();

        batch.begin();
        String label = (bossCount > 1)
                ? "DREADNOUGHT x" + bossCount
                : "DREADNOUGHT  ·  " + (int) (pct * 100) + "%";
        hudFont.setColor(1f, 0.4f, 0.55f, 1f);
        cachedLayout.setText(hudFont, label);
        hudFont.draw(batch, label, (sw - cachedLayout.width) / 2f, barY + barH + 16f);
        batch.end();
    }



    /**
     * Draws the full gameplay HUD with absolute coordinates so the layout is
     * reliably distributed (never clumped):
     *   LEFT    → HP (red) + weapon label
     *   CENTRE  → WAVE pill
     *   RIGHT   → star currency + score
     *   TOP-RIGHT → pause button (Scene2D, positioned in buildHUD)
     */
    private void renderPlayerHudTop(float sw, float sh) {
        Ship ship = state.ship;

        // ── Wave pill (top-centre) ─────────────────────────────────
        String waveText = "WAVE " + state.currentWaveId;
        cachedLayout.setText(overlayFont, waveText);
        float pillW = cachedLayout.width + 44f;
        float pillH = 32f;
        float pillX = (sw - pillW) / 2f;
        float pillY = sh - 56f;

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.04f, 0.08f, 0.15f, 0.82f);
        shapeRenderer.rect(pillX, pillY, pillW, pillH);
        shapeRenderer.setColor(0f, 0.85f, 1f, 0.9f);
        shapeRenderer.rect(pillX, pillY + pillH - 3f, pillW, 3f);
        shapeRenderer.end();

        // ── Batch pass: all HUD text ───────────────────────────────
        batch.begin();

        // LEFT: HP in red (bottom-left)
        hudFont.setColor(1f, 0.22f, 0.22f, 1f);
        String hpText = "HP " + (int) Math.ceil(ship.getLife()) + "/" + (int) ship.getMaxLife();
        hudFont.draw(batch, hpText, 16f, sh * 0.055f);

        // LEFT: weapon label (cyan, above HP)
        hudFont.setColor(0f, 0.85f, 1f, 1f);
        hudFont.draw(batch, hudWeaponText, 16f, sh * 0.055f + 56f);

        // RIGHT: gold star icon + star count, score below it in white
        com.badlogic.gdx.graphics.g2d.TextureRegion starReg = com.alexei.spaceshooter.utils.TextureRegistry.itemStar;
        if (starReg != null) {
            float iconSize = 34f;
            float iconY = sh * 0.055f + 50f;
            float iconX = sw - iconSize - 16f;
            batch.setColor(1f, 0.85f, 0.3f, 1f); // gold tint
            batch.draw(starReg, iconX, iconY, iconSize, iconSize);
            batch.setColor(Color.WHITE);
            // Count text sits to the left of the icon, right-aligned at iconX.
            hudFont.setColor(1f, 0.88f, 0.2f, 1f);
            cachedLayout.setText(hudFont, hudItemsText);
            hudFont.draw(batch, hudItemsText, iconX - cachedLayout.width - 8f, iconY + 26f);
        } else {
            hudFont.setColor(1f, 0.88f, 0.2f, 1f);
            hudFont.draw(batch, "\u2605 " + hudItemsText, sw - 16f - 60f, sh * 0.055f + 56f);
        }

        // Score — white text, right side
        hudFont.setColor(1f, 1f, 1f, 1f);
        String scoreText = "SCORE " + hudScoreText;
        cachedLayout.setText(hudFont, scoreText);
        hudFont.draw(batch, scoreText, sw - cachedLayout.width - 16f, sh * 0.055f + 6f);

        // CENTRE: WAVE text on the pill
        overlayFont.setColor(0f, 0.92f, 1f, 1f);
        overlayFont.draw(batch, waveText, pillX + 22f, pillY + pillH - 8f);

        batch.end();
    }

    private void renderIntroOverlay(float sw, float sh) {
        float progress = MathUtils.clamp(introTimer / INTRO_DURATION, 0f, 1f);

        float alpha;
        if (progress < 0.2f) alpha = progress / 0.2f;
        else if (progress > 0.8f) alpha = 1f - (progress - 0.8f) / 0.2f;
        else alpha = 1f;

        float pulse = (MathUtils.sin(introTimer * 4f) + 1f) / 2f;

        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha));
        String title = "GET READY!";
        cachedLayout.setText(overlayFont, title);
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.5f);
        overlayFont.draw(batch, title, (sw - cachedLayout.width) / 2f + 3, sh * 0.60f - 3);
        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha * (0.8f + pulse * 0.2f)));
        overlayFont.draw(batch, title, (sw - cachedLayout.width) / 2f, sh * 0.60f);

        hudFont.setColor(new Color(1f, 1f, 1f, alpha * 0.7f));
        String sub = "HOLD SCREEN TO SHOOT  •  DRAG TO MOVE";
        cachedLayout.setText(hudFont, sub);
        hudFont.draw(batch, sub, (sw - cachedLayout.width) / 2f, sh * 0.50f);
    }

    private void renderWaveAnnouncement(float sw, float sh) {
        float progress = MathUtils.clamp(waveAnnouncementTimer / WAVE_ANNOUNCE_DURATION, 0f, 1f);
        float alpha;
        if (progress < 0.2f) alpha = progress / 0.2f;
        else if (progress > 0.75f) alpha = 1f - (progress - 0.75f) / 0.25f;
        else alpha = 1f;

        float slideY = sh * 0.72f + (1f - Interpolation.pow3Out.apply(Math.min(1f, progress * 3f))) * sh * 0.15f;

        String txt = "WAVE " + state.currentWaveId;
        cachedLayout.setText(overlayFont, txt);
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.6f);
        overlayFont.draw(batch, txt, (sw - cachedLayout.width) / 2f + 3, slideY - 3);
        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha));
        overlayFont.draw(batch, txt, (sw - cachedLayout.width) / 2f, slideY);
    }

    private void renderWaveClear(float sw, float sh) {
        float progress = MathUtils.clamp(waveClearTimer / WAVE_CLEAR_DURATION, 0f, 1f);
        float alpha;
        if (progress < 0.15f) alpha = progress / 0.15f;
        else if (progress > 0.7f) alpha = 1f - (progress - 0.7f) / 0.3f;
        else alpha = 1f;

        String txt = "WAVE CLEAR!";
        cachedLayout.setText(overlayFont, txt);
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.5f);
        overlayFont.draw(batch, txt, (sw - cachedLayout.width) / 2f + 3, sh * 0.62f - 3);
        overlayFont.setColor(new Color(1f, 0.88f, 0.1f, alpha));
        overlayFont.draw(batch, txt, (sw - cachedLayout.width) / 2f, sh * 0.62f);
    }

    private void updateHUD() {
        Ship ship = state.ship;
        hudScoreText = "" + state.scoreTracker.getTotalPoints();
        hudItemsText = "" + state.scoreTracker.getStarsCollected();

        String wpnTypeStr = (ship.getActiveWeaponType() == Ship.WEAPON_TYPE_PLASMA) ? "LASER" :
                (ship.getActiveWeaponType() == Ship.WEAPON_TYPE_EXPLOSIVE) ? "BLAST" : "HOMING";
        int stock = ship.getStockpile();
        String stockStr = (stock > 0) ? "  [x" + stock + " stock]" : "";
        hudWeaponText = wpnTypeStr + " Lv:" + ship.getWeaponLevel() + stockStr;
    }

    private void saveCurrentGame() {
        Ship s = state.ship;
        saveManager.save(
                state.currentWaveId,
                state.scoreTracker.getTotalPoints(),
                s.getLife(),
                state.scoreTracker.getStarsCollected(),
                s.getActiveWeaponType(),
                s.getWeaponLevel(0),
                s.getWeaponLevel(1),
                s.getWeaponLevel(2),
                s.getStockpile(0),
                s.getStockpile(1),
                s.getStockpile(2),
                s.getMaxLife()
        );
    }

    private void startSingleEnemyTestMode(float sw, float sh) {
        String enemyType = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_TEST_ENEMY_TYPE;
        int count = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_TEST_ENEMY_COUNT;
        float hpMult = com.alexei.spaceshooter.utils.DebugConfig.DEBUG_TEST_HP_MULTIPLIER;

        com.alexei.spaceshooter.data.wave.SpawnAction action = new com.alexei.spaceshooter.data.wave.SpawnAction();
        action.enemyType = enemyType;
        action.pattern = "GRID";
        action.count = count;
        action.hoverYPct = 0.55f;

        List<Unit> testEnemies = enemyFactory.createFromAction(action, sw, sh, state.currentWaveId);
        for (Unit u : testEnemies) {
            u.setMaxLife(u.getMaxLife() * hpMult);
            u.setLife(u.getMaxLife());
            wireEnemyWeapons(u);
        }
        state.enemies.addAll(testEnemies);
    }

    private void wireEnemyWeapons(Unit enemy) {
        for (Weapon w : enemy.getWeapons()) w.setAudioManager(audioManager);
    }

    // ─────────────────────────────────────────────────────────────────
    // Collision Detection
    // ─────────────────────────────────────────────────────────────────
    public void doCollisionDetection() {
        Ship ship = state.ship;
        int  buf  = GameState.BUFFER_ZONE;

        // Out-of-bounds projectiles
        for (int i = state.projectiles.size() - 1; i >= 0; i--) {
            Projectile p = state.projectiles.get(i);
            if (p.getX() < -p.getWidth() - buf || p.getX() > Gdx.graphics.getWidth() + buf
             || p.getY() < -p.getHeight() - buf || p.getY() > Gdx.graphics.getHeight() + buf) {
                state.projectiles.remove(i);
            }
        }

        // Out-of-bounds enemies (only bottom edge — hovering enemies bounce off sides)
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            if (e.getY() < -e.getHeight() - buf) state.enemies.remove(i);
        }

        // Player projectiles vs enemies
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            for (int j = state.projectiles.size() - 1; j >= 0; j--) {
                Projectile p = state.projectiles.get(j);
                if (p.isShipProjectile() && p.isColliding(e)) {
                    p.doDamage(e);
                    // Piercing projectiles punch through; otherwise remove on hit.
                    if (p.consumePierce()) {
                        state.projectiles.remove(j);
                    }
                    if (e.isDead()) {
                        state.scoreTracker.addEnemyKilled();
                        state.enemies.remove(i);
                        break;
                    }
                }
            }
        }

        // Enemy projectiles vs ship
        for (int j = state.projectiles.size() - 1; j >= 0; j--) {
            Projectile p = state.projectiles.get(j);
            if (!p.isShipProjectile() && p.isColliding(ship)) {
                p.doDamage(ship);
                state.projectiles.remove(j);
            }
        }

        // Enemy vs ship collision (only active when ship is NOT invulnerable)
        if (!ship.isInvulnerable()) {
            for (int i = state.enemies.size() - 1; i >= 0; i--) {
                Unit e = state.enemies.get(i);
                if (e.isColliding(ship)) {
                    e.receiveDamage(ship);
                    ship.receiveDamage(e);
                    if (e.isDead()) {
                        state.enemies.remove(i);
                        state.scoreTracker.addEnemyKilled();
                    }
                    // Ship gained invulnerability from damage; stop processing further enemy collisions this frame
                    if (ship.isInvulnerable()) {
                        break;
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Screen lifecycle
    // ─────────────────────────────────────────────────────────────────
    @Override public void resize(int width, int height) {
        if (uiStage != null) uiStage.getViewport().update(width, height, true);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
    @Override public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        hudFont.dispose();
        overlayFont.dispose();
        if (textureSolid != null) textureSolid.dispose();
        if (uiStage != null) uiStage.dispose();
        if (uiSkin  != null) uiSkin.dispose();
    }
}
