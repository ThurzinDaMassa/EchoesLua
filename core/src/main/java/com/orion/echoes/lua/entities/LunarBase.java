package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.utils.GameConstants;

public class LunarBase {

    private final float x;

    private final float y;

    private final float width;

    private final float height;

    private final Rectangle bounds;

    private final Rectangle entranceBounds;

    private final Sprite sprite;

    public LunarBase(
        float x,
        float y,
        GameAssets assets
    ) {

        this.x = x;
        this.y = y;

        width =
            GameConstants.BASE_WIDTH;

        height =
            GameConstants.BASE_HEIGHT;

        sprite =
            new Sprite(
                assets.getLunarBase()
            );

        sprite.setSize(
            width,
            height
        );

        sprite.setPosition(
            x,
            y
        );

        /*
         * Área interna da base.
         *
         * Menor que a imagem inteira,
         * porque antenas etc. não devem
         * ativar a recarga.
         */
        bounds =
            new Rectangle(
                x + width * 0.12f,
                y + height * 0.08f,
                width * 0.76f,
                height * 0.72f
            );

        entranceBounds = new Rectangle(
            x + width * 0.35f,
            y - 34f,
            width * 0.30f,
            height * 0.40f
        );
    }

    public void update(
        float delta,
        Player player,
        PlayerStatus status
    ) {

        if (
            isPlayerInside(
                player
            )
        ) {

            status.addOxygen(
                GameConstants
                    .BASE_OXYGEN_RECHARGE_RATE
                    * delta
            );
        }
    }

    public void render(
        SpriteBatch batch,
        boolean playerInside
    ) {

        /*
         * Feedback visual sutil.
         */

        if (playerInside) {

            sprite.setColor(
                0.82f,
                1f,
                0.92f,
                1f
            );

        } else {

            sprite.setColor(
                Color.WHITE
            );
        }

        sprite.draw(
            batch
        );

        sprite.setColor(
            Color.WHITE
        );
    }

    public boolean isPlayerInside(
        Player player
    ) {

        return bounds.overlaps(
            player.getBounds()
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isPlayerNearEntrance(Player player) {
        return entranceBounds.overlaps(player.getBounds());
    }

    public float getEntranceX() {
        return x + width / 2f;
    }

    public float getEntranceY() {
        return y;
    }

    public float getCenterX() {

        return x
            + width / 2f;
    }

    public float getCenterY() {

        return y
            + height / 2f;
    }
}
