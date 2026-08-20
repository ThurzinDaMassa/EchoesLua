package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public final class UiFonts implements AutoCloseable {
    private static final String REGULAR = "fonts/SpaceMono-Regular.ttf";
    private static final String BOLD = "fonts/SpaceMono-Bold.ttf";
    private static final String GLYPHS = FreeTypeFontGenerator.DEFAULT_CHARS
        + "áàâãéêíóôõúçÁÀÂÃÉÊÍÓÔÕÚÇ";

    public final BitmapFont micro;
    public final BitmapFont label;
    public final BitmapFont body;
    public final BitmapFont heading;
    public final BitmapFont display;

    public UiFonts() {
        micro = generate(REGULAR, 12);
        label = generate(BOLD, 14);
        body = generate(REGULAR, 18);
        heading = generate(BOLD, 26);
        display = generate(BOLD, 48);
    }

    private BitmapFont generate(String path, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.characters = GLYPHS;
        parameter.kerning = true;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    @Override
    public void close() {
        micro.dispose();
        label.dispose();
        body.dispose();
        heading.dispose();
        display.dispose();
    }
}
