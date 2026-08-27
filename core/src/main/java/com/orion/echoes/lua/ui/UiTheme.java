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
        shapes.setColor(0f, 0f, 0f, 0.28f);
        shapes.rect(x + 5f, y - 5f, width, height);
        shapes.setColor(0.008f, 0.021f, 0.031f, 0.93f);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.025f, 0.062f, 0.082f, 0.50f);
        shapes.rect(x + 1f, y + 1f, width - 2f, height - 2f);
        shapes.setColor(BORDER.r, BORDER.g, BORDER.b, 0.84f);
        shapes.rect(x, y, width, 1f);
        shapes.rect(x, y + height - 1f, width, 1f);
        shapes.rect(x + width - 1f, y, 1f, height);
        shapes.setColor(accent.r, accent.g, accent.b, 0.94f);
        shapes.rect(x, y, 3f, height);
        shapes.rect(x, y + height - 3f, Math.min(54f, width * 0.24f), 3f);
        shapes.setColor(accent.r, accent.g, accent.b, 0.15f);
        shapes.rect(x + 3f, y + 1f, Math.min(88f, width * 0.36f), height - 2f);
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
        float clamped = MathUtils.clamp(value, 0f, 1f);
        shapes.setColor(0.002f, 0.009f, 0.014f, 0.96f);
        shapes.rect(x, y, width, height);
        shapes.setColor(color.r, color.g, color.b, 0.18f);
        shapes.rect(x - 2f, y - 2f, width * clamped + 4f, height + 4f);
        shapes.setColor(color.r, color.g, color.b, 0.96f);
        shapes.rect(x, y, width * clamped, height);
        shapes.setColor(1f, 1f, 1f, 0.12f);
        shapes.rect(x, y + height - 1f, width, 1f);
        shapes.setColor(0.002f, 0.009f, 0.014f, 0.72f);
        for (int i = 1; i < 5; i++) {
            shapes.rect(x + width * i / 5f, y, 1f, height);
        }
    }
}
