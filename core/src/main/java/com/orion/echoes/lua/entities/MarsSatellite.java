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
    private float animationTime;
    private float repairTime;
    private boolean repairing;

    public MarsSatellite(int index, float x, float y, GameAssets assets) {
        this.index = index;
        bounds = new Rectangle(x, y, 128f, 98f);
        brokenSprite = createSprite(assets.getMarsSatelliteBroken(), x, y);
        repairedSprite = createSprite(assets.getMarsSatelliteRepaired(), x, y);
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
    }

    public void renderGlow(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.20f, 0.96f, 0.62f, 1f)
            : repairing ? new Color(1f, 0.66f, 0.16f, 1f)
            : new Color(0.96f, 0.30f, 0.14f, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 0.34f : 0.16f);
        shapes.circle(getCenterX(), getCenterY(), nearby ? 86f : 70f, 30);
    }

    public void renderRepairOverlay(ShapeRenderer shapes, boolean repaired, boolean nearby) {
        Color accent = repaired ? new Color(0.20f, 0.96f, 0.62f, 1f)
            : repairing ? new Color(1f, 0.66f, 0.16f, 1f)
            : new Color(0.96f, 0.30f, 0.14f, 1f);
        shapes.setColor(accent);
        shapes.circle(bounds.x + bounds.width - 9f, bounds.y + bounds.height - 3f,
            nearby ? 8f : 5f, 18);
        if (repairing) renderHammer(shapes);
    }

    private void renderHammer(ShapeRenderer shapes) {
        float cx = getCenterX();
        float y = bounds.y + bounds.height + 42f;
        float swing = -28f + MathUtils.sin(animationTime * 11f) * 24f;
        shapes.setColor(0.72f, 0.42f, 0.16f, 1f);
        shapes.rect(cx - 3f, y - 5f, 3f, 5f, 6f, 34f, 1f, 1f, swing);
        shapes.setColor(0.84f, 0.90f, 0.94f, 1f);
        shapes.rect(cx - 15f, y + 24f, 30f, 11f);
        shapes.setColor(0.02f, 0.02f, 0.02f, 0.92f);
        shapes.rect(cx - 50f, y - 19f, 100f, 8f);
        shapes.setColor(1f, 0.48f, 0.14f, 1f);
        shapes.rect(cx - 50f, y - 19f, 100f * getRepairProgress(), 8f);
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
