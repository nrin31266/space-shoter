package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.manager.DevRunConfig;
import com.alexei.spaceshooter.manager.CampaignStore;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Chung một bộ chọn cho menu và pause để đường thử không lệch nhau. */
final class DevPanel extends Table {
    private final MainGame game;
    private final CampaignUi ui;
    private final DevRunConfig config;
    private final Label message;
    private SelectBox<String> encounter;

    DevPanel(MainGame game, CampaignUi ui, Runnable back) {
        this.game = game; this.ui = ui; config = game.devConfig;
        setBackground(ui.modal); pad(18); defaults().pad(5);
        Label heading = ui.title("DEV · CHẠY THỬ"); heading.setFontScale(.55f);
        add(heading).colspan(4).height(36).row();
        add(ui.label("NHIỆM VỤ")).left();
        SelectBox<String> mission = ui.select(new String[]{"M1", "M2", "M3", "M4 · BOSS", "M5", "M6", "M7", "M8 · BOSS"}, config.mission - 1);
        add(mission).width(230).height(46);
        add(ui.label("ĐỢT")).left();
        encounter = ui.select(encounters(), config.encounter);
        add(encounter).width(280).height(46).row();
        onChange(mission, () -> { config.mission = mission.getSelectedIndex() + 1; config.encounter = 0; encounter.setItems(encounters()); encounter.setSelectedIndex(0); });
        onChange(encounter, () -> config.encounter = encounter.getSelectedIndex());
        add(ui.label("ĐIỂM BẮT ĐẦU")).left();
        SelectBox<String> route = ui.select(DevRunConfig.ROUTES, config.route);
        add(route).colspan(3).growX().height(46).row(); onChange(route, () -> {
            config.route = route.getSelectedIndex();
            if (config.route >= 5) mission.setSelectedIndex(3);
        });
        add(ui.label("VŨ KHÍ")).left();
        SelectBox<String> weapon = ui.select(new String[]{"Pulse", "Plasma", "Homing"}, config.weapon);
        add(weapon).width(230).height(46); onChange(weapon, () -> config.weapon = weapon.getSelectedIndex());
        add(ui.label("CẤP SÚNG")).left();
        SelectBox<String> level = ui.select(new String[]{"LV1", "LV2", "LV3", "LV4", "LV5"}, config.level - 1);
        add(level).width(280).height(46).row(); onChange(level, () -> config.level = level.getSelectedIndex() + 1);
        TextButton god = ui.toggle("BẤT TỬ", config.god), intro = ui.toggle("BỎ INTRO", config.skipIntro);
        add(god).colspan(2).growX().height(46); add(intro).colspan(2).growX().height(46).row();
        onChange(god, () -> config.god = god.isChecked()); onChange(intro, () -> config.skipIntro = intro.isChecked());
        TextButton fake = ui.toggle("ĐẠN GIẢ · HIT/VFX, KHÔNG DAMAGE", config.fakeShots);
        fake.getLabel().setFontScale(.8f); add(fake).colspan(4).growX().height(46).row(); onChange(fake, () -> config.fakeShots = fake.isChecked());
        message = ui.hudLabel("Dev chạy riêng · không ghi tiến độ / tiền / trang bị");
        message.setFontScale(.65f); message.setWrap(true); add(message).colspan(4).growX().height(40).row();
        add(ui.secondary("QUAY LẠI", back)).colspan(2).growX().height(48);
        add(ui.button("CHẠY THỬ", this::launch)).colspan(2).growX().height(48);
    }

    private String[] encounters() {
        int count = game.assets.catalog.missions[config.mission - 1].get("Encounters").size;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            com.badlogic.gdx.utils.JsonValue e = game.assets.catalog.resolve(game.assets.catalog.missions[config.mission - 1].get("Encounters").get(i));
            names[i] = "Đợt " + (i + 1) + (e.getInt("Completion") == 3 ? " · BOSS" : "");
        }
        return names;
    }

    private static void onChange(Actor actor, Runnable action) {
        actor.addListener(new ChangeListener() { public void changed(ChangeEvent event, Actor target) { action.run(); } });
    }

    private void launch() {
        CampaignStore.State state = game.copy(game.profile);
        try { config.prepare(state, game.assets.catalog); }
        catch (IllegalArgumentException error) { ui.error(); message.setText(error.getMessage()); message.setColor(1, .5f, .3f, 1); return; }
        GamePlayScreen screen = new GamePlayScreen(game, state, true, !config.skipIntro && config.route == 0);
        if (config.god) screen.world.devGodModeToggle();
        screen.world.devFakeShots = config.fakeShots;
        screen.world.devStartRoute(config.route);
        game.setScreen(screen);
    }
}
