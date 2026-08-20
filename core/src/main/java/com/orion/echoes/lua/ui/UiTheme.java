package com.orion.echoes.lua.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class UiTheme {
    public static final Color SPACE = new Color(0.006f, 0.011f, 0.020f, 1f);
    public static final Color PANEL = new Color(0.018f, 0.035f, 0.050f, 0.94f);
    public static final Color PANEL_SOLID = new Color(0.018f, 0.035f, 0.050f, 1f);
    public static final Color PANEL_LIGHT = new Color(0.028f, 0.060f, 0.082f, 1f);
    public static final Color CYAN = new Color(0.05f, 0.84f, 1f, 1f);
    public static final Color CYAN_SOFT = new Color(0.34f, 0.70f, 0.80f, 1f);
    public static final Color GREEN = new Color(0.20f, 0.92f, 0.62f, 1f);
    public static final Color PURPLE = new Color(0.56f, 0.35f, 1f, 1f);
    public static final Color WARNING = new Color(1f, 0.67f, 0.18f, 1f);
    public static final Color DANGER = new Color(1f, 0.25f, 0.31f, 1f);
    public static final Color TEXT = new Color(0.92f, 0.96f, 1f, 1f);
    public static final Color MUTED = new Color(0.47f, 0.59f, 0.66f, 1f);
    public static final Color BORDER = new Color(0.10f, 0.25f, 0.32f, 1f);

    private UiTheme() {
    }

    public static void panel(
        ShapeRenderer shapes,
        float x,
        float y,
        float width,
        float height,
        Color accent
    ) {
        shapes.setColor(0f, 0f, 0f, 0.36f);
        shapes.rect(x + 7f, y - 7f, width, height);
        shapes.setColor(PANEL);
        shapes.rect(x, y, width, height);
        shapes.setColor(BORDER);
        shapes.rect(x, y, width, 1f);
        shapes.rect(x, y + height - 1f, width, 1f);
        shapes.setColor(accent);
        shapes.rect(x, y, 4f, height);
        shapes.rect(x, y + height - 3f, 48f, 3f);
    }

    public static void bar(
        ShapeRenderer shapes,
        float x,
        float y,
        float width,
        float height,
        float value,
        Color color
    ) {
        shapes.setColor(0.005f, 0.014f, 0.021f, 0.9f);
        shapes.rect(x, y, width, height);
        shapes.setColor(color);
        shapes.rect(x, y, width * MathUtils.clamp(value, 0f, 1f), height);
        shapes.setColor(1f, 1f, 1f, 0.12f);
        shapes.rect(x, y + height - 1f, width, 1f);
    }
}
