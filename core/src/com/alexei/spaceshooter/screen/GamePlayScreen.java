package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.manager.CampaignStore;
import com.alexei.spaceshooter.manager.CampaignWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public final class GamePlayScreen extends ScreenAdapter {
    private final MainGame game;
    public final CampaignWorld world;
    private final CampaignRenderer renderer;
    private final CampaignUi ui;
    private final Stage stage = new Stage(new ExtendViewport(1280, 720));
    private final Table hud = new Table(), overlay = new Table();
    private final Label status, score, shard, heat, title, bossStatus, effects;
    private final TextButton weaponButton, missileButton;
    private final Image heatFill, heatFrame, heatVent, weaponIcon, bossFill, shieldIcon, overdriveIcon, supportIcon;
    private final Label overdriveTimer, supportTimer;
    private final com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable[] ventFrames;
    private final Table bossPanel = new Table();
    private int shownEffects = -1;
    private final String[] weaponNames = {"Pulse", "Plasma", "Homing"};
    private Table heatGauge, lifePanel, indicatorPanel, coinPanel;
    private TextButton pauseButton;
    private final Image[] heatNotches = new Image[4];
    private Label bossPercentage;
    private boolean settingsOpen;
    private Runnable confirmAction;
    private String confirmCopy;
    private final CampaignStore.State initial;
    private boolean paused;
    private int pointer = -1, lastX, lastY, shownRevision = -1, shownHeat = -1;
    private CampaignWorld.Stage shownStage;
    private float accumulator, pendingDragX, pendingDragY, tapStarted = -1;
    private Label continueHint;

    public GamePlayScreen(MainGame game, CampaignStore.State state, boolean replay) {
        this(game, state, replay, false);
    }

    public GamePlayScreen(MainGame game, CampaignStore.State state, boolean replay, boolean withIntro) {
        this.game = game;
        initial = game.copy(state);
        ui = new CampaignUi(game.assets);
        world = new CampaignWorld(game.assets.catalog, state, replay, new CampaignWorld.Listener() {
            public void sound(int cue) { game.audio.cue(cue); }
            public void checkpoint(CampaignStore.State checkpoint) { game.checkpoint(checkpoint); }
            public int creditShards(int cumulative, int balance) { return game.creditShards(cumulative); }
        });
        if (withIntro) world.introduceMission();
        renderer = new CampaignRenderer(game.assets, world);
        status = ui.hudLabel(""); score = ui.hudLabel(""); shard = ui.hudLabel(""); heat = ui.hudLabel(""); title = ui.title("");
        shard.setColor(com.badlogic.gdx.graphics.Color.GOLD);
        title.setFontScale(.5f);
        hud.setFillParent(true); hud.top().pad(12);
        Table thermal = new Table();
        thermal.add(heat).left().row();
        Stack gauge = new Stack();
        heatFill = new Image(ui.icon("UI/ui-heat-fill.png"));
        gauge.add(heatFill);
        heatFrame = new Image(ui.icon("UI/ui-heat-housing.png")); gauge.add(heatFrame);
        Table notches = new Table();
        for (int i = 0; i < 4; i++) {
            heatNotches[i] = new Image(ui.icon("UI/ui-heat-notch.png"));
            notches.add(heatNotches[i]).width(1.6f).height(7.2f).padLeft(i == 0 ? 0 : 39.36f);
        }
        gauge.add(notches);
        thermal.add(gauge).size(205, 20);
        com.badlogic.gdx.utils.JsonValue hudConfig = null;
        for (com.badlogic.gdx.utils.JsonValue doc : game.assets.catalog.record("Scenes/02_Gameplay.unity").get("documents"))
            if (doc.get("data").has("heatVentFrames")) hudConfig = doc.get("data");
        if (hudConfig == null) throw new IllegalArgumentException("Missing HUD configuration");
        String[] ventKeys = CampaignWorld.frames(hudConfig.get("heatVentFrames"));
        ventFrames = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable[ventKeys.length];
        for (int i = 0; i < ventKeys.length; i++) ventFrames[i] = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(game.assets.sprite(ventKeys[i]).region);
        heatVent = new Image(ventFrames[0]); thermal.add(heatVent).size(23).padLeft(4);
        hud.add(thermal).left().width(218);
        Table totals = new Table();
        totals.add(score).minWidth(100).padRight(48);
        totals.add(shard).padRight(8);
        totals.add(new Image(ui.icon("UI/ui-shard.png"))).size(32);
        hud.add(totals).center().expandX();
        pauseButton = ui.button("", () -> setPaused(true));
        pauseButton.clearChildren(); pauseButton.add(new Image(ui.icon("UI/ui-icon-pause.png"))).size(42);
        hud.add(pauseButton).width(58).height(58).row();
        bossStatus = ui.hudLabel("");
        bossPercentage = ui.hudLabel("");
        bossPanel.add(bossStatus).expandX().left();
        bossPanel.add(bossPercentage).right().row();
        Stack bossGauge = new Stack();
        bossGauge.add(new Image(ui.panel));
        bossFill = new Image(ui.icon("UI/ui-heat-fill.png")); bossFill.setColor(com.badlogic.gdx.graphics.Color.CORAL);
        bossGauge.add(bossFill);
        bossPanel.add(bossGauge).colspan(2).growX().height(11.2f).padTop(3.2f);
        bossPanel.top();
        bossPanel.setTouchable(Touchable.disabled);
        hud.add(title).colspan(3).padTop(20).expandY().top().row();
        effects = ui.hudLabel("");
        Table pickupCue = new Table(); pickupCue.setFillParent(true); pickupCue.bottom().padBottom(120);
        pickupCue.add(effects); pickupCue.setTouchable(Touchable.disabled);
        hud.addActor(pickupCue);
        Table indicators = new Table();
        shieldIcon = new Image(ui.icon("UI/ui-shield.png"));
        overdriveIcon = new Image(ui.icon("UI/ui-overdrive.png"));
        supportIcon = new Image(ui.icon("UI/ui-support.png"));
        overdriveTimer = ui.hudLabel(""); supportTimer = ui.hudLabel("");
        indicators.add(shieldIcon).size(38).pad(6); indicators.add(overdriveIcon).size(38).pad(6); indicators.add(supportIcon).size(38).pad(6).row();
        indicators.add(); indicators.add(overdriveTimer); indicators.add(supportTimer);
        hud.add(indicators).colspan(3).left().padBottom(6).row();
        Table life = new Table();
        life.add(new Image(ui.icon("UI/ui-life.png"))).size(48);
        life.add(status).padLeft(12);
        hud.add(life).left();
        hud.add().expandX();
        Table controls = new Table();
        missileButton = ui.button("", () -> {
            if (paused) return;
            if (!world.missile()) game.audio.cue(34);
            else if (game.profile.vibrationEnabled) Gdx.input.vibrate(500);
        });
        missileButton.clearChildren();
        missileButton.add(new Image(ui.icon("UI/ui-ammo.png"))).size(51.2f).padRight(0);
        missileButton.add(missileButton.getLabel()); missileButton.getLabel().setFontScale(.8f);
        controls.add(missileButton).size(122, 67).padBottom(10).row();
        weaponButton = ui.button("", () -> {
            if (!paused && world.gameplayInput() && world.player.cycle()) { world.revision++; game.audio.cue(32); }
        });
        weaponIcon = new Image(ui.icon("UI/ui-weapon-pulse.png"));
        weaponButton.clearChildren(); weaponButton.add(weaponIcon).size(57.6f).padRight(0);
        weaponButton.add(weaponButton.getLabel()); weaponButton.getLabel().setFontScale(.8f);
        weaponButton.getLabel().setColor(com.badlogic.gdx.graphics.Color.CYAN);
        TextButton.TextButtonStyle controlStyle = new TextButton.TextButtonStyle(ui.buttonStyle);
        controlStyle.up = ui.panel;
        weaponButton.setStyle(controlStyle); missileButton.setStyle(controlStyle); pauseButton.setStyle(controlStyle);
        controls.add(weaponButton).size(122, 74);
        hud.add(controls).right().bottom();
        hud.setTouchable(Touchable.childrenOnly);
        heatGauge = thermal; lifePanel = life; indicatorPanel = indicators;
        coinPanel = new Table(); coinPanel.add(shard).padRight(6.4f); coinPanel.add(new Image(ui.icon("UI/ui-shard.png"))).size(32);
        hud.clearChildren();
        hud.addActor(heatGauge); hud.addActor(score); hud.addActor(coinPanel); hud.addActor(pauseButton);
        hud.addActor(lifePanel); hud.addActor(indicatorPanel); hud.addActor(missileButton); hud.addActor(weaponButton);
        hud.addActor(effects); hud.addActor(title);
        title.setAlignment(Align.center); score.setAlignment(Align.center); effects.setAlignment(Align.center);
        stage.addActor(hud);
        stage.addActor(bossPanel);
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.childrenOnly);
        stage.addActor(overlay);
    }

    private void clearPointer() { tapStarted = -1; pointer = -1; world.held = false; pendingDragX = pendingDragY = 0; }

    private void setPaused(boolean value) {
        if (world.stage == CampaignWorld.Stage.GAME_OVER || world.stage == CampaignWorld.Stage.RESULT) return;
        if (!value && !game.flushPending()) { refreshOverlay(); return; }
        paused = value; settingsOpen = false; confirmAction = null; clearPointer(); accumulator = 0;
        refreshOverlay();
    }

    private void menu() {
        if (!game.flushPending()) { refreshOverlay(); return; }
        game.audio.cue(33); game.setScreen(new MainMenuScreen(game));
    }

    private void refreshOverlay() {
        overlay.clear();
        if (paused && settingsOpen) { showSettings(); return; }
        if (paused && confirmAction != null) { showConfirmation(); return; }
        if (paused) { showPause(); return; }
        if (!paused && world.awaitingContinue()) {
            Table briefing = new Table(); briefing.left().bottom().padLeft(83).padBottom(270);
            Label location = ui.title(world.announcement); location.setFontScale(.5f);
            briefing.add(location).left().row();
            Label description = ui.hudLabel(world.stage == CampaignWorld.Stage.REGION_TRAVEL ? "Hành trình tiếp tục về phía trước." : world.mission.getString("MissionBriefing"));
            description.setFontScale(.72f); description.setWrap(true);
            briefing.add(description).width(820).left().padTop(12);
            overlay.add(briefing).grow();
            Table hint = new Table(); hint.setFillParent(true); hint.bottom().padBottom(32);
            hint.setTouchable(Touchable.disabled);
            continueHint = ui.hudLabel("Chạm để tiếp tục");
            hint.add(continueHint); overlay.addActor(hint);
            return;
        }
        continueHint = null;
        if (!paused && world.stage != CampaignWorld.Stage.RESULT && world.stage != CampaignWorld.Stage.GAME_OVER) return;
        Table panel = new Table(); panel.setBackground(ui.modal); panel.pad(34); panel.defaults().pad(9);
        panel.add(ui.title(paused ? "TẠM DỪNG" : world.announcement)).width(850).row();
        if (game.saveError != null) {
            Label error = ui.label("Không lưu được checkpoint: " + game.saveError); error.setWrap(true);
            panel.add(error).width(850).row();
        }
        if (game.saveError != null) panel.add(ui.button("THỬ LƯU LẠI", () -> { game.flushPending(); refreshOverlay(); })).width(360).height(64).row();
        {
            panel.add(ui.label("ĐIỂM NHIỆM VỤ " + world.save.missionScore + "   ·   SHARD " + world.save.shard)).row();
            panel.add(ui.label("THỜI GIAN " + (int)world.save.missionSeconds + "s · HẠ " + world.save.enemiesDestroyed
                    + " · CORE " + world.save.coresCollected + " · MẤT MẠNG " + world.save.livesLost)).row();
            if (world.stage == CampaignWorld.Stage.RESULT && world.save.mission < 8 && !world.replay)
                panel.add(ui.button("NHIỆM VỤ TIẾP THEO", () -> { if (game.flushPending()) { world.nextMission(); overlay.clear(); } else refreshOverlay(); })).width(420).height(64).row();
            if (world.stage == CampaignWorld.Stage.GAME_OVER || world.replay) panel.add(ui.button("THỬ LẠI", () -> {
                if (!game.flushPending()) { refreshOverlay(); return; }
                CampaignStore.State restored = game.copy(world.replay ? initial : game.profile);
                restored.missionCleared = false;
                game.setScreen(new GamePlayScreen(game, restored, world.replay));
            })).width(360).height(64).row();
        }
        panel.add(ui.button("VỀ MENU", this::menu)).width(360).height(64);
        overlay.add(panel);
    }

    private Table shellPanel(float width, float height) {
        overlay.clear();
        Image shade = new Image(ui.panel); shade.setColor(.025f, .045f, .085f, .98f);
        shade.setFillParent(true); overlay.addActor(shade);
        Table panel = new Table(); panel.setBackground(ui.modal); panel.pad(28.8f);
        panel.defaults().growX().spaceBottom(12.8f);
        overlay.add(panel).size(width, height);
        return panel;
    }

    private void showPause() {
        Table panel = shellPanel(560, 520);
        Label heading = ui.title("TẠM DỪNG"); heading.setFontScale(.5f); heading.setAlignment(Align.center);
        panel.add(heading).height(60.8f).row();
        Label detail = ui.hudLabel(game.saveError != null ? "Chưa lưu được tiến độ. Hãy thử lưu lại trước khi tiếp tục."
                : String.format(java.util.Locale.ROOT, "NHIỆM VỤ %02d · ĐỢT %d\n%s · CẤP %d", world.save.mission, world.save.encounter + 1,
                weaponNames[world.save.activeWeapon], world.save.weaponLevels[world.save.activeWeapon]));
        detail.setWrap(true); detail.setAlignment(Align.center); detail.setColor(.68f, .77f, .84f, 1);
        panel.add(detail).height(73.6f).row();
        TextButton resume = ui.button("TIẾP TỤC", () -> setPaused(false));
        panel.add(resume).height(57.6f).row();
        panel.add(ui.secondary(game.saveError == null ? "CHƠI LẠI ĐỢT" : "THỬ LƯU LẠI", () -> {
            if (game.saveError != null) { game.flushPending(); refreshOverlay(); return; }
            confirm("Chơi lại từ đầu đợt đã lưu?\nĐiểm, thời gian và trang bị sẽ trở về đã lưu.", () -> {
                if (game.flushPending()) game.setScreen(new GamePlayScreen(game, game.copy(world.replay ? initial : game.profile), world.replay));
                else { confirmAction = null; refreshOverlay(); }
            });
        })).height(57.6f).row();
        panel.add(ui.secondary("CÀI ĐẶT", () -> { settingsOpen = true; refreshOverlay(); })).height(57.6f).row();
        panel.add(ui.secondary("MÀN HÌNH CHÍNH", () -> confirm("Rời nhiệm vụ?\nKhi tiếp tục, bạn sẽ trở về đầu đợt đã lưu. Phần đang chơi sẽ được bỏ qua.", this::menu))).height(57.6f);
        stage.setKeyboardFocus(resume);
    }

    private void confirm(String copy, Runnable action) {
        confirmCopy = copy; confirmAction = action; refreshOverlay();
    }

    private void showConfirmation() {
        Table panel = shellPanel(608, 280);
        Label copy = ui.label(confirmCopy); copy.setWrap(true); copy.setAlignment(Align.center);
        panel.add(copy).colspan(2).grow().row();
        TextButton agree = ui.button("ĐỒNG Ý", () -> confirmAction.run());
        panel.add(agree).width(260).height(57.6f);
        TextButton cancel = ui.secondary("HỦY", () -> { confirmAction = null; refreshOverlay(); });
        panel.add(cancel).width(260).height(57.6f); stage.setKeyboardFocus(cancel);
    }

    private void showSettings() {
        Table panel = shellPanel(720, 584);
        Label heading = ui.title("CÀI ĐẶT"); heading.setFontScale(.625f); heading.setAlignment(Align.center);
        panel.add(heading).height(60.8f).row();
        Slider music = ui.slider(game.profile.musicVolume), sfx = ui.slider(game.profile.sfxVolume);
        panel.add(ui.label("NHẠC")).left().row(); panel.add(music).height(32).row();
        panel.add(ui.label("HIỆU ỨNG ÂM THANH")).left().row(); panel.add(sfx).height(32).row();
        TextButton vibration = ui.toggle("RUNG THIẾT BỊ", game.profile.vibrationEnabled);
        TextButton shake = ui.toggle("RUNG MÀN HÌNH", !game.profile.reducedMotion);
        panel.add(vibration).height(57.6f).row(); panel.add(shake).height(57.6f).row();
        TextButton save = ui.button("LƯU VÀ QUAY LẠI", () -> {
            if (!game.flushPending()) return;
            CampaignStore.State candidate = game.copy(game.profile);
            candidate.musicVolume = music.getValue(); candidate.sfxVolume = sfx.getValue();
            candidate.vibrationEnabled = vibration.isChecked(); candidate.reducedMotion = !shake.isChecked();
            game.checkpoint(candidate);
            if (game.saveError != null) return;
            game.audio.musicVolume = candidate.musicVolume; game.audio.sfxVolume = candidate.sfxVolume;
            settingsOpen = false; refreshOverlay();
        });
        panel.add(save).height(57.6f).row();
        panel.add(ui.secondary("QUAY LẠI", () -> { settingsOpen = false; refreshOverlay(); })).height(48);
        stage.setKeyboardFocus(save);
    }

    @Override public void show() {
        InputAdapter playInput = new InputAdapter() {
            @Override public boolean touchDown(int x, int y, int id, int button) {
                if (paused || pointer != -1) return false;
                if (world.awaitingContinue()) {
                    if (!world.readyToContinue()) return true;
                    pointer = id; lastX = x; lastY = y; tapStarted = world.clock; return true;
                }
                if (!world.player.canInput() || !world.gameplayInput()) return false;
                pointer = id; lastX = x; lastY = y; world.held = true;
                return true;
            }
            @Override public boolean touchDragged(int x, int y, int id) {
                if (id != pointer) return false;
                if (world.awaitingContinue()) { tapStarted = -1; return true; }
                pendingDragX += (x - lastX) * world.width / Gdx.graphics.getWidth();
                pendingDragY += (lastY - y) * world.height / Gdx.graphics.getHeight();
                lastX = x; lastY = y;
                return true;
            }
            @Override public boolean touchUp(int x, int y, int id, int button) {
                if (id != pointer) return false;
                if (tapStarted >= 0 && world.clock - tapStarted <= .6f && x >= 0 && y >= 0
                        && x < Gdx.graphics.getWidth() && y < Gdx.graphics.getHeight()) world.acceptBriefing();
                clearPointer(); return true;
            }
            @Override public boolean touchCancelled(int x, int y, int id, int button) {
                if (id != pointer) return false;
                clearPointer(); return true;
            }
            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.BACK || key == Input.Keys.ESCAPE) {
                    if (settingsOpen) { settingsOpen = false; refreshOverlay(); }
                    else if (confirmAction != null) { confirmAction = null; refreshOverlay(); }
                    else setPaused(!paused);
                    return true;
                }
                if (key == Input.Keys.SPACE && !paused) {
                    if (world.awaitingContinue()) world.acceptBriefing();
                    else if (world.gameplayInput()) world.held = true;
                    return true;
                }
                if (key == Input.Keys.M) return world.missile();
                return false;
            }
            @Override public boolean keyUp(int key) {
                if (key == Input.Keys.SPACE) { world.held = false; return true; }
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, playInput));
        game.audio.music("bgm-combat");
    }

    @Override public void render(float delta) {
        float seconds = Math.min(delta, .1f);
        if (!paused) {
            accumulator += seconds;
            while (accumulator >= 1f / 60) {
                world.move(pendingDragX, pendingDragY); pendingDragX = pendingDragY = 0;
                world.update(1f / 60); accumulator -= 1f / 60;
            }
            if (!world.player.canInput()) clearPointer();
        }
        if (game.saveError != null && !paused && world.stage != CampaignWorld.Stage.RESULT && world.stage != CampaignWorld.Stage.GAME_OVER) setPaused(true);
        game.audio.tick(seconds);
        renderer.render(seconds);
        if (shownRevision != world.revision) {
            shownRevision = world.revision;
            status.setText("×" + world.save.lives);
            weaponButton.setText("LV" + world.save.weaponLevels[world.save.activeWeapon]);
            weaponIcon.setDrawable(ui.icon(new String[]{"UI/ui-weapon-pulse.png", "UI/ui-weapon-plasma.png", "UI/ui-weapon-homing.png"}[world.save.activeWeapon]));
            missileButton.setText(world.save.missiles + "/3");
            score.setText(Integer.toString(world.save.score));
            shard.setText(Integer.toString(world.save.shard));
            title.setText(world.announcement);
            boolean missionTitle = world.announcement.startsWith("NHIỆM VỤ");
            title.setFontScale(missionTitle ? .525f : .3125f);
            layoutHud();
        }
        int effectsState = (world.player.shield ? 1 : 0) + ((int)Math.ceil(world.player.overdriveRemaining) << 1) + ((int)Math.ceil(world.player.droneRemaining) << 10);
        if (shownEffects != effectsState) {
            shownEffects = effectsState;
            shieldIcon.setVisible(world.player.shield);
            overdriveIcon.setVisible(world.player.overdriveRemaining > 0);
            supportIcon.setVisible(world.player.droneRemaining > 0);
            overdriveTimer.setText(world.player.overdriveRemaining > 0 ? (int)Math.ceil(world.player.overdriveRemaining) + "s" : "");
            supportTimer.setText(world.player.droneRemaining > 0 ? (int)Math.ceil(world.player.droneRemaining) + "s" : "");
        }
        String cue = world.pickupCueRemaining > 0 && world.player.deathRemaining <= 0 && world.stage != CampaignWorld.Stage.RESULT && world.stage != CampaignWorld.Stage.GAME_OVER ? world.pickupAnnouncement : "";
        if (!effects.textEquals(cue)) effects.setText(cue);
        heatVent.setVisible(world.player.overheated);
        if (world.player.overheated) heatVent.setDrawable(ventFrames[(int)(world.clock / .06f) % ventFrames.length]);
        if (continueHint != null) {
            float delay = world.stage == CampaignWorld.Stage.BRIEFING ? world.mission.get("Intro").getFloat("ContinueDelaySeconds") : 0;
            continueHint.setColor(.64f, .78f, .83f, world.readyToContinue() ? Math.min(1, (world.stageClock - delay) / .4f) : 0);
        }
        hud.setVisible(world.gameplayInput() && !paused);
        weaponButton.setDisabled(paused || !world.gameplayInput() || !world.player.canInput());
        missileButton.setDisabled(paused || !world.gameplayInput() || !world.player.canInput() || world.save.missiles <= 0);
        int currentHeat = (int)(100 * world.player.heat / world.thermal().getFloat("capacity"));
        if (shownHeat != currentHeat || !heat.textEquals(world.player.overheated ? "XẢ NHIỆT" : currentHeat >= 80 ? "NÓNG" : "")) {
            shownHeat = currentHeat;
            for (Image notch : heatNotches) {
                if (currentHeat >= 80 || world.player.overheated) notch.setColor(1, .9f, .7f, .9f);
                else notch.setColor(.86f, .96f, 1, .8f);
            }
            heat.setText(world.player.overheated ? "XẢ NHIỆT" : currentHeat >= 80 ? "NÓNG" : "");
            heatFill.setScaleX(Math.max(0, Math.min(1, currentHeat / 100f)));
            if (world.player.overheated) heatFill.setColor(1, .36f, .22f, 1);
            else if (currentHeat >= 80) heatFill.setColor(1, .74f, .2f, 1);
            else heatFill.setColor(.26f, .92f, 1, 1);
            if (world.player.overheated) heatFrame.setColor(1, .62f, .45f, 1);
            else if (currentHeat >= 80) heatFrame.setColor(1, .86f, .55f, 1);
            else heatFrame.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
        if (shownStage != world.stage) {
            shownStage = world.stage;
            if (!world.gameplayInput()) clearPointer();
            refreshOverlay();
            game.audio.music(world.stage == CampaignWorld.Stage.RESULT || world.stage == CampaignWorld.Stage.GAME_OVER ? "bgm-hub" : world.encounter.getInt("Completion") == 3 ? "bgm-boss" : "bgm-combat");
        }
        String bossText = "";
        bossPanel.setVisible(false);
        for (CampaignWorld.Enemy enemy : world.enemies) if (enemy.boss != null && world.stage == CampaignWorld.Stage.RUNNING && !paused) {
            bossPanel.setVisible(true);
            int nodes = 0; float shieldHp = 0, shieldMax = 0;
            for (com.alexei.spaceshooter.manager.BossCombat.Node node : enemy.boss.nodes) {
                if (node.hp > 0) nodes++;
                shieldHp += Math.max(0, node.hp); shieldMax += node.maxHp;
            }
            boolean shielded = enemy.boss.blocksCoreDamage();
            float fraction = shielded ? shieldHp / Math.max(1, shieldMax) : enemy.hp / enemy.maxHp;
            bossFill.setScaleX(Math.max(0, fraction));
            if (shielded) bossFill.setColor(.2f, .8f, 1, 1); else bossFill.setColor(1, .35f, .2f, 1);
            bossPercentage.setText((int)Math.ceil(100 * fraction) + "%");
            bossText = enemy.boss.displayName() + (shielded ? nodes > 0 ? " · NÚT KHIÊN " + nodes : " · KHIÊN ĐANG VỠ"
                    : enemy.boss.phaseB ? enemy.boss.nodes.size > 0 ? " · LÕI LỘ DIỆN" : " · HỎA LỰC TĂNG CƯỜNG" : "");
            break;
        }
        if (!bossStatus.textEquals(bossText)) bossStatus.setText(bossText);
        stage.getViewport().apply(); stage.act(seconds); stage.draw();
    }

    @Override public void resize(int width, int height) {
        renderer.resize(width, height);
        stage.getViewport().update(width, height, true);
        layoutHud();
    }

    private void layoutHud() {
        float width = stage.getViewport().getWorldWidth(), height = stage.getViewport().getWorldHeight();
        float scale = width / Math.max(1, Gdx.graphics.getWidth());
        float left = Gdx.graphics.getSafeInsetLeft() * scale, right = Gdx.graphics.getSafeInsetRight() * scale;
        float bottom = Gdx.graphics.getSafeInsetBottom() * scale, top = Gdx.graphics.getSafeInsetTop() * scale;
        float usableWidth = width - left - right, usableHeight = height - bottom - top;
        heatGauge.setBounds(left + 11.2f, height - top - 68.8f, 240, 60.8f);
        score.setBounds(left + usableWidth * .42f - 64, height - top - 67.2f, 128, 51.2f);
        coinPanel.setBounds(left + usableWidth * .62f - 64, height - top - 67.2f, 128, 51.2f);
        pauseButton.setBounds(width - right - 70.4f, height - top - 70.4f, 57.6f, 57.6f);
        lifePanel.setBounds(left, bottom + 6.4f, 105.6f, 57.6f);
        indicatorPanel.setBounds(left + usableWidth * .5f - 88, bottom + 20.8f, 176, 76.8f);
        weaponButton.setBounds(width - right - 128, bottom + 8, 121.6f, 73.6f);
        missileButton.setBounds(width - right - 128, bottom + 94.4f, 121.6f, 67.2f);
        effects.setBounds(left + usableWidth * .5f - 208, bottom + 106.4f, 416, 27.2f);
        boolean missionTitle = world.announcement.startsWith("NHIỆM VỤ");
        title.setBounds(left + usableWidth * .5f - 440, bottom + usableHeight * (missionTitle ? .67f : .87f) - 20, 880, 40);
        bossPanel.setBounds(left + usableWidth * .27f, height - top - 84, usableWidth * .46f, 40);
    }
    @Override public void pause() { setPaused(true); }
    @Override public void hide() { clearPointer(); Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { renderer.dispose(); stage.dispose(); }
}
