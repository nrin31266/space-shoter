package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.manager.CampaignAssets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonValue;

final class CampaignUi {
    final CampaignAssets assets;
    final TextButton.TextButtonStyle buttonStyle;
    private final TextButton.TextButtonStyle secondaryStyle, cardStyle, tabStyle;
    final NinePatchDrawable panel, modal;

    CampaignUi(CampaignAssets assets) {
        this.assets = assets;
        buttonStyle = style("UI/ui-button.png");
        secondaryStyle = style("UI/ui-button-secondary.png");
        cardStyle = style("UI/ui-card.png");
        tabStyle = style("UI/ui-tab.png");
        panel = patch("UI/ui-panel.png"); modal = patch("UI/ui-modal.png");
    }

    private TextButton.TextButtonStyle style(String path) {
        NinePatchDrawable base = patch(path);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = base;
        style.down = base.tint(new Color(.55f, .8f, .9f, 1));
        style.over = base.tint(new Color(.82f, 1, 1, 1));
        style.focused = base.tint(new Color(.7f, 1, 1, 1));
        style.disabled = base.tint(new Color(.45f, .52f, .6f, .55f));
        style.font = assets.font; style.fontColor = Color.WHITE;
        return style;
    }

    String key(String path) {
        for (JsonValue sprite : assets.catalog.root.get("sprites"))
            if (sprite.getString("path").equals("art/" + path)) return sprite.name;
        throw new IllegalArgumentException("Missing UI asset: " + path);
    }

    TextureRegionDrawable icon(String path) { return new TextureRegionDrawable(assets.sprite(key(path)).region); }

    private NinePatchDrawable patch(String path) {
        NinePatch patch = new NinePatch(assets.sprite(key(path)).region, 6, 6, 6, 6);
        patch.scale(.8f, .8f);
        return new NinePatchDrawable(patch);
    }

    Drawable solid(float r, float g, float b, float a) {
        return new TextureRegionDrawable(assets.whiteRegion).tint(new Color(r, g, b, a));
    }

    Label label(String text) { return new Label(text, new Label.LabelStyle(assets.font, Color.WHITE)); }
    Label hudLabel(String text) { return new Label(text, new Label.LabelStyle(assets.hudFont, Color.WHITE)); }
    Label title(String text) { return new Label(text, new Label.LabelStyle(assets.titleFont, Color.valueOf("9DEEFF"))); }

    TextButton button(String text, Runnable action) { return button(text, action, buttonStyle, false); }
    TextButton secondary(String text, Runnable action) { return button(text, action, secondaryStyle, false); }
    TextButton card(String text, Runnable action) { return button(text, action, cardStyle, false); }
    TextButton tab(String text, Runnable action) { return button(text, action, tabStyle, false); }

    TextButton toggle(String text, boolean checked) {
        TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle(secondaryStyle);
        toggleStyle.up = toggleStyle.down = toggleStyle.over = toggleStyle.checked = solid(.055f, .095f, .15f, .98f);
        TextButton toggle = button(text, null, toggleStyle, true);
        toggle.getLabel().setAlignment(com.badlogic.gdx.utils.Align.left);
        toggle.padRight(95); toggle.setChecked(checked);
        return toggle;
    }

    Slider slider(float value) {
        Slider.SliderStyle style = new Slider.SliderStyle();
        Drawable track = solid(.68f, .77f, .84f, 1); track.setMinHeight(12.8f);
        Drawable handle = solid(.36f, .9f, 1, 1); handle.setMinWidth(30.4f); handle.setMinHeight(44.8f);
        style.background = track; style.knob = handle;
        Slider slider = new Slider(0, 1, .01f, false, style); slider.setValue(value);
        return slider;
    }

    private TextButton button(String text, Runnable action, TextButton.TextButtonStyle style, boolean toggle) {
        TextButton button = new TextButton(text, style) {
            { setTransform(true); }
            @Override public void act(float delta) {
                super.act(delta);
                setOrigin(getWidth() / 2, getHeight() / 2);
                boolean focused = getStage() != null && getStage().getKeyboardFocus() == this;
                float target = isDisabled() ? 1 : isPressed() ? .98f : isOver() || focused ? 1.012f : 1;
                setScale(getScaleX() + (target - getScaleX()) * (1 - (float)Math.exp(-delta * 22)));
            }
            @Override public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (!toggle) return;
                float x = getX() + getWidth() - 76, y = getY() + (getHeight() - 36.8f) / 2;
                batch.setColor(.68f, .77f, .84f, parentAlpha);
                batch.draw(assets.whiteRegion, x, y, 57.6f, 36.8f);
                if (isChecked()) {
                    batch.setColor(.36f, .9f, 1, parentAlpha);
                    batch.draw(assets.whiteRegion, x + 6.4f, y + 6.4f, 44.8f, 24);
                }
                batch.setColor(Color.WHITE);
            }
        };
        button.pad(10, 24, 10, 24);
        button.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (button.isDisabled()) return;
                if (button.getStage() != null) button.getStage().setKeyboardFocus(button);
                if (action != null) action.run();
            }
        });
        button.addListener(new InputListener() {
            @Override public boolean keyDown(InputEvent event, int key) {
                if (button.isDisabled() || (key != com.badlogic.gdx.Input.Keys.ENTER && key != com.badlogic.gdx.Input.Keys.SPACE)) return false;
                if (toggle) button.setChecked(!button.isChecked());
                if (action != null) action.run();
                return true;
            }
        });
        return button;
    }
}
