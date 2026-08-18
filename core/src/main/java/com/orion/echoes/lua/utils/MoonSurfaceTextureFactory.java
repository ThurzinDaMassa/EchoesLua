package com.orion.echoes.lua.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public final class MoonSurfaceTextureFactory {

    private MoonSurfaceTextureFactory() {
    }

    public static Texture createMoonSurfaceTexture() {

        final int size = 256;

        Pixmap pixmap =
            new Pixmap(
                size,
                size,
                Pixmap.Format.RGBA8888
            );

        // Base mais suave
        pixmap.setColor(
            new Color(
                0.43f,
                0.44f,
                0.48f,
                1f
            )
        );
        pixmap.fill();

        // Poeira fina
        for (int i = 0; i < 800; i++) {

            float shade =
                MathUtils.random(
                    0.38f,
                    0.50f
                );

            pixmap.setColor(
                shade,
                shade,
                shade + 0.02f,
                1f
            );

            int x =
                MathUtils.random(0, size - 1);
            int y =
                MathUtils.random(0, size - 1);
            int r =
                MathUtils.random(1, 2);

            pixmap.fillCircle(
                x,
                y,
                r
            );
        }

        // Pedrinhas
        for (int i = 0; i < 140; i++) {

            pixmap.setColor(
                new Color(
                    0.30f,
                    0.31f,
                    0.34f,
                    1f
                )
            );

            int x =
                MathUtils.random(0, size - 1);
            int y =
                MathUtils.random(0, size - 1);

            pixmap.fillCircle(
                x,
                y,
                MathUtils.random(1, 3)
            );
        }

        // Crateras menores e menos frequentes
        for (int i = 0; i < 9; i++) {

            int x =
                MathUtils.random(22, size - 22);
            int y =
                MathUtils.random(22, size - 22);
            int r =
                MathUtils.random(8, 18);

            pixmap.setColor(
                new Color(
                    0.53f,
                    0.54f,
                    0.58f,
                    1f
                )
            );
            pixmap.fillCircle(
                x,
                y,
                r
            );

            pixmap.setColor(
                new Color(
                    0.27f,
                    0.28f,
                    0.31f,
                    1f
                )
            );
            pixmap.fillCircle(
                x,
                y,
                r - 3
            );
        }

        Texture texture =
            new Texture(pixmap);

        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        pixmap.dispose();

        return texture;
    }
}
