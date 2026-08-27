package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.assets.GameAssets;

public class RepairStation {
    private final RepairType type;
    private final Rectangle bounds;
    private final Sprite brokenSprite;
    private final Sprite repairedSprite;
    private float animationTime;
    private float repairProgress;
    private boolean repairing;

    public static final float REPAIR_DURATION = 3f;

    public RepairStation(RepairType type, float x, float y, GameAssets assets) {
        this.type = type;
        this.bounds = new Rectangle(x, y, 112f, 96f);
        brokenSprite = createSprite(assets.getRepairStationBroken(), x, y);
        repairedSprite = createSprite(assets.getRepairStation(), x, y);
    }

    private Sprite createSprite(com.badlogic.gdx.graphics.Texture texture, float x, float y) {
        Sprite result = new Sprite(texture);
        result.setSize(132f, 142f);
        result.setPosition(x - 10f, y - 16f);
        result.setOriginCenter();
        return result;
    }

    public void update(float delta) {
        animationTime += delta;
        if (repairing) repairProgress = Math.min(REPAIR_DURATION, repairProgress + delta);
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
        Sprite active = repaired ? repairedSprite : brokenSprite;
        active.setColor(Color.WHITE);
        active.setRotation(repairing ? MathUtils.sin(animationTime * 18f) * 0.8f : 0f);
        active.draw(batch);
    }

    public void renderStatus(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.18f, 0.92f, 0.58f, 1f)
            : new Color(0.42f, 0.86f, 0.96f, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 1f : 0.72f);
        shapes.circle(bounds.x + bounds.width - 14f, bounds.y + bounds.height - 6f,
            nearby ? 9f : 6f, 18);
        if (repairing) renderRepairAnimation(shapes);
    }

    private void renderRepairAnimation(ShapeRenderer shapes) {
        float cx = getCenterX();
        float y = bounds.y + bounds.height + 31f;
        float swing = -28f + MathUtils.sin(animationTime * 11f) * 24f;
        shapes.setColor(0.72f, 0.46f, 0.18f, 1f);
        shapes.rect(cx - 3f, y - 4f, 3f, 4f, 6f, 32f, 1f, 1f, swing);
        shapes.setColor(0.82f, 0.88f, 0.92f, 1f);
        shapes.rect(cx - 14f, y + 22f, 28f, 10f);
        shapes.setColor(0.01f, 0.02f, 0.03f, 0.92f);
        shapes.rect(cx - 46f, y - 17f, 92f, 7f);
        shapes.setColor(0.16f, 0.90f, 1f, 1f);
        shapes.rect(cx - 46f, y - 17f, 92f * getRepairProgress(), 7f);
    }

    public void startRepair() {
        repairing = true;
        repairProgress = 0f;
    }

    public void cancelRepair() {
        repairing = false;
        repairProgress = 0f;
    }

    public boolean isRepairing() { return repairing; }
    public boolean isRepairComplete() { return repairing && repairProgress >= REPAIR_DURATION; }
    public float getRepairProgress() { return MathUtils.clamp(repairProgress / REPAIR_DURATION, 0f, 1f); }

    public RepairType getType() {
        return type;
    }

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
