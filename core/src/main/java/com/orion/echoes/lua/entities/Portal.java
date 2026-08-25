package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import com.orion.echoes.lua.assets.GameAssets;

public class Portal {

    private final float x;

    private final float y;

    private final float width;

    private final float height;

    private final Sprite sprite;

    private final Rectangle bounds;

    private boolean active;

    private float animationTime;

    public Portal(
        float x,
        float y,
        float width,
        float height,
        GameAssets assets
    ) {

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        sprite =
            new Sprite(
                assets.getPortal()
            );

        sprite.setSize(
            width,
            height
        );

        sprite.setPosition(
            x,
            y
        );

        sprite.setOriginCenter();

        bounds =
            new Rectangle(
                x + width * 0.24f,
                y + height * 0.12f,
                width * 0.52f,
                height * 0.70f
            );

        active = false;
    }

    public void update(
        float delta
    ) {

        animationTime += delta;

        if (active) {

            float pulse =
                1f
                    + MathUtils.sin(
                    animationTime * 3f
                )
                    * 0.025f;

            sprite.setScale(
                pulse
            );

        } else {

            sprite.setScale(
                1f
            );
        }
    }

    public void render(
        SpriteBatch batch
    ) {

        if (active) {

            sprite.setColor(
                Color.WHITE
            );

        } else {

            sprite.setColor(
                0.25f,
                0.28f,
                0.32f,
                0.55f
            );
        }

        sprite.draw(
            batch
        );

        sprite.setColor(
            Color.WHITE
        );
    }

    public boolean overlaps(
        Player player
    ) {

        return active
            &&
            bounds.overlaps(
                player.getBounds()
            );
    }

    public boolean isPlayerNear(Player player) {
        Rectangle interaction = new Rectangle(bounds);
        interaction.x -= 70f;
        interaction.y -= 70f;
        interaction.width += 140f;
        interaction.height += 140f;
        return interaction.overlaps(player.getBounds());
    }

    public void activate() {

        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public float getCenterX() {

        return x
            + width / 2f;
    }

    public float getCenterY() {

        return y
            + height / 2f;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
