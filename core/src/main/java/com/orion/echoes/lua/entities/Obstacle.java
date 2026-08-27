package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import com.orion.echoes.lua.assets.GameAssets;

public class Obstacle {

    private final float x;

    private final float y;

    private final float width;

    private final float height;

    private final Sprite sprite;

    private final Rectangle bounds;

    public Obstacle(
        float x,
        float y,
        float width,
        float height,
        GameAssets assets
    ) {

        this(x, y, width, height, assets.getObstacle());
    }

    public Obstacle(
        float x,
        float y,
        float width,
        float height,
        Texture texture
    ) {

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        sprite =
            new Sprite(
                texture
            );

        sprite.setPosition(
            x,
            y
        );

        sprite.setSize(
            width,
            height
        );

        /*
         * Bounds um pouco menores
         * que a imagem.
         */
        bounds =
            new Rectangle(
                x + width * 0.15f,
                y + height * 0.10f,
                width * 0.70f,
                height * 0.72f
            );
    }

    public void render(
        SpriteBatch batch
    ) {

        sprite.draw(
            batch
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
