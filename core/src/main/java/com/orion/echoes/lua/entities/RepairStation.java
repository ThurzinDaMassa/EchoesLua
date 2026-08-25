package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.assets.GameAssets;

public class RepairStation {
    private final RepairType type;
    private final Rectangle bounds;
    private final Sprite sprite;
    private float animationTime;

    public RepairStation(RepairType type, float x, float y, GameAssets assets) {
        this.type = type;
        this.bounds = new Rectangle(x, y, 112f, 96f);
        sprite = new Sprite(assets.getRepairStation());
        sprite.setSize(132f, 142f);
        sprite.setPosition(x - 10f, y - 16f);
    }

    public void update(float delta) {
        animationTime += delta;
    }

    public boolean isPlayerNear(Player player) {
        Rectangle interaction = new Rectangle(bounds);
        interaction.x -= 42f;
        interaction.y -= 42f;
        interaction.width += 84f;
        interaction.height += 84f;
        return interaction.overlaps(player.getBounds());
    }

    public void render(SpriteBatch batch, boolean repaired) {
        Color accent = repaired ? new Color(0.70f, 1f, 0.84f, 1f) : getTypeTint();
        sprite.setColor(accent);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
    }

    public void renderStatus(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.18f, 0.92f, 0.58f, 1f)
            : getTypeTint();
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 1f : 0.72f);
        shapes.circle(bounds.x + bounds.width - 14f, bounds.y + bounds.height - 6f,
            nearby ? 9f : 6f, 18);
    }

    private Color getTypeTint() {
        return switch (type) {
            case COMMUNICATION -> new Color(0.72f, 0.96f, 1f, 1f);
            case ENERGY -> new Color(1f, 0.88f, 0.62f, 1f);
            case EXTRACTION -> new Color(0.68f, 0.84f, 1f, 1f);
            case GREENHOUSE -> new Color(0.70f, 1f, 0.78f, 1f);
        };
    }

    public RepairType getType() {
        return type;
    }

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }
}
