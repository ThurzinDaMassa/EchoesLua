package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.orion.echoes.lua.assets.GameAssets;

/** Satelite de superficie marciano com estados quebrado, reparando e operacional. */
public class MarsSatellite {
    public static final float REPAIR_DURATION = 3f;

    private final int index;
    private final Rectangle bounds;
    private final Sprite brokenSprite;
    private final Sprite repairedSprite;
    private final Sprite hammerSprite;
    private float animationTime;
    private float repairTime;
    private boolean repairing;

    public MarsSatellite(int index, float x, float y, GameAssets assets) {
        this.index = index;
        bounds = new Rectangle(x, y, 128f, 98f);
        brokenSprite = createSprite(assets.getMarsSatelliteBroken(), x, y);
        repairedSprite = createSprite(assets.getMarsSatelliteRepaired(), x, y);
        hammerSprite = new Sprite(assets.getRepairHammer());
        hammerSprite.setSize(62f, 62f);
        hammerSprite.setOriginCenter();
    }

    private Sprite createSprite(com.badlogic.gdx.graphics.Texture texture, float x, float y) {
        Sprite sprite = new Sprite(texture);
        sprite.setSize(184f, 184f);
        sprite.setPosition(x - 28f, y - 31f);
        sprite.setOriginCenter();
        return sprite;
    }

    public void update(float delta) {
        animationTime += delta;
        if (repairing) repairTime = Math.min(REPAIR_DURATION, repairTime + delta);
    }

    public void render(SpriteBatch batch, boolean repaired) {
        Sprite active = repaired ? repairedSprite : brokenSprite;
        float bob = repaired ? MathUtils.sin(animationTime * 2.2f) * 1.2f : 0f;
        active.setY(bounds.y - 31f + bob);
        active.setRotation(repairing ? MathUtils.sin(animationTime * 18f) * 0.7f : 0f);
        active.setColor(Color.WHITE);
        active.draw(batch);
        if (repairing) renderRepairTool(batch);
    }

    private void renderRepairTool(SpriteBatch batch) {
        float impact = (MathUtils.sin(animationTime * 13f) + 1f) * 0.5f;
        hammerSprite.setPosition(getCenterX() + 16f, bounds.y + bounds.height + 7f);
        hammerSprite.setRotation(-20f - impact * 38f);
        hammerSprite.setColor(1f, 1f, 1f, 0.98f);
        hammerSprite.draw(batch);
    }

    public void renderGlow(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.20f, 0.96f, 0.62f, 1f)
            : repairing ? new Color(1f, 0.66f, 0.16f, 1f)
            : new Color(0.96f, 0.30f, 0.14f, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 0.11f : 0.055f);
        shapes.circle(getCenterX(), getCenterY(), nearby ? 76f : 65f, 30);
    }

    public void renderRepairOverlay(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.20f, 0.96f, 0.62f, 1f)
            : repairing ? new Color(1f, 0.66f, 0.16f, 1f)
            : new Color(0.96f, 0.30f, 0.14f, 1f);
        shapes.setColor(accent);
        shapes.circle(bounds.x + bounds.width - 9f, bounds.y + bounds.height - 3f,
            nearby ? 8f : 5f, 18);
        if (repairing) renderRepairProgress(shapes, accent);
    }

    private void renderRepairProgress(ShapeRenderer shapes, Color accent) {
        float cx = getCenterX();
        float y = bounds.y + bounds.height + 2f;
        float progress = getRepairProgress();
        shapes.setColor(0.018f, 0.010f, 0.008f, 0.95f);
        shapes.rect(cx - 61f, y, 122f, 25f);
        shapes.setColor(accent.r, accent.g, accent.b, 0.30f);
        shapes.rect(cx - 61f, y + 23f, 122f, 2f);
        shapes.setColor(0.15f, 0.075f, 0.035f, 1f);
        shapes.rect(cx - 52f, y + 8f, 104f, 8f);
        shapes.setColor(accent.r, accent.g, accent.b, 1f);
        shapes.rect(cx - 52f, y + 8f, 104f * progress, 8f);
        shapes.setColor(1f, 0.86f, 0.56f, 0.86f);
        shapes.rect(cx - 52f, y + 14f, 104f * progress, 2f);
        shapes.setColor(0.035f, 0.018f, 0.010f, 0.78f);
        for (int i = 1; i < 4; i++) shapes.rect(cx - 52f + i * 26f, y + 8f, 1f, 8f);
    }

    public boolean isPlayerNear(Player player) {
        Rectangle interaction = new Rectangle(bounds);
        interaction.x -= 52f;
        interaction.y -= 52f;
        interaction.width += 104f;
        interaction.height += 104f;
        return interaction.overlaps(player.getBounds());
    }

    public void startRepair() {
        repairing = true;
        repairTime = 0f;
    }

    public void finishRepair() {
        repairing = false;
        repairTime = REPAIR_DURATION;
    }

    public void cancelRepair() {
        repairing = false;
        repairTime = 0f;
    }

    public boolean isRepairing() { return repairing; }
    public boolean isRepairComplete() { return repairing && repairTime >= REPAIR_DURATION; }
    public float getRepairProgress() { return MathUtils.clamp(repairTime / REPAIR_DURATION, 0f, 1f); }
    public int getIndex() { return index; }
    public float getCenterX() { return bounds.x + bounds.width / 2f; }
    public float getCenterY() { return bounds.y + bounds.height / 2f; }
    public Rectangle getBounds() { return bounds; }
}
