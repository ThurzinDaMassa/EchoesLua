package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.enums.ItemType;

public class CollectibleItem {

    private final ItemType type;

    private final float baseX;

    private final float baseY;

    private final Rectangle bounds;

    private final Sprite sprite;

    private boolean collected;

    private float animationTime;

    private float currentY;

    public CollectibleItem(
        ItemType type,
        float x,
        float y,
        GameAssets assets
    ) {

        this.type =
            type;

        baseX = x;
        baseY = y;

        currentY = y;

        Texture texture =
            getTextureForType(
                type,
                assets
            );

        sprite =
            new Sprite(
                texture
            );

        float size =
            getSizeForType(
                type
            );

        sprite.setSize(
            size,
            size
        );

        sprite.setOriginCenter();

        sprite.setPosition(
            x,
            y
        );

        sprite.setColor(getColorForType(type));

        bounds =
            new Rectangle(
                x,
                y,
                size,
                size
            );
    }

    private Texture getTextureForType(
        ItemType type,
        GameAssets assets
    ) {

        switch (type) {

            case OXYGEN:
                return assets.getOxygen();

            case FOOD:
                return assets.getFood();

            case ICE_ROCK:
                return assets.getIceRock();
            case MEDKIT:
                return assets.getMedkit();

            case ANTENNA_PART:
                return assets.getAntennaPart();
            case ENERGY_PART:
                return assets.getEnergyPart();
            case EXTRACTION_PART:
                return assets.getExtractionPart();
            case GREENHOUSE_PART:
                return assets.getGreenhousePart();
            case WEAPON_PART_A:
                return assets.getWeaponPartA();
            case WEAPON_PART_B:
                return assets.getWeaponPartB();
            case WEAPON_PART_C:
                return assets.getWeaponPartC();

            default:
                throw new IllegalArgumentException(
                    "Tipo de item nao suportado: "
                        + type
                );
        }
    }

    private float getSizeForType(
        ItemType type
    ) {

        switch (type) {

            case OXYGEN:
                return 54f;

            case FOOD:
                return 58f;

            case ICE_ROCK:
                return 62f;
            case MEDKIT:
                return 58f;

            default:
                return 48f;
        }
    }

    private Color getColorForType(ItemType type) {
        return Color.WHITE;
    }

    public void update(
        float delta
    ) {

        if (collected) {
            return;
        }

        animationTime += delta;

        /*
         * Flutuação vertical.
         */
        float floating =
            MathUtils.sin(
                animationTime * 2.3f
            ) * 6f;

        currentY =
            baseY + floating;

        sprite.setY(
            currentY
        );

        /*
         * Pulsação.
         */
        float pulse =
            1f
                + MathUtils.sin(
                animationTime * 3f
            ) * 0.04f;

        sprite.setScale(
            pulse
        );

        /*
         * Movimento suave.
         */
        bounds.setPosition(
            baseX,
            currentY
        );
    }

    public void render(
        SpriteBatch batch
    ) {

        if (collected) {
            return;
        }

        sprite.draw(
            batch
        );
    }

    public boolean overlaps(
        Player player
    ) {

        if (collected) {
            return false;
        }

        return bounds.overlaps(
            player.getBounds()
        );
    }

    public void collect() {

        collected = true;
    }

    public ItemType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getCenterX() {

        return bounds.x
            + bounds.width / 2f;
    }

    public float getCenterY() {

        return bounds.y
            + bounds.height / 2f;
    }
}
