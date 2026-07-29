package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontUtil {
    
    public static BitmapFont generateRoboto(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        // Include standard ASCII characters
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        font.setUseIntegerPositions(false);
        return font;
    }

    public static BitmapFont generateFontAwesome(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/fa-solid-900.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        
        // Add specific FontAwesome unicodes we need
        // \uf004 = heart, \uf005 = star, \uf04c = pause, \uf04b = play, 
        // \uf013 = gear/settings, \uf01e = redo/continue, \uf00d = times/close
        // \uf028 = volume up
        parameter.characters = "\uf004\uf005\uf04c\uf04b\uf013\uf01e\uf00d\uf028";
        
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        font.setUseIntegerPositions(false);
        return font;
    }
}
