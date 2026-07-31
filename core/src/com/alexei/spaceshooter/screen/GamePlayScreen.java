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

    // ─── Rendering ────────────────────────────────────────────────────
    private ShapeRenderer shapeRenderer;
    private ShapeDrawer   drawer;
    private SpriteBatch   batch;
    private BitmapFont    hudFont;
    private BitmapFont    overlayFont;
    private Texture       textureSolid;

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
    private Label  scoreLabel;
    private Label  healthLabel;
    private Label  itemsLabel;
    private Label  weaponLabel;

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
        hudFont.getData().setScale(0.5f);

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
                ship.setLife(saveData.life);
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
            }
            ship.setLife(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_HP);
            ship.setWeaponLevel(com.alexei.spaceshooter.utils.DebugConfig.DEBUG_START_WEAPON_LEVEL);
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
    private void launchWave(int waveId, boolean showAnnounce) {
        state.currentWaveId = waveId;
        waveManager.reset();
        waveManager.startWave(waveId);
        pendingNextWave = false;
        if (showAnnounce) {
            showWaveAnnouncement = true;
            waveAnnouncementTimer = 0f;
        }
        Gdx.app.log("[GamePlay]", "Launched Wave " + waveId);
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
        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().pad(20);

        Table left = new Table().left();

        Table hpTable = new Table();
        Label hpIcon = new Label("\uf004 ", uiSkin, "icon");
        hpIcon.setColor(new Color(1f, 0.3f, 0.35f, 1f));
        hpIcon.setFontScale(1.0f);
        healthLabel = new Label("5", uiSkin);
        healthLabel.setColor(new Color(1f, 0.3f, 0.35f, 1f));
        healthLabel.setFontScale(1.0f);
        hpTable.add(hpIcon).padRight(5); hpTable.add(healthLabel);
        left.add(hpTable).left().padBottom(12).row();

        Table starTable = new Table();
        Label starIcon = new Label("\uf005 ", uiSkin, "icon");
        starIcon.setColor(new Color(1f, 0.88f, 0f, 1f));
        starIcon.setFontScale(1.0f);
        itemsLabel = new Label("0", uiSkin);
        itemsLabel.setColor(new Color(1f, 0.88f, 0f, 1f));
        itemsLabel.setFontScale(1.0f);
        starTable.add(starIcon).padRight(5); starTable.add(itemsLabel);
        left.add(starTable).left().padBottom(8).row();

        scoreLabel = new Label("Score: 0", uiSkin);
        scoreLabel.setColor(new Color(0f, 1f, 0.65f, 1f));
        scoreLabel.setFontScale(1.0f);
        left.add(scoreLabel).left().row();
        
        weaponLabel = new Label("Wpn Lv: 1", uiSkin);
        weaponLabel.setColor(new Color(0f, 1f, 1f, 1f));
        weaponLabel.setFontScale(1.0f);
        left.add(weaponLabel).left().padTop(8).row();

        hud.add(left).expandX().left();

        // Pause button — TextButton with Unicode pause bars (works on all devices,
        // no external icon library needed).
        Button pauseBtn = CustomUI.createButton("\uf04c", null, false);
        pauseBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                isPaused = true;
                Ship s = state.ship;
                saveManager.save(state.currentWaveId,
                        state.scoreTracker.getTotalPoints(),
                        s.getLife(),
                        state.scoreTracker.getStarsCollected());
            }
        });
        hud.add(pauseBtn).size(110, 100).right().top();
        uiStage.addActor(hud);
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

        VisDialog d = createBaseDialog();
        d.getContentTable().add(dialogTitle("GAME OVER", new Color(1f, 0.25f, 0.25f, 1f)))
                           .padBottom(30).row();
        addDivider(d, new Color(0.8f, 0.2f, 0.2f, 0.5f));

        Label titleLabel = CustomUI.createTitle("WAVE " + state.currentWaveId + " FAILED", new Color(1f, 0.4f, 0.4f, 1f));
        d.getContentTable().add(titleLabel).padBottom(30).row();

        Label detailLbl = CustomUI.createTitle("Killed: " + killed + "    Score: " + score, new Color(0.68f, 0.68f, 0.88f, 1f));
        detailLbl.setFontScale(1.0f);
        d.getContentTable().add(detailLbl).padTop(8).padBottom(30).row();

        addDivider(d, new Color(0.8f, 0.2f, 0.2f, 0.4f));

        Button restartBtn = CustomUI.createButton("\uf01e", "RESTART", false);
        restartBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                d.hide(Actions.sequence(Actions.alpha(0f, 0.15f), Actions.removeActor()));
                resetGame();
            }
        });
        d.getContentTable().add(restartBtn).width(720).height(160).padTop(22).row();

        Button menuBtn = dangerBtn("<  Menu");
        menuBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
        });
        d.getContentTable().add(menuBtn).width(720).height(160).padTop(18).row();
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

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // ── Game Over ─────────────────────────────────────────────
        if (isGameOver) {
            state.starfield.update(dt);
            state.starfield2.update(dt);
            state.starfield.render(shapeRenderer, batch);
            state.starfield2.render(shapeRenderer, batch);
            shapeRenderer.end();
            showGameOverPopup();
            updateHUD();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

        // ── Paused ────────────────────────────────────────────────
        if (isPaused) {
            state.starfield.render(shapeRenderer, batch);
            state.starfield2.render(shapeRenderer, batch);
            ship.render(shapeRenderer, batch);
            shapeRenderer.end();
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
            saveManager.save(state.currentWaveId,
                    state.scoreTracker.getTotalPoints(),
                    ship.getLife(),
                    state.scoreTracker.getStarsCollected());
            shapeRenderer.end();
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

            // After spawn delay, actually start the wave (enemies begin spawning)
            if (introTimer >= INTRO_SPAWN_DELAY && !waveManager.isWaveStarted()) {
                waveManager.startWave(state.currentWaveId);
                showWaveAnnouncement = true;
                waveAnnouncementTimer = 0f;
            }

            // Spawn enemies if wave is started (even during intro's tail)
            if (waveManager.isWaveStarted()) {
                java.util.List<Unit> newEnemies = waveManager.update(dt, sw, sh);
                for (Unit e : newEnemies) { wireEnemyWeapons(e); state.enemies.add(e); }
            }

            if (introTimer >= INTRO_DURATION) {
                introActive = false;
            }

            // Render
            state.starfield.render(shapeRenderer, batch);
            state.starfield2.render(shapeRenderer, batch);
            for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
            ship.render(shapeRenderer, batch);
            for (Visual v : state.visualEffects) v.render(shapeRenderer, batch);
            shapeRenderer.end();

            batch.begin();
            renderIntroOverlay(sw, sh);
            if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
            batch.end();

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
                        ship.addLife(1f); // clamps to maxLife internally (N8)
                    } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgrade) {
                        ship.upgradeWeapon();
                    } else {
                        state.scoreTracker.collectStar();
                    }
                }
            }

            if (waveClearTimer < WAVE_CLEAR_DURATION) {
                waveClearTimer += delta;
            }

            if (nextWaveDelay <= 0f) {
                // Launch next wave
                int nextId = waveManager.hasMoreWaves() ? state.currentWaveId + 1 : 1;
                launchWave(nextId, true);
                pendingNextWave = false;
            }

            // Render everything that's still around
            state.starfield.render(shapeRenderer, batch);
            state.starfield2.render(shapeRenderer, batch);
            for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
            shapeRenderer.end();
            batch.begin();
            for (Item i : state.items) i.render(shapeRenderer, batch);
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            ship.render(shapeRenderer, batch);
            for (Visual v : state.visualEffects) v.render(shapeRenderer, batch);
            shapeRenderer.end();

            batch.begin();
            if (showWaveClear)        renderWaveClear(sw, sh);
            if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
            batch.end();

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
        java.util.List<Unit> newEnemies = waveManager.update(dt, sw, sh);
        for (Unit e : newEnemies) { wireEnemyWeapons(e); state.enemies.add(e); }

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
                    ship.addLife(1f); // clamps to maxLife internally (N8)
                } else if (item instanceof com.alexei.spaceshooter.entity.ItemWeaponUpgrade) {
                    ship.upgradeWeapon();
                } else {
                    state.scoreTracker.collectStar();
                }
            }
            float d2 = item.squareDistanceToCenter(ship);
            // 180 pixels radius = 32400
            if (d2 <= 32400) {
                if (item.isMagnetizing()) item.setDirection(ship);
                else item.magnetize(ship);
            } else if (item.isMagnetizing()) item.unmagnetize();
        }

        doCollisionDetection();

        // ── Wave clear check ──────────────────────────────────────
        if (waveManager.isWaveFinished()
                && state.enemies.isEmpty()
                && waveManager.getTotalEnemiesSpawned() > 0
                && !pendingNextWave) {

            waveManager.markWaveCleared();
            saveManager.save(state.currentWaveId,
                    state.scoreTracker.getTotalPoints(),
                    ship.getLife(),
                    state.scoreTracker.getStarsCollected());

            // Show "WAVE CLEAR!" overlay
            showWaveClear  = true;
            waveClearTimer = 0f;

            // Schedule next wave after delay
            pendingNextWave = true;
            nextWaveDelay   = NEXT_WAVE_DELAY;

            Gdx.app.log("[GamePlay]", "Wave " + state.currentWaveId + " cleared!");
        }

        // ══ RENDER ════════════════════════════════════════════════

        state.starfield.render(shapeRenderer, batch);
        state.starfield2.render(shapeRenderer, batch);

        for (Projectile p : state.projectiles) p.render(shapeRenderer, batch);
        for (Unit      e : state.enemies)     e.render(shapeRenderer, batch);

        shapeRenderer.end();
        batch.begin();
        for (Item i : state.items) i.render(shapeRenderer, batch);
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        ship.render(shapeRenderer, batch);
        for (Visual v : state.visualEffects) v.render(shapeRenderer, batch);

        shapeRenderer.end();

        // Overlays
        batch.begin();
        if (showWaveAnnouncement) renderWaveAnnouncement(sw, sh);
        if (showWaveClear)        renderWaveClear(sw, sh);
        batch.end();

        updateHUD();
        uiStage.act(delta);
        uiStage.draw();
    }

    // ─────────────────────────────────────────────────────────────────
    // Overlay renders
    // ─────────────────────────────────────────────────────────────────

    /**
     * Intro overlay shown when a New Game starts.
     * Shows "SPACE SHOOTER" title + "GET READY!" while the ship sits at the bottom
     * and the starfield scrolls.
     */
    private void renderIntroOverlay(float sw, float sh) {
        float progress = MathUtils.clamp(introTimer / INTRO_DURATION, 0f, 1f);

        // Fade out near the end
        float alpha;
        if (progress < 0.2f) alpha = progress / 0.2f;
        else if (progress > 0.8f) alpha = 1f - (progress - 0.8f) / 0.2f;
        else alpha = 1f;

        // "GET READY!" pulse
        float pulse = (MathUtils.sin(introTimer * 4f) + 1f) / 2f; // 0..1

        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha));
        String title = "GET READY!";
        GlyphLayout gl = new GlyphLayout(overlayFont, title);
        // Shadow
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.5f);
        overlayFont.draw(batch, title, (sw - gl.width) / 2f + 3, sh * 0.60f - 3);
        // Main
        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha * (0.8f + pulse * 0.2f)));
        overlayFont.draw(batch, title, (sw - gl.width) / 2f, sh * 0.60f);

        // Sub-text: "TAP AND HOLD TO SHOOT"
        hudFont.setColor(new Color(1f, 1f, 1f, alpha * 0.7f));
        String sub = "HOLD SCREEN TO SHOOT  •  DRAG TO MOVE";
        GlyphLayout sl = new GlyphLayout(hudFont, sub);
        hudFont.draw(batch, sub, (sw - sl.width) / 2f, sh * 0.50f);
    }

    /** "WAVE X" slide-in — no dark background, text only */
    private void renderWaveAnnouncement(float sw, float sh) {
        float progress = MathUtils.clamp(waveAnnouncementTimer / WAVE_ANNOUNCE_DURATION, 0f, 1f);
        float alpha;
        if (progress < 0.2f) alpha = progress / 0.2f;
        else if (progress > 0.75f) alpha = 1f - (progress - 0.75f) / 0.25f;
        else alpha = 1f;

        float slideY = sh * 0.72f + (1f - Interpolation.pow3Out.apply(Math.min(1f, progress * 3f))) * sh * 0.15f;

        String txt = "WAVE " + state.currentWaveId;
        GlyphLayout gl = new GlyphLayout(overlayFont, txt);
        // Shadow
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.6f);
        overlayFont.draw(batch, txt, (sw - gl.width) / 2f + 3, slideY - 3);
        // Text
        overlayFont.setColor(new Color(0f, 0.92f, 1f, alpha));
        overlayFont.draw(batch, txt, (sw - gl.width) / 2f, slideY);
    }

    /** "WAVE CLEAR!" brief text — shown during between-wave delay */
    private void renderWaveClear(float sw, float sh) {
        float progress = MathUtils.clamp(waveClearTimer / WAVE_CLEAR_DURATION, 0f, 1f);
        float alpha;
        if (progress < 0.15f) alpha = progress / 0.15f;
        else if (progress > 0.7f) alpha = 1f - (progress - 0.7f) / 0.3f;
        else alpha = 1f;

        String txt = "WAVE CLEAR!";
        GlyphLayout gl = new GlyphLayout(overlayFont, txt);
        overlayFont.setColor(0f, 0f, 0f, alpha * 0.5f);
        overlayFont.draw(batch, txt, (sw - gl.width) / 2f + 3, sh * 0.62f - 3);
        overlayFont.setColor(new Color(1f, 0.88f, 0.1f, alpha));
        overlayFont.draw(batch, txt, (sw - gl.width) / 2f, sh * 0.62f);
    }

    private void updateHUD() {
        Ship ship = state.ship;
        scoreLabel.setText("Score: " + state.scoreTracker.getTotalPoints());
        itemsLabel.setText("" + state.scoreTracker.getStarsCollected());
        healthLabel.setText("" + (int) ship.getLife());
        weaponLabel.setText("Wpn Lv: " + ship.getWeaponLevel());
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
                    state.projectiles.remove(j);
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

        // Enemy vs ship collision
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            if (e.isColliding(ship)) {
                e.receiveDamage(ship);
                ship.receiveDamage(e);
                if (e.isDead()) {
                    state.enemies.remove(i);
                    state.scoreTracker.addEnemyKilled();
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
