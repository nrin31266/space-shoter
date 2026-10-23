package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public final class CampaignAssets implements Disposable {
    public final CampaignCatalog catalog;
    public final BitmapFont font, titleFont, hudFont, buttonFont, digitFont;
    public final TextureRegion whiteRegion;
    private final Texture whiteTexture;
    private final AssetManager manager;
    private final ObjectMap<String, Sprite> sprites = new ObjectMap<>();
    private final ObjectMap<String, String> audioPaths = new ObjectMap<>();

    public static final class Sprite {
        public final TextureRegion region;
        public final float width, height, pivotX, pivotY;
        Sprite(TextureRegion region, float ppu, float px, float py) {
            this.region = region;
            width = region.getRegionWidth() / ppu;
            height = region.getRegionHeight() / ppu;
            pivotX = px * width;
            pivotY = py * height;
        }
    }

    public CampaignAssets(AssetManager manager) {
        this.manager = manager;
        com.badlogic.gdx.graphics.Pixmap white = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        white.setColor(com.badlogic.gdx.graphics.Color.WHITE); white.fill();
        whiteTexture = new Texture(white); white.dispose(); whiteRegion = new TextureRegion(whiteTexture);
        catalog = new CampaignCatalog(Gdx.files.internal("campaign/catalog.json"));
        manager.load("campaign/art.atlas", TextureAtlas.class);
        for (JsonValue file : catalog.root.get("files")) {
            String path = file.asString();
            if (!path.endsWith(".wav")) continue;
            String name = path.substring(path.lastIndexOf('/') + 1, path.length() - 4);
            audioPaths.put(name, "campaign/" + path);
            if (path.contains("/Music/")) manager.load("campaign/" + path, Music.class);
            else manager.load("campaign/" + path, Sound.class);
        }
        manager.finishLoading();
        TextureAtlas atlas = manager.get("campaign/art.atlas", TextureAtlas.class);
        for (JsonValue entry : catalog.root.get("sprites")) {
            String path = entry.getString("path").replaceFirst("^art/", "").replaceFirst("\\.png$", "");
            TextureRegion page = atlas.findRegion(path);
            if (page == null) throw new IllegalArgumentException("Sprite not packed: " + path);
            JsonValue rect = entry.get("rect"), pivot = entry.get("pivot");
            int w = rect.getInt("width"), h = rect.getInt("height");
            TextureRegion region = new TextureRegion(page, rect.getInt("x"), page.getRegionHeight() - rect.getInt("y") - h, w, h);
            sprites.put(entry.name, new Sprite(region, entry.getFloat("ppu"), pivot.getFloat("x"), pivot.getFloat("y")));
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("campaign/fonts/ChakraPetch-Medium.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯưẠạẢảẤấẦầẨẩẪẫẬậẮắẰằẲẳẴẵẶặẸẹẺẻẼẽẾếỀềỂểỄễỆệỈỉỊịỌọỎỏỐốỒồỔổỖỗỘộỚớỜờỞởỠỡỢợỤụỦủỨứỪừỬửỮữỰựỲỳỴỵỶỷỸỹ·→–";
        p.minFilter = p.magFilter = Texture.TextureFilter.Linear;
        p.size = 24;
        font = generator.generateFont(p);
        generator.dispose();

        FreeTypeFontGenerator semiBoldGen = new FreeTypeFontGenerator(Gdx.files.internal("campaign/fonts/ChakraPetch-SemiBold.ttf"));
        p.size = 28;
        buttonFont = semiBoldGen.generateFont(p);
        p.size = 22;
        hudFont = semiBoldGen.generateFont(p);
        semiBoldGen.dispose();

        FreeTypeFontGenerator titleGenerator = new FreeTypeFontGenerator(Gdx.files.internal("campaign/fonts/VT323-Regular.ttf"));
        p.size = 64;
        titleFont = titleGenerator.generateFont(p);
        p.size = 42;
        digitFont = titleGenerator.generateFont(p);
        titleGenerator.dispose();
    }

    public Sprite sprite(JsonValue reference) {
        String key = CampaignCatalog.spriteKey(reference);
        Sprite sprite = sprites.get(key);
        if (sprite == null) throw new IllegalArgumentException("Missing sprite: " + key);
        return sprite;
    }

    public Sprite sprite(String key) {
        Sprite sprite = sprites.get(key);
        if (sprite == null) throw new IllegalArgumentException("Missing sprite: " + key);
        return sprite;
    }

    public void draw(SpriteBatch batch, String key, float x, float y, float scale, float rotation) {
        Sprite s = sprite(key);
        batch.draw(s.region, x - s.pivotX * scale, y - s.pivotY * scale,
                s.pivotX * scale, s.pivotY * scale, s.width * scale, s.height * scale, 1, 1, rotation);
    }

    public Sound sound(String event) {
        String path = audioPaths.get(event);
        return path == null ? null : manager.get(path, Sound.class);
    }

    public Music music(String event) {
        String path = audioPaths.get(event);
        return path == null ? null : manager.get(path, Music.class);
    }

    @Override public void dispose() {
        whiteTexture.dispose();
        font.dispose();
        buttonFont.dispose();
        hudFont.dispose();
        titleFont.dispose();
        digitFont.dispose();
    }
}
