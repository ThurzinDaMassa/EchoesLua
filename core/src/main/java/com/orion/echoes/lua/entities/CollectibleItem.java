package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

        this(type, x, y, assets, false);
    }

    public CollectibleItem(
        ItemType type,
        float x,
        float y,
        GameAssets assets,
        boolean marsVariant
    ) {

        this.type =
            type;

        baseX = x;
        baseY = y;

        currentY = y;

        Texture texture =
            getTextureForType(
                type,
                assets,
                marsVariant
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
        GameAssets assets,
        boolean marsVariant
    ) {

        switch (type) {

            case OXYGEN:
                return marsVariant ? assets.getMarsOxygen() : assets.getOxygen();

            case FOOD:
                return marsVariant ? assets.getMarsFood() : assets.getFood();

            case ICE_ROCK:
                return marsVariant ? assets.getMarsIceSample() : assets.getIceRock();
            case MEDKIT:
                return marsVariant ? assets.getMarsMedkit() : assets.getMedkit();

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
            case AMMO_CELL:
                return assets.getEnergyProjectile();
            case ALLOY_PLATE:
                return assets.getAlloyPlate();
            case QUANTUM_CORE:
                return assets.getQuantumCore();
            case FIBER_MESH:
                return assets.getFiberMesh();
            case MINING_TOOL:
                return assets.getMiningTool();
            case REPAIR_TOOL:
                return assets.getRepairTool();
            case ARMOR_HELMET:
                return assets.getArmorHelmet();
            case ARMOR_CHEST:
                return assets.getArmorChest();
            case ARMOR_BOOTS:
                return assets.getArmorBoots();
            case METHANE_SAMPLE:
                return assets.getMethaneSample();
            case TITAN_CORE:
                return assets.getTitanPowerCore();

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
            case AMMO_CELL:
                return 50f;
            case ALLOY_PLATE, QUANTUM_CORE, FIBER_MESH:
                return 56f;
            case MINING_TOOL, REPAIR_TOOL:
                return 64f;
            case ARMOR_HELMET, ARMOR_CHEST, ARMOR_BOOTS:
                return 62f;
            case METHANE_SAMPLE:
                return 60f;
            case TITAN_CORE:
                return 88f;

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

    public void renderGlow(ShapeRenderer shapes, boolean missionTarget) {
        if (collected) return;
        Color glow = getGlowColor();
        float pulse = 1f + MathUtils.sin(animationTime * 3.4f) * 0.10f;
        float radius = (bounds.width * 0.62f + (missionTarget ? 13f : 7f)) * pulse;
        shapes.setColor(glow.r, glow.g, glow.b, missionTarget ? 0.24f : 0.13f);
        shapes.circle(getCenterX(), getCenterY(), radius, 28);
        shapes.setColor(glow.r, glow.g, glow.b, missionTarget ? 0.34f : 0.20f);
        shapes.circle(getCenterX(), baseY + 4f, bounds.width * 0.44f, 24);
    }

    private Color getGlowColor() {
        return switch (type) {
            case OXYGEN -> new Color(0.12f, 0.90f, 1f, 1f);
            case FOOD -> new Color(1f, 0.78f, 0.22f, 1f);
            case ICE_ROCK -> new Color(0.54f, 0.88f, 1f, 1f);
            case MEDKIT -> new Color(0.30f, 1f, 0.54f, 1f);
            case AMMO_CELL -> new Color(1f, 0.45f, 0.16f, 1f);
            case ALLOY_PLATE -> new Color(0.66f, 0.88f, 1f, 1f);
            case QUANTUM_CORE -> new Color(0.30f, 1f, 0.92f, 1f);
            case FIBER_MESH -> new Color(1f, 0.64f, 0.22f, 1f);
            case ARMOR_HELMET, ARMOR_CHEST, ARMOR_BOOTS -> new Color(0.24f, 0.92f, 1f, 1f);
            case MINING_TOOL, REPAIR_TOOL -> new Color(1f, 0.76f, 0.25f, 1f);
            case METHANE_SAMPLE -> new Color(1f, 0.62f, 0.18f, 1f);
            case TITAN_CORE -> new Color(1f, 0.72f, 0.16f, 1f);
            default -> new Color(0.22f, 0.96f, 1f, 1f);
        };
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
