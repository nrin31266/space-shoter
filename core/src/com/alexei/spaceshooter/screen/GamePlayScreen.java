package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.SpaceShooter;
import com.alexei.spaceshooter.effect.*;
import com.alexei.spaceshooter.entity.*;
import com.alexei.spaceshooter.manager.AudioManager;
import com.alexei.spaceshooter.manager.GameState;
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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GamePlayScreen implements Screen {
    private final MainGame game;
    private final AudioManager audioManager;
    private final GameState state;

    private ShapeRenderer shapeRenderer;
    private ShapeDrawer drawer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture textureSolid;

    private TouchData touch = new TouchData();
    private Vector2 touchDisplacement = new Vector2(0, 0);
    private boolean screenTouched = false;

    private boolean isGameOver = false;
    private boolean isPaused = false;
    private boolean gameOverPopupShown = false;
    private boolean pausePopupShown = false;

    // Scene2D
    private Stage uiStage;
    private Skin uiSkin;
    private Window gameOverWindow;
    private Window pauseWindow;
    private Label scoreLabel;
    private Label healthLabel;
    private Label itemsLabel;

    public GamePlayScreen(MainGame game, AudioManager audioManager) {
        this.game = game;
        this.audioManager = audioManager;
        this.state = new GameState();
    }

    @Override
    public void show() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // init rendering
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(0xDEADBEFF);
        pix.fill();
        textureSolid = new Texture(pix);
        TextureRegion textureRegion = new TextureRegion(textureSolid);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        drawer = new ShapeDrawer(batch, textureRegion);

        // init fonts
        float menuFontScale = 2f;
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(menuFontScale, menuFontScale);

        // init game state
        state.init(screenWidth, screenHeight);
        state.ship.setAudioManager(audioManager);

        Ship ship = state.ship;
        ship.setX(screenWidth / 2f);
        ship.setY(screenWidth / 8f);
        ship.setLife(ship.getMaxLife());

        // wire weapons to audio manager
        for (Weapon w : ship.getWeapons()) {
            w.setAudioManager(audioManager);
        }

        // set visual effects list on Visual (bridge pattern)
        Visual.setVisualEffectsList(state.visualEffects);

        // set active enemies list for projectile homing
        SpaceShooter.setActiveEnemiesList(state.enemies);

        // set static audio manager for legacy SpaceShooter.playSound calls
        SpaceShooter.setStaticAudioManager(audioManager);

        // set items list for Unit.dropStars()
        SpaceShooter.setActiveItemsList(state.items);

        // Scene2D UI
        createUISkin();
        uiStage = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());
        buildHUD();

        // set up input with multiplexer so Stage buttons + ship dragging both work
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button) {
                touch.set(x, y, false);
                screenTouched = true;
                Ship s = state.ship;
                touchDisplacement.set(x - s.getX(), -y - s.getY());
                return true;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                touch.set(x, y, true);
                screenTouched = false;
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer) {
                if (screenTouched) {
                    Ship s = state.ship;
                    if (x - touchDisplacement.x >= 0 && x - touchDisplacement.x <= Gdx.graphics.getWidth() - s.getWidth())
                        s.setX(x - touchDisplacement.x);
                    else
                        touchDisplacement.set(x - s.getX(), touchDisplacement.y);
                    if (-y - touchDisplacement.y >= 0 && -y - touchDisplacement.y <= Gdx.graphics.getHeight() - s.getHeight())
                        s.setY(-y - touchDisplacement.y);
                    else
                        touchDisplacement.set(touchDisplacement.x, -y - s.getY());
                }
                return true;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        audioManager.stopAllMusic();
        audioManager.playMusic(SoundName.ActionMusic);
    }

    private void createUISkin() {
        uiSkin = new Skin();

        Pixmap pix = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        uiSkin.add("white", new Texture(pix));
        pix.dispose();

        BitmapFont uiFont = new BitmapFont();
        uiFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        uiFont.getData().setScale(1f);
        uiFont.setUseIntegerPositions(false);
        uiSkin.add("default-font", uiFont);

        Label.LabelStyle labelStyle = new Label.LabelStyle(uiFont, Color.WHITE);
        uiSkin.add("default", labelStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = uiSkin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.down = uiSkin.newDrawable("white", Color.GRAY);
        btnStyle.over = uiSkin.newDrawable("white", Color.LIGHT_GRAY);
        btnStyle.font = uiFont;
        btnStyle.fontColor = Color.GREEN;
        btnStyle.downFontColor = Color.YELLOW;
        uiSkin.add("btn", btnStyle);

        // window style for Dialog/Window (required to prevent crash)
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = uiFont;
        windowStyle.background = uiSkin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 0.95f));
        uiSkin.add("default", windowStyle);
    }

    private void buildHUD() {
        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().pad(20);

        // Score, HP, Coins on the left
        Table leftTable = new Table();
        leftTable.left();

        // HP row - Text only
        healthLabel = new Label("Health: 5", uiSkin);
        healthLabel.setColor(Color.RED);
        healthLabel.setFontScale(3.5f);
        leftTable.add(healthLabel).left().padBottom(10).row();

        // Coins row - Text only
        itemsLabel = new Label("Coins: 0", uiSkin);
        itemsLabel.setColor(Color.YELLOW);
        itemsLabel.setFontScale(3.5f);
        leftTable.add(itemsLabel).left().padBottom(5).row();

        // Score
        scoreLabel = new Label("Score: 0", uiSkin);
        scoreLabel.setColor(Color.GREEN);
        scoreLabel.setFontScale(3.5f);
        leftTable.add(scoreLabel).left().row();

        hud.add(leftTable).expandX().left();

        // Pause button on the right - Smaller
        Drawable pauseIcon = UIIcons.createPauseIcon(40, Color.WHITE);
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle(uiSkin.get("btn", TextButton.TextButtonStyle.class));
        pauseStyle.imageUp = pauseIcon;
        ImageButton pauseBtn = new ImageButton(pauseStyle);
        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = true;
            }
        });
        hud.add(pauseBtn).size(80, 80).right().top();

        uiStage.addActor(hud);
    }

    private void showPauseDialog() {
        if (pausePopupShown) return;
        pausePopupShown = true;

        VisDialog dialog = new VisDialog("");
        dialog.pad(80);
        dialog.getContentTable().defaults().center().pad(25);

        VisLabel titleLabel = new VisLabel("PAUSED");
        titleLabel.setColor(Color.CYAN);
        titleLabel.setFontScale(6f);
        titleLabel.setAlignment(Align.center);
        dialog.getContentTable().add(titleLabel).padBottom(60).row();

        VisTextButton resumeBtn = new VisTextButton("  Resume  ");
        resumeBtn.getLabel().setFontScale(2.5f);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                pausePopupShown = false;
                dialog.hide();
            }
        });
        dialog.getContentTable().add(resumeBtn).width(600).height(150).row();

        VisTextButton menuBtn = new VisTextButton("  Menu  ");
        menuBtn.getLabel().setFontScale(2.5f);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
        });
        dialog.getContentTable().add(menuBtn).width(600).height(150).row();

        dialog.show(uiStage, null);
        dialog.setWidth(Gdx.graphics.getWidth() * 0.85f);
        dialog.setPosition(
                (Gdx.graphics.getWidth() - dialog.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    private void showGameOverPopup() {
        if (gameOverPopupShown) return;
        gameOverPopupShown = true;

        VisDialog dialog = new VisDialog("");
        dialog.pad(80);
        dialog.getContentTable().defaults().center().pad(25);

        long score = state.scoreTracker.getTotalPoints();
        long killed = state.scoreTracker.getEnemiesKilled();
        long stars = state.scoreTracker.getStarsCollected();

        VisLabel titleLabel = new VisLabel("GAME OVER");
        titleLabel.setColor(Color.RED);
        titleLabel.setFontScale(7f);
        titleLabel.setAlignment(Align.center);
        dialog.getContentTable().add(titleLabel).padBottom(60).row();

        VisLabel scoreText = new VisLabel("Score: " + score);
        scoreText.setColor(Color.YELLOW);
        scoreText.setFontScale(4f);
        scoreText.setAlignment(Align.center);
        dialog.getContentTable().add(scoreText).row();

        // Details row
        VisLabel detailText = new VisLabel("Killed: " + killed + "    Coins: " + stars);
        detailText.setColor(Color.LIGHT_GRAY);
        detailText.setFontScale(3f);
        detailText.setAlignment(Align.center);
        dialog.getContentTable().add(detailText).row();

        VisTextButton replayBtn = new VisTextButton("  Retry  ");
        replayBtn.getLabel().setFontScale(2.5f);
        replayBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                resetGame();
            }
        });
        dialog.getContentTable().add(replayBtn).width(600).height(150).row();

        VisTextButton menuBtn = new VisTextButton("  Menu  ");
        menuBtn.getLabel().setFontScale(2.5f);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, audioManager));
            }
        });
        dialog.getContentTable().add(menuBtn).width(600).height(150).row();

        dialog.show(uiStage, null);
        dialog.setWidth(Gdx.graphics.getWidth() * 0.9f);
        dialog.setPosition(
                (Gdx.graphics.getWidth() - dialog.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - dialog.getHeight()) / 2f);
    }

    private void resetGame() {
        isGameOver = false;
        isPaused = false;
        gameOverPopupShown = false;
        pausePopupShown = false;
        state.reset();

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        state.init(sw, sh);
        state.ship.setAudioManager(audioManager);

        Ship ship = state.ship;
        ship.setX(sw / 2f);
        ship.setY(sw / 8f);
        ship.setLife(ship.getMaxLife());

        for (Weapon w : ship.getWeapons()) {
            w.setAudioManager(audioManager);
        }

        Visual.setVisualEffectsList(state.visualEffects);
        SpaceShooter.setActiveEnemiesList(state.enemies);
        SpaceShooter.setStaticAudioManager(audioManager);
        SpaceShooter.setActiveItemsList(state.items);

        if (gameOverWindow != null) {
            gameOverWindow.remove();
            gameOverWindow = null;
        }
        uiStage.clear();
        buildHUD();

        audioManager.stopAllMusic();
        audioManager.playMusic(SoundName.ActionMusic);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float deltaTime = Gdx.graphics.getDeltaTime() * 1000;
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        Ship ship = state.ship;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (isGameOver) {
            // still render starfields behind the popup
            state.starfield.update(deltaTime);
            state.starfield2.update(deltaTime);
            state.starfield.render(shapeRenderer, batch);
            state.starfield2.render(shapeRenderer, batch);
            shapeRenderer.end();

            showGameOverPopup();
            updateHUD();
            uiStage.act(delta);
            uiStage.draw();
            return;
        }

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

        // check if ship is dead
        if (ship.isDead()) {
            touch.handle();
            audioManager.stopSound(SoundName.Alarm);
            audioManager.stopAllMusic();
            isGameOver = true;
            shapeRenderer.end();
            return;
        }

        // spawn enemies
        state.enemySpawnTimer.update(deltaTime);
        if (state.enemySpawnTimer.isTimerElapsed()) {
            Unit enemy = new EnemyShipA();
            enemy.setX(MathUtils.random(0, screenWidth - enemy.getWidth()));
            enemy.setY(screenHeight + 1);
            // wire up EnergyBall weapons with target
            wireEnemyWeapons(enemy);
            state.enemies.add(enemy);

            if (MathUtils.random() <= 0.2f) {
                enemy = new EnemyShipB();
                enemy.setX(MathUtils.random(0, screenWidth - enemy.getWidth()));
                enemy.setY(screenHeight + 1);
                wireEnemyWeapons(enemy);
                state.enemies.add(enemy);
            }
        }

        // update score
        state.scoreTracker.update(deltaTime);

        // update starfields
        state.starfield.update(deltaTime);
        state.starfield2.update(deltaTime);

        // update visual effects
        for (int i = state.visualEffects.size() - 1; i >= 0; i--) {
            Visual v = state.visualEffects.get(i);
            v.update(deltaTime);
            if (v.isDead()) state.visualEffects.remove(i);
        }

        // update projectiles
        for (Projectile p : state.projectiles) {
            p.update(deltaTime);
        }

        // update enemies (and their weapons)
        for (Unit e : state.enemies) {
            e.update(deltaTime);
            // update enemy weapons with current target (ship)
            for (Weapon w : e.getWeapons()) {
                if (w instanceof WeaponEnergyBallA) {
                    ((WeaponEnergyBallA) w).setTarget(ship);
                }
                w.update(deltaTime, state.projectiles);
            }
        }

        // update ship (and its weapons)
        ship.update(deltaTime);
        for (Weapon w : ship.getWeapons()) {
            w.update(deltaTime, state.projectiles);
        }

        // update items
        for (int i = state.items.size() - 1; i >= 0; i--) {
            Item item = state.items.get(i);
            item.update(deltaTime);
            if (item.isDead()) {
                state.items.remove(i);
                continue;
            }
            if (item.isColliding(ship) && !item.isPickedUp()) {
                item.pickUp();
                state.scoreTracker.collectStar();
            }
            if (item.squareDistanceToCenter(ship) <= 90000) {
                if (item.isMagnetizing()) {
                    item.setDirection(ship);
                } else {
                    item.magnetize(ship);
                }
            } else if (item.squareDistanceToCenter(ship) > 90000 && item.isMagnetizing()) {
                item.unmagnetize();
            }
        }

        doCollisionDetection();

        // RENDER
        state.starfield.render(shapeRenderer, batch);
        state.starfield2.render(shapeRenderer, batch);

        for (Projectile p : state.projectiles) {
            p.render(shapeRenderer, batch);
        }
        for (Unit e : state.enemies) {
            e.render(shapeRenderer, batch);
        }

        // render items with ShapeDrawer
        shapeRenderer.end();
        batch.begin();
        for (Item i : state.items) {
            i.render(shapeRenderer, batch);
        }
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // render ship
        ship.render(shapeRenderer, batch);

        // render visual effects
        for (Visual v : state.visualEffects) {
            v.render(shapeRenderer, batch);
        }

        shapeRenderer.end();

        // Scene2D overlay
        updateHUD();
        uiStage.act(delta);
        uiStage.draw();
    }

    private void updateHUD() {
        Ship ship = state.ship;
        scoreLabel.setText("Score: " + state.scoreTracker.getTotalPoints());
        itemsLabel.setText("Coins: " + state.scoreTracker.getStarsCollected());
        healthLabel.setText("Health: " + (int)ship.getLife());
    }

    private void wireEnemyWeapons(Unit enemy) {
        for (Weapon w : enemy.getWeapons()) {
            w.setAudioManager(audioManager);
        }
    }

    public void doCollisionDetection() {
        Ship ship = state.ship;
        int bufferZone = GameState.BUFFER_ZONE;

        // remove out-of-bounds projectiles
        for (int i = state.projectiles.size() - 1; i >= 0; i--) {
            Projectile p = state.projectiles.get(i);
            if (p.getX() < -p.getWidth() - bufferZone || p.getX() > Gdx.graphics.getWidth() + bufferZone
                    || p.getY() < -p.getHeight() - bufferZone || p.getY() > Gdx.graphics.getHeight() + bufferZone) {
                state.projectiles.remove(i);
            }
        }

        // remove out-of-bounds enemies
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            if (e.getX() < -e.getWidth() - bufferZone || e.getX() > Gdx.graphics.getWidth() + bufferZone
                    || e.getY() < -e.getHeight() - bufferZone || e.getY() > Gdx.graphics.getHeight() + bufferZone) {
                state.enemies.remove(i);
            }
        }

        // player projectiles vs enemies
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            for (int j = state.projectiles.size() - 1; j >= 0; j--) {
                Projectile p = state.projectiles.get(j);
                if (p.isShipProjectile()) {
                    if (p.isColliding(e)) {
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
        }

        // enemy projectiles vs player ship
        for (int j = state.projectiles.size() - 1; j >= 0; j--) {
            Projectile p = state.projectiles.get(j);
            if (!p.isShipProjectile()) {
                if (p.isColliding(ship)) {
                    p.doDamage(ship);
                    state.projectiles.remove(j);
                }
            }
        }

        // enemy ships vs player ship
        for (int i = state.enemies.size() - 1; i >= 0; i--) {
            Unit e = state.enemies.get(i);
            if (e.isColliding(ship)) {
                e.receiveDamage(ship);
                ship.receiveDamage(e);
                if (e.isDead()) {
                    state.enemies.remove(e);
                    state.scoreTracker.addEnemyKilled();
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (uiStage != null) uiStage.getViewport().update(width, height, true);
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
        font.dispose();
        if (textureSolid != null) textureSolid.dispose();
        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();
    }
}
