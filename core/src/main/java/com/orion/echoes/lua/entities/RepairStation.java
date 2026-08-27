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
    private final Sprite hammerSprite;
    private float animationTime;
    private float repairProgress;
    private boolean repairing;

    public static final float REPAIR_DURATION = 3f;

    public RepairStation(RepairType type, float x, float y, GameAssets assets) {
        this.type = type;
        this.bounds = new Rectangle(x, y, 112f, 96f);
        brokenSprite = createSprite(assets.getRepairStationBroken(), x, y);
        repairedSprite = createSprite(assets.getRepairStation(), x, y);
        hammerSprite = new Sprite(assets.getRepairHammer());
        hammerSprite.setSize(58f, 58f);
        hammerSprite.setOriginCenter();
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
        if (repairing) renderRepairTool(batch);
    }

    private void renderRepairTool(SpriteBatch batch) {
        float impact = (MathUtils.sin(animationTime * 13f) + 1f) * 0.5f;
        hammerSprite.setPosition(getCenterX() + 18f, bounds.y + bounds.height + 6f);
        hammerSprite.setRotation(-22f - impact * 36f);
        hammerSprite.setColor(1f, 1f, 1f, 0.98f);
        hammerSprite.draw(batch);
    }

    public void renderStatus(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.18f, 0.92f, 0.58f, 1f)
            : new Color(0.42f, 0.86f, 0.96f, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 1f : 0.72f);
        shapes.circle(bounds.x + bounds.width - 14f, bounds.y + bounds.height - 6f,
            nearby ? 9f : 6f, 18);
        if (repairing) renderRepairProgress(shapes, accent);
    }

    private void renderRepairProgress(ShapeRenderer shapes, Color accent) {
        float cx = getCenterX();
        float y = bounds.y + bounds.height + 2f;
        float progress = getRepairProgress();
        shapes.setColor(0.002f, 0.012f, 0.020f, 0.94f);
        shapes.rect(cx - 57f, y, 114f, 24f);
        shapes.setColor(accent.r, accent.g, accent.b, 0.28f);
        shapes.rect(cx - 57f, y + 22f, 114f, 2f);
        shapes.setColor(0.07f, 0.12f, 0.15f, 1f);
        shapes.rect(cx - 49f, y + 8f, 98f, 7f);
        shapes.setColor(accent.r, accent.g, accent.b, 1f);
        shapes.rect(cx - 49f, y + 8f, 98f * progress, 7f);
        shapes.setColor(0.76f, 0.97f, 1f, 0.82f);
        shapes.rect(cx - 49f, y + 13f, 98f * progress, 2f);
        shapes.setColor(0.01f, 0.025f, 0.035f, 0.78f);
        for (int i = 1; i < 4; i++) shapes.rect(cx - 49f + i * 24.5f, y + 8f, 1f, 7f);
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
