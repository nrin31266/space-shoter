package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;

public class CustomUI {
    private static Skin skin;
    private static BitmapFont fontAwesome;
    private static BitmapFont robotoFont;

    public static void init() {
        if (skin != null) return;
        skin = new Skin();

        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE); pix.fill();
        skin.add("white", new Texture(pix));
        pix.dispose();

        robotoFont = FontUtil.generateRoboto(60);
        robotoFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        fontAwesome = FontUtil.generateFontAwesome(60);
        fontAwesome.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin.add("default", new Label.LabelStyle(robotoFont, Color.WHITE));
        skin.add("icon", new Label.LabelStyle(fontAwesome, Color.WHITE));

        // Base Button Style
        Button.ButtonStyle btnStyle = new Button.ButtonStyle();
        btnStyle.up   = skin.newDrawable("white", new Color(0.06f, 0.10f, 0.20f, 0.95f));
        btnStyle.down = skin.newDrawable("white", new Color(0.00f, 0.55f, 0.75f, 0.95f));
        btnStyle.over = skin.newDrawable("white", new Color(0.10f, 0.20f, 0.35f, 0.95f));
        skin.add("default", btnStyle);
        
        // Danger Button Style
        Button.ButtonStyle dangerStyle = new Button.ButtonStyle();
        dangerStyle.up   = skin.newDrawable("white", new Color(0.20f, 0.05f, 0.05f, 0.95f));
        dangerStyle.down = skin.newDrawable("white", new Color(0.75f, 0.10f, 0.10f, 0.95f));
        dangerStyle.over = skin.newDrawable("white", new Color(0.40f, 0.08f, 0.08f, 0.95f));
        skin.add("danger", dangerStyle);

        // Window style
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = robotoFont;
        windowStyle.titleFontColor = new Color(0.5f, 0.7f, 1f, 1f);
        windowStyle.background = skin.newDrawable("white", new Color(0.04f, 0.06f, 0.12f, 0.97f));
        skin.add("default", windowStyle);
    }

    public static Skin getSkin() {
        if (skin == null) init();
        return skin;
    }

    public static Button createButton(String iconUnicode, String text, boolean isDanger) {
        Button btn = new Button(getSkin(), isDanger ? "danger" : "default");
        
        if (iconUnicode != null && !iconUnicode.isEmpty()) {
            Label iconLbl = new Label(iconUnicode, getSkin(), "icon");
            iconLbl.setColor(isDanger ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0f, 0.9f, 1f, 1f));
            if (text != null && !text.isEmpty()) {
                btn.add(iconLbl).padRight(15);
            } else {
                btn.add(iconLbl);
            }
        }
        
        if (text != null && !text.isEmpty()) {
            Label textLbl = new Label(text, getSkin(), "default");
            textLbl.setColor(isDanger ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0f, 0.9f, 1f, 1f));
            btn.add(textLbl);
        }
        
        btn.pad(20);
        return btn;
    }

    public static Label createTitle(String text, Color color) {
        Label l = new Label(text, getSkin(), "default");
        l.setColor(color);
        l.setFontScale(1.5f);
        l.setAlignment(Align.center);
        return l;
    }
}
