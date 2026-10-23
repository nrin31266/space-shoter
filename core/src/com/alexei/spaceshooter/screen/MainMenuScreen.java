package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.MainGame;
import com.alexei.spaceshooter.manager.CampaignStore;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public final class MainMenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Stage stage = new Stage(new ExtendViewport(1280, 720));
    private final CampaignUi ui;
    private final Group root = new Group();
    private final Image backdrop;
    private final Array<Actor> anchored = new Array<>();
    private final Array<float[]> anchors = new Array<>();
    private Runnable back = this::home;
    private int selectedMission;

    public MainMenuScreen(MainGame game) {
        this.game = game;
        game.audio.clearGameplay();
        ui = new CampaignUi(game.assets, game.audio::cue);
        backdrop = new Image(ui.icon("Environments/background.png"));
        backdrop.setScaling(Scaling.fill);
        backdrop.setTouchable(Touchable.disabled);
        stage.addActor(backdrop);
        Actor stars = new Actor() {
            private float clock;
            private final com.badlogic.gdx.graphics.g2d.TextureRegion star = ui.icon("VFX/speed-streak.png").getRegion();
            private static final int STAR_COUNT = 112;
            @Override public void act(float delta) { clock += delta; }
            @Override public void draw(Batch batch, float parentAlpha) {
                float w = stage.getWidth(), h = stage.getHeight();
                for (int i = 0; i < STAR_COUNT; i++) {
                    float depth = 0.35f + (i % 3) * 0.32f;
                    float speed = 0.005f * depth;
                    float x = ((i * 0.618034f) % 1f);
                    float y = (i * 0.3713f - clock * speed) % 1f;
                    if (y < 0) y += 1f;
                    float size = (1.6f + (i % 4) * .65f) * depth;
                    float twinkle = 0.75f + 0.25f * MathUtils.sin(clock * (2f + (i % 5)) + i);
                    float alpha = parentAlpha * (0.22f + 0.45f * depth) * twinkle;
                    if (i % 5 == 0) batch.setColor(0.55f, 0.85f, 1f, alpha);
                    else if (i % 7 == 0) batch.setColor(1f, 0.88f, 0.7f, alpha * 0.9f);
                    else batch.setColor(0.85f, 0.95f, 1f, alpha);
                    batch.draw(star, x * w - size * 0.5f, y * h - size * 0.5f, size, size);
                }
                batch.setColor(Color.WHITE);
            }
        };
        stars.setTouchable(Touchable.disabled); stage.addActor(stars); stage.addActor(root);
        home();
    }

    // Các anchor và size lấy từ canvas 1600×900; giữ khoảng an toàn riêng với nền phủ màn hình.
    private <T extends Actor> T place(T actor, float x, float y, float width, float height) {
        return place(actor, x, y, width, height, .5f);
    }
    private <T extends Actor> T place(T actor, float x, float y, float width, float height, float pivotX) {
        root.addActor(actor); anchored.add(actor); anchors.add(new float[]{x, y, width * .8f, height * .8f, pivotX});
        layout(); return actor;
    }
    private void layout() {
        for (int i = 0; i < anchored.size; i++) {
            float[] p = anchors.get(i);
            anchored.get(i).setBounds(root.getWidth() * p[0] - p[2] * p[4], root.getHeight() * p[1] - p[3] / 2, p[2], p[3]);
        }
    }
    private void clear() {
        root.clearChildren(); anchored.clear(); anchors.clear(); stage.setKeyboardFocus(null); back = this::home;
    }
    private Label copy(String text, boolean center) {
        Label label = ui.label(text); label.setWrap(true); label.setAlignment(center ? Align.center : Align.left); return label;
    }
    private void header(String text) {
        place(ui.title(text), .06f, .88f, 950, 80, 0);
        place(ui.secondary("QUAY LẠI", () -> back.run()), .94f, .88f, 250, 64, 1);
    }
    private void focus(Actor actor) { stage.setKeyboardFocus(actor); }

    private JsonValue cosmetic(String id) {
        for (JsonValue option : game.assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options"))
            if (option.getString("Id").equals(id)) return option;
        throw new IllegalArgumentException("Unknown cosmetic: " + id);
    }
    private boolean owned(String id) {
        for (String item : game.profile.ownedCosmetics) if (item.equals(id)) return true;
        return false;
    }
    private void shipPreview(String[] selection, String status, boolean equipped) {
        JsonValue ship = cosmetic(selection[0]);
        place(new ShipPreview(game.assets, ship, selection[1], cosmetic(selection[2])), .28f, .6267f, 256, 320);
        Label name = ui.title(ship.getString("DisplayName")); name.setFontScale(.52f); name.setAlignment(Align.center); name.setColor(Color.WHITE);
        place(name, .28f, .31f, 650, 52);
        Label label = copy(equipped ? "ĐANG TRANG BỊ" : "XEM TRƯỚC", true); label.setFontScale(.8f); label.setColor(Color.CYAN);
        place(label, .28f, .265f, 650, 32);
        Label detail = copy(status, true); detail.setFontScale(.8f); place(detail, .28f, .2f, 660, 70);
        Table wallet = new Table(); wallet.add(new Image(ui.icon("UI/ui-shard.png"))).size(34).padRight(10); wallet.add(ui.label(String.format("%,d", game.profile.shard)));
        place(wallet, .28f, .12f, 650, 48);
    }

    private void home() {
        clear();
        place(ui.title("SPACE SHOOTER"), .08f, .91f, 730, 100, 0);
        String status = !game.profile.hasCampaign ? "Chọn CHƠI MỚI để bắt đầu hành trình." :
                "NHIỆM VỤ " + String.format("%02d", game.profile.mission) + " · " + (game.profile.mission <= 4 ? "KHU VỰC A" : "KHU VỰC B");
        shipPreview(game.profile.equippedCosmetics, status, true);
        Table navigation = new Table(); navigation.top().pad(12.8f); navigation.defaults().spaceBottom(17.6f).growX();
        TextButton resume = ui.button("TIẾP TỤC", () -> start(game.copy(game.profile), false));
        resume.setDisabled(game.saveError != null || !game.profile.hasCampaign);
        navigation.add(resume).height(67.2f).row();
        TextButton newGame = ui.button("CHƠI MỚI", () -> { if (game.profile.hasCampaign) confirmNewGame(); else newGame(); });
        newGame.setDisabled(game.saveError != null); navigation.add(newGame).height(57.6f).row();
        Table destinations = new Table(); destinations.defaults().growX().height(60.8f);
        destinations.add(ui.secondary("XƯỞNG", () -> hangar(false, 0))).padRight(12.8f);
        destinations.add(ui.secondary("CỬA HÀNG", () -> hangar(true, 0)));
        navigation.add(destinations).row();
        navigation.add(ui.secondary("NHIỆM VỤ", this::missions)).height(57.6f).row();
        place(navigation, .75f, .54f, 560, 420);
        Table footer = new Table(); footer.defaults().growX().height(54.4f);
        TextButton[] controls = {ui.secondary("CÁCH CHƠI", this::help), ui.secondary("CÀI ĐẶT", this::settings), ui.secondary("GIỚI THIỆU", this::credits), ui.secondary("DEV", this::devMenu)};
        for (int i = 0; i < controls.length; i++) { controls[i].getLabel().setFontScale(.68f); footer.add(controls[i]).padRight(i == 3 ? 0 : 8f); }
        place(footer, .75f, .17f, 560, 68);
        if (game.saveError != null) place(copy("Không đọc/ghi được save. Dữ liệu được giữ nguyên.\n" + game.saveError, true), .5f, .055f, 1300, 60);
        if (game.pendingCheckpoint != null) place(ui.secondary("THỬ LƯU LẠI", () -> { game.flushPending(); home(); }), .28f, .055f, 480, 60);
        focus(resume.isDisabled() ? newGame : resume);
    }

    private void confirm(String message, Runnable accept, Runnable cancel) {
        clear(); back = cancel;
        Table card = new Table(); card.setBackground(ui.modal); card.pad(28); card.defaults().pad(12);
        card.add(copy(message, true)).colspan(2).grow().row();
        TextButton no = ui.secondary("HỦY", cancel);
        card.add(no).width(250).height(58);
        card.add(ui.button("ĐỒNG Ý", accept)).width(250).height(58);
        place(card, .5f, .5f, 900, 420); focus(no);
    }
    private void confirmNewGame() {
        confirm("Bắt đầu hành trình mới?\nTiến độ chiến dịch hiện tại sẽ được thay thế. Coin, kỷ lục, ngoại hình và cài đặt được giữ lại.", this::newGame, this::home);
    }
    private void newGame() {
        CampaignStore.State state = game.copy(game.profile);
        state.mission = 1; state.encounter = 0; state.lives = 3; state.missiles = 1;
        state.activeWeapon = 0; state.weaponLevels = new int[]{1, 0, 0};
        state.score = 0; state.missionCleared = state.completed = state.regionReady = false;
        state.hasCampaign = true; CampaignStore.resetMissionStats(state);
        game.setScreen(new GamePlayScreen(game, state, false, true));
    }
    private void start(CampaignStore.State state, boolean replay) {
        game.setScreen(new GamePlayScreen(game, state, replay, replay));
    }

    private static String time(float seconds) {
        int total = (int)Math.max(0, seconds); return total >= 3600 ? String.format("%d:%02d:%02d", total / 3600, total / 60 % 60, total % 60) : String.format("%02d:%02d", total / 60, total % 60);
    }
    private void missions() { selectedMission = Math.max(1, game.profile.mission); missions(selectedMission); }
    private void missions(int selected) {
        clear(); selectedMission = selected; header("NHIỆM VỤ");
        place(ui.label("KHU VỰC A"), .06f, .77f, 400, 50, 0);
        place(ui.label("KHU VỰC B"), .06f, .43f, 400, 50, 0);
        TextButton first = null;
        for (int i = 1; i <= 8; i++) {
            final int number = i;
            boolean unlocked = i <= game.profile.unlockedMission;
            int best = game.profile.bestScores[i - 1];
            String status = !unlocked ? "ĐÃ KHÓA" : game.profile.hasCampaign && !game.profile.missionCleared && game.profile.mission == i ? "ĐANG TIẾN HÀNH" : best > 0 ? "ĐÃ HOÀN THÀNH" : "ĐÃ MỞ";
            TextButton card = ui.card(String.format("NHIỆM VỤ %02d", i) + "\n" + status + (best > 0 ? "\n" + String.format("%,d", best) + " · " + time(game.profile.bestTimes[i - 1]) : ""), () -> missions(number));
            card.getLabel().setFontScale(.85f); card.setDisabled(!unlocked); card.setChecked(i == selected);
            place(card, .17f + ((i - 1) % 4) * .22f, i <= 4 ? .64f : .3f, 300, 180);
            if (i == 1) first = card;
        }
        int best = game.profile.bestScores[selected - 1];
        Label detail = copy(String.format("NHIỆM VỤ %02d", selected) + " · " + (best <= 0 ? "Chưa có kỷ lục hoàn thành" : "KỶ LỤC " + String.format("%,d", best) + " điểm · " + time(game.profile.bestTimes[selected - 1])), false);
        detail.setFontScale(.8f); place(detail, .06f, .1f, 820, 65, 0);
        TextButton launch = ui.button("CHƠI LẠI NHIỆM VỤ", () -> replay(selected));
        launch.setDisabled(selected > game.profile.unlockedMission || game.saveError != null);
        place(launch, .94f, .1f, 510, 72, 1); focus(first);
    }
    private void replay(int number) {
        CampaignStore.State state = game.copy(game.profile);
        state.mission = number; state.encounter = 0; state.score = state.missionScore = state.encounterShardHighWater = state.shard = 0;
        state.missionCleared = state.completed = state.regionReady = false; CampaignStore.resetMissionStats(state);
        JsonValue preset = game.assets.catalog.missions[number - 1].get("ReplayPreset");
        state.lives = preset.getInt("Lives"); state.missiles = preset.getInt("Missiles");
        state.activeWeapon = preset.getInt("ActiveWeapon"); state.weaponLevels = preset.get("WeaponLevels").asIntArray(); start(state, true);
    }

    private void hangar(boolean store, int slot) {
        String selected = game.profile.equippedCosmetics[slot];
        if (store) for (JsonValue option : game.assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options"))
            if (option.getInt("Slot") == slot && !owned(option.getString("Id"))) { selected = option.getString("Id"); break; }
        hangar(store, slot, selected);
    }
    private void hangar(boolean store, int slot, String selected) {
        clear();
        place(ui.secondary("QUAY LẠI", this::home), .06f, .88f, 250, 64, 0);
        TextButton hangar = ui.tab("XƯỞNG", () -> hangar(false, slot)), shop = ui.tab("CỬA HÀNG", () -> hangar(true, slot));
        hangar.setDisabled(!store); hangar.setChecked(!store); shop.setDisabled(store); shop.setChecked(store);
        place(hangar, .64f, .87f, 230, 64); place(shop, .81f, .87f, 270, 64);
        String[] selection = game.profile.equippedCosmetics.clone(); selection[slot] = selected;
        String[] tabs = {"TÀU", "SƠN TÀU", "VỆT DI CHUYỂN"};
        shipPreview(selection, slot == 0 ? "Chọn tàu, rồi tùy chỉnh sơn và vệt di chuyển." : tabs[slot] + " · " + cosmetic(selection[0]).getString("DisplayName"), game.profile.equippedCosmetics[slot].equals(selected));
        for (int i = 0; i < 3; i++) {
            final int tabIndex = i;
            TextButton tab = ui.tab(tabs[i], () -> hangar(store, tabIndex)); tab.getLabel().setFontScale(.8f); tab.setChecked(i == slot); tab.setDisabled(i == slot);
            place(tab, .565f + i * .165f, .78f, 240, 54);
        }
        Label context = copy(slot == 0 ? store ? "KHÁM PHÁ TÀU" : "TÀU CỦA BẠN" : "NGOẠI HÌNH CỦA " + cosmetic(selection[0]).getString("DisplayName"), true); context.setFontScale(.8f);
        place(context, .73f, .71f, 720, 48);
        Array<JsonValue> options = new Array<>();
        for (JsonValue option : game.assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options"))
            if (option.getInt("Slot") == slot && (store || owned(option.getString("Id")))) options.add(option);
        TextButton first = null;
        for (int i = 0; i < options.size; i++) {
            JsonValue option = options.get(i); String id = option.getString("Id");
            String status = game.profile.equippedCosmetics[slot].equals(id) ? "ĐANG TRANG BỊ" : owned(id) ? "ĐÃ SỞ HỮU" : option.getInt("ShardCost") + " Coin";
            TextButton card = ui.card(option.getString("DisplayName") + "\n" + status, () -> hangar(store, slot, id));
            card.clearChildren(); card.pad(8); String[] swatch = game.profile.equippedCosmetics.clone(); swatch[slot] = id;
            card.add(new ShipPreview(game.assets, cosmetic(swatch[0]), swatch[1], cosmetic(swatch[2]))).size(64, 80).padBottom(10).row();
            card.getLabel().setFontScale(.72f); card.add(card.getLabel()).growX(); card.setChecked(selected.equals(id));
            place(card, .73f + (i - (options.size - 1) * .5f) * (options.size > 2 ? .165f : .24f), .44f, options.size > 2 ? 230 : 320, 220);
            if (first == null) first = card;
        }
        JsonValue option = cosmetic(selected); boolean available = owned(selected), equipped = game.profile.equippedCosmetics[slot].equals(selected); int cost = option.getInt("ShardCost");
        Label detail = copy(option.getString("DisplayName") + "\n" + (store ? "Mua và trang bị bằng Coin." : "Đã sở hữu · thay đổi miễn phí.") + " Chỉ thay đổi ngoại hình.", true); detail.setFontScale(.8f);
        place(detail, .73f, .24f, 720, 100);
        String action = store ? available ? "ĐÃ SỞ HỮU" : game.profile.shard >= cost ? "MUA & TRANG BỊ · " + cost + " Coin" : "CẦN THÊM " + (cost - game.profile.shard) + " Coin" : equipped ? "ĐANG TRANG BỊ" : "TRANG BỊ";
        Runnable commit = () -> { game.equipCosmetic(selected); hangar(store, slot, selected); };
        TextButton equip = ui.button(action, () -> {
            if (available) commit.run(); else confirm("Mua " + option.getString("DisplayName") + " với " + cost + " Coin?\nChỉ thay đổi ngoại hình; không thay đổi chỉ số chiến đấu.", commit, () -> hangar(store, slot, selected));
        });
        equip.setDisabled(game.saveError != null || (store ? available || game.profile.shard < cost : equipped));
        place(equip, .73f, .11f, 720, 76); focus(first);
    }

    private void settings() {
        clear(); header("CÀI ĐẶT");
        place(ui.label("NHẠC"), .12f, .68f, 540, 70, 0);
        place(ui.label("HIỆU ỨNG ÂM THANH"), .12f, .51f, 540, 70, 0);
        Slider music = place(ui.slider(game.profile.musicVolume), .73f, .68f, 520, 72);
        Slider sfx = place(ui.slider(game.profile.sfxVolume), .73f, .51f, 520, 72);
        TextButton vibration = place(ui.toggle("RUNG THIẾT BỊ", game.profile.vibrationEnabled), .5f, .35f, 1200, 72);
        TextButton shake = place(ui.toggle("RUNG MÀN HÌNH", !game.profile.reducedMotion), .5f, .23f, 1200, 72);
        Label error = copy("", true); error.setFontScale(.75f); place(error, .5f, .035f, 1400, 48);
        TextButton save = ui.button("LƯU VÀ QUAY LẠI", () -> {
            if (game.pendingCheckpoint != null && !game.flushPending()) { error.setText("Chưa lưu được tiến độ. Dữ liệu trước đó vẫn được giữ. Hãy thử lại."); return; }
            if (game.saveError != null) { error.setText("Không thể thay cài đặt khi bản lưu chưa đọc được. Dữ liệu được giữ nguyên."); return; }
            CampaignStore.State candidate = game.copy(game.profile);
            candidate.musicVolume = music.getValue(); candidate.sfxVolume = sfx.getValue();
            candidate.vibrationEnabled = vibration.isChecked(); candidate.reducedMotion = !shake.isChecked();
            game.checkpoint(candidate);
            if (game.saveError != null) {
                // Cài đặt nháp không được trở thành checkpoint chờ ghi sau khi người chơi hủy.
                game.pendingCheckpoint = null; game.saveError = null;
                error.setText("Chưa lưu được cài đặt. Giá trị trước đó vẫn được giữ. Hãy thử lại."); return;
            }
            game.audio.musicVolume = candidate.musicVolume; game.audio.sfxVolume = candidate.sfxVolume; home();
        });
        place(save, .5f, .1f, 590, 72); focus(save);
    }

    private void help() {
        clear(); header("CÁCH CHƠI");
        String[] titles = {"DI CHUYỂN VÀ BẮN", "VŨ KHÍ VÀ NHIỆT", "SỐNG SÓT VÀ TIẾP TẾ", "TẠM DỪNG VÀ TIẾP TỤC"};
        String[] body = {"Kéo ở vùng trống để di chuyển tương đối. Giữ ngón tay để bắn; nhấc tay để ngừng bắn.", "Chạm ô vũ khí để đổi nhánh đã mở. Các nhánh dùng chung nhiệt. Khi quá nóng, chờ hạ nhiệt để bắn lại.", "Mất mạng làm giảm cấp vũ khí đang dùng. Sau hồi sinh có bảo vệ ngắn. Nhặt Core và hỗ trợ; dùng nút Tên lửa khi cần.", "Chạm Ⅱ để tạm dừng. Bạn có thể đổi cài đặt hoặc chơi lại đợt đã lưu. Tiếp tục chiến dịch từ đầu đợt đã lưu."};
        for (int i = 0; i < 4; i++) {
            Table block = new Table(); block.setBackground(ui.panel); block.pad(20); block.defaults().growX();
            Label title = copy(titles[i], false); title.setColor(Color.CYAN); block.add(title).padBottom(15).row();
            Label text = copy(body[i], false); text.setFontScale(.86f); block.add(text).growY();
            place(block, i % 2 == 0 ? .28f : .72f, i < 2 ? .62f : .28f, 660, 280);
        }
    }
    private void credits() {
        clear(); header("GIỚI THIỆU");
        Label text = copy("SPACE SHOOTER\n\nPhát triển: Nguyễn Văn Rin\nNền source: Fando / space-shooter · Java/LibGDX\nArtwork: tác giả dự án với AI, agent/MCP và Aseprite\n\nAUDIO\nMusic: Andrew Sitkov · license thương mại\nSFX: Kenney (CC0), Little Robot Sound Factory (CC BY 3.0)\nFont: Chakra Petch / VT323 (OFL)\nChi tiết và bằng chứng: assets/campaign/licenses", true);
        place(text, .5f, .45f, 1360, 600);
    }

    private void devMenu() {
        clear(); header("DEV FAST MODE");
        DevPanel panel = new DevPanel(game, ui, this::home);
        place(panel, .5f, .47f, 1050, 570);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override public boolean keyDown(int key) {
                if (key == Input.Keys.F1 || key == Input.Keys.F2 || key == Input.Keys.D) {
                    devMenu();
                    return true;
                }
                if (key == Input.Keys.BACK || key == Input.Keys.ESCAPE) { back.run(); return true; }
                return false;
            }
        })); game.audio.music("bgm-hub");
    }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(.025f, .045f, .085f, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.audio.tick(Math.min(delta, .1f)); stage.getViewport().apply(); stage.act(Math.min(delta, .1f)); stage.draw();
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        float scale = stage.getViewport().getWorldWidth() / width;
        float left = Gdx.graphics.getSafeInsetLeft() * scale, bottom = Gdx.graphics.getSafeInsetBottom() * scale;
        root.setBounds(left, bottom, stage.getWidth() - left - Gdx.graphics.getSafeInsetRight() * scale, stage.getHeight() - bottom - Gdx.graphics.getSafeInsetTop() * scale);
        backdrop.setBounds(0, 0, stage.getWidth(), stage.getHeight()); layout();
    }
    @Override public void dispose() { stage.dispose(); }
}
