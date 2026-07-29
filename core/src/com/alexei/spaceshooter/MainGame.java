package com.alexei.spaceshooter;

import com.alexei.spaceshooter.screen.LoadingScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.kotcrab.vis.ui.VisUI;

public class MainGame extends Game {
    private AssetManager assetManager;

    @Override
    public void create() {
        VisUI.load();
        // ensure linear filtering for all UI textures to prevent pixelation
        for (Texture texture : VisUI.getSkin().getAtlas().getTextures()) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        assetManager = new AssetManager();
        setScreen(new LoadingScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        VisUI.dispose();
        if (assetManager != null) {
            assetManager.dispose();
        }
    }
}
