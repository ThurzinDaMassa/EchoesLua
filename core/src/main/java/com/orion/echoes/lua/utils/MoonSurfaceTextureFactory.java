package com.orion.echoes.lua.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;

public final class MoonSurfaceTextureFactory {
    public static final int TEXTURE_SIZE = 1024;
    private static final long TERRAIN_SEED = 0x4C554E41524C4F4EL;

    private MoonSurfaceTextureFactory() {
    }

    public static Texture createMoonSurfaceTexture() {
        RandomXS128 random = new RandomXS128(TERRAIN_SEED);
        Pixmap pixmap = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);

        drawRegolithBase(pixmap, random);
        drawLargeMineralPatches(pixmap, random);
        drawCraterField(pixmap, random);
        drawFineRegolith(pixmap, random);
        drawMicroRocks(pixmap, random);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static void drawRegolithBase(Pixmap pixmap, RandomXS128 random) {
        pixmap.setColor(0.315f, 0.325f, 0.35f, 1f);
        pixmap.fill();

        // Ruido fino por pixel, sem os aneis repetitivos da textura anterior.
        for (int y = 0; y < TEXTURE_SIZE; y += 2) {
            for (int x = 0; x < TEXTURE_SIZE; x += 2) {
                float shade = 0.285f + random.nextFloat() * 0.075f;
                float blue = shade + 0.018f + random.nextFloat() * 0.012f;
                pixmap.setColor(shade, shade + 0.006f, blue, 1f);
                pixmap.fillRectangle(x, y, 2, 2);
            }
        }
    }

    private static void drawLargeMineralPatches(Pixmap pixmap, RandomXS128 random) {
        for (int i = 0; i < 38; i++) {
            int x = random.nextInt(TEXTURE_SIZE);
            int y = random.nextInt(TEXTURE_SIZE);
            int radius = 35 + random.nextInt(100);
            boolean light = random.nextBoolean();
            float shade = light ? 0.38f : 0.24f;
            pixmap.setColor(shade, shade + 0.008f, shade + 0.026f, 0.055f);
            pixmap.fillCircle(x, y, radius);
        }
    }

    private static void drawCraterField(Pixmap pixmap, RandomXS128 random) {
        for (int i = 0; i < 24; i++) {
            int radiusX = 12 + random.nextInt(38);
            int radiusY = Math.max(9, Math.round(radiusX * (0.62f + random.nextFloat() * 0.28f)));
            int x = 60 + random.nextInt(TEXTURE_SIZE - 120);
            int y = 60 + random.nextInt(TEXTURE_SIZE - 120);
            drawCrater(pixmap, random, x, y, radiusX, radiusY);
        }

        for (int i = 0; i < 52; i++) {
            int radiusX = 4 + random.nextInt(9);
            int radiusY = Math.max(3, Math.round(radiusX * 0.72f));
            int x = 20 + random.nextInt(TEXTURE_SIZE - 40);
            int y = 20 + random.nextInt(TEXTURE_SIZE - 40);
            drawCrater(pixmap, random, x, y, radiusX, radiusY);
        }
    }

    private static void drawCrater(
        Pixmap pixmap,
        RandomXS128 random,
        int x,
        int y,
        int radiusX,
        int radiusY
    ) {
        int lightOffset = Math.max(1, radiusX / 10);
        fillEllipse(pixmap, x - lightOffset, y + lightOffset, radiusX + 3, radiusY + 3,
            new Color(0.47f, 0.48f, 0.51f, 0.72f));
        fillEllipse(pixmap, x + lightOffset, y - lightOffset, radiusX + 1, radiusY + 1,
            new Color(0.17f, 0.18f, 0.205f, 0.82f));
        fillEllipse(pixmap, x, y, Math.max(2, radiusX - 4), Math.max(2, radiusY - 3),
            new Color(0.235f, 0.242f, 0.265f, 1f));
        fillEllipse(pixmap, x - lightOffset, y + lightOffset,
            Math.max(1, radiusX - 8), Math.max(1, radiusY - 6),
            new Color(0.275f, 0.282f, 0.305f, 0.72f));

        int ejecta = Math.max(4, radiusX / 3);
        for (int i = 0; i < ejecta; i++) {
            float angle = random.nextFloat() * MathUtils.PI2;
            float distance = radiusX * (1.1f + random.nextFloat() * 0.75f);
            int px = x + Math.round(MathUtils.cos(angle) * distance);
            int py = y + Math.round(MathUtils.sin(angle) * distance * radiusY / radiusX);
            pixmap.setColor(0.42f, 0.43f, 0.46f, 0.38f);
            pixmap.fillCircle(px, py, 1 + random.nextInt(2));
        }
    }

    private static void fillEllipse(
        Pixmap pixmap,
        int centerX,
        int centerY,
        int radiusX,
        int radiusY,
        Color color
    ) {
        pixmap.setColor(color);
        for (int y = -radiusY; y <= radiusY; y++) {
            float normalized = (float) y / radiusY;
            int halfWidth = Math.round(radiusX * (float) Math.sqrt(1f - normalized * normalized));
            pixmap.drawLine(centerX - halfWidth, centerY + y,
                centerX + halfWidth, centerY + y);
        }
    }

    private static void drawFineRegolith(Pixmap pixmap, RandomXS128 random) {
        for (int i = 0; i < 7200; i++) {
            float shade = 0.22f + random.nextFloat() * 0.22f;
            pixmap.setColor(shade, shade + 0.005f, shade + 0.018f,
                0.35f + random.nextFloat() * 0.35f);
            pixmap.drawPixel(random.nextInt(TEXTURE_SIZE), random.nextInt(TEXTURE_SIZE));
        }
    }

    private static void drawMicroRocks(Pixmap pixmap, RandomXS128 random) {
        for (int i = 0; i < 520; i++) {
            int x = random.nextInt(TEXTURE_SIZE);
            int y = random.nextInt(TEXTURE_SIZE);
            int radius = 1 + random.nextInt(3);
            pixmap.setColor(0.15f, 0.16f, 0.18f, 0.68f);
            pixmap.fillCircle(x + 1, y - 1, radius);
            pixmap.setColor(0.47f, 0.48f, 0.51f, 0.62f);
            pixmap.fillCircle(x, y, Math.max(1, radius - 1));
        }
    }
}
