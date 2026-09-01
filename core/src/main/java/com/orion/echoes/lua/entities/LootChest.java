package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.enums.ItemType;

/** Bau persistente que ejeta materiais de crafting ao ser inspecionado. */
public final class LootChest {
    private static final float WIDTH = 118f;
    private static final float HEIGHT = 82f;

    private final int index;
    private final boolean mars;
    private final Rectangle bounds;
    private final Sprite closedSprite;
    private final Sprite openSprite;
    private boolean opened;
    private boolean lootSpawned;
    private float animationTime;

    public LootChest(int index, float x, float y, boolean mars, GameAssets assets, boolean opened) {
        this.index = index;
        this.mars = mars;
        this.opened = opened;
        bounds = new Rectangle(x, y, WIDTH, HEIGHT);
        closedSprite = create(assets.getLootChestClosed(), x, y);
        openSprite = create(assets.getLootChestOpen(), x, y);
    }

    private Sprite create(com.badlogic.gdx.graphics.Texture texture, float x, float y) {
        Sprite result = new Sprite(texture);
        result.setSize(WIDTH + 18f, HEIGHT + 18f);
        result.setPosition(x - 9f, y - 8f);
        result.setOriginCenter();
        return result;
    }

    public void update(float delta) {
        animationTime += delta;
        Sprite sprite = opened ? openSprite : closedSprite;
        float pulse = opened ? 1f : 1f + MathUtils.sin(animationTime * 2.2f) * 0.018f;
        sprite.setScale(pulse);
        sprite.setColor(opened ? new Color(0.80f, 0.88f, 0.92f, 1f) : Color.WHITE);
    }

    public void render(SpriteBatch batch) {
        (opened ? openSprite : closedSprite).draw(batch);
    }

    public void renderGlow(ShapeRenderer shapes, boolean nearby) {
        if (opened) return;
        float pulse = 1f + MathUtils.sin(animationTime * 3f) * 0.08f;
        Color accent = mars ? new Color(1f, 0.43f, 0.14f, 1f) : new Color(0.12f, 0.88f, 1f, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 0.25f : 0.12f);
        shapes.circle(getCenterX(), getCenterY(), 76f * pulse, 32);
        shapes.setColor(accent.r, accent.g, accent.b, nearby ? 0.76f : 0.42f);
        shapes.circle(getCenterX(), bounds.y + bounds.height + 8f, nearby ? 7f : 4f, 18);
    }

    public boolean isPlayerNear(Player player) {
        Rectangle interaction = new Rectangle(bounds.x - 46f, bounds.y - 42f,
            bounds.width + 92f, bounds.height + 84f);
        return interaction.overlaps(player.getBounds());
    }

    public void markOpened() {
        opened = true;
    }

    public void spawnLoot(Array<CollectibleItem> items, GameAssets assets) {
        if (lootSpawned) return;
        ItemType[][] lunarLoot = {
            {ItemType.ALLOY_PLATE, ItemType.ALLOY_PLATE, ItemType.FIBER_MESH, ItemType.AMMO_CELL},
            {ItemType.QUANTUM_CORE, ItemType.FIBER_MESH, ItemType.ALLOY_PLATE, ItemType.AMMO_CELL},
            {ItemType.FIBER_MESH, ItemType.FIBER_MESH, ItemType.ALLOY_PLATE},
            {ItemType.QUANTUM_CORE, ItemType.ALLOY_PLATE, ItemType.FIBER_MESH, ItemType.ALLOY_PLATE}
        };
        ItemType[][] marsLoot = {
            {ItemType.ALLOY_PLATE, ItemType.QUANTUM_CORE, ItemType.FIBER_MESH, ItemType.AMMO_CELL},
            {ItemType.QUANTUM_CORE, ItemType.QUANTUM_CORE, ItemType.ALLOY_PLATE},
            {ItemType.FIBER_MESH, ItemType.ALLOY_PLATE, ItemType.ALLOY_PLATE, ItemType.QUANTUM_CORE}
        };
        ItemType[] loot = (mars ? marsLoot : lunarLoot)[MathUtils.clamp(index, 0,
            (mars ? marsLoot : lunarLoot).length - 1)];
        float[][] offsets = {{-76f, -18f}, {122f, -12f}, {-46f, 86f}, {108f, 82f}};
        for (int i = 0; i < loot.length; i++) {
            items.add(new CollectibleItem(loot[i], bounds.x + offsets[i][0], bounds.y + offsets[i][1], assets, mars));
        }
        lootSpawned = true;
    }

    public int getIndex() { return index; }
    public boolean isMars() { return mars; }
    public boolean isOpened() { return opened; }
    public boolean isLootSpawned() { return lootSpawned; }
    public float getCenterX() { return bounds.x + bounds.width * 0.5f; }
    public float getCenterY() { return bounds.y + bounds.height * 0.5f; }
    public Rectangle getBounds() { return bounds; }
}
