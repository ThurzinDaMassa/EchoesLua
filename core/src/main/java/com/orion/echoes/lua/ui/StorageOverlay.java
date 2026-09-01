package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.progress.MissionState;

/** Bau em duas grades com transferencia de pilhas por arrastar e soltar. */
public final class StorageOverlay {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;
    private static final Rectangle PANEL = new Rectangle(226f, 34f, 828f, 652f);
    private static final Rectangle CLOSE = new Rectangle(884f, 618f, 132f, 40f);
    private static final Rectangle CRAFT = new Rectangle(456f, 270f, 368f, 66f);
    private static final float GRID_X = 430f;
    private static final float CHEST_TOP = 492f;
    private static final float INVENTORY_TOP = 236f;
    private static final float SLOT = 56f;
    private static final float GAP = 8f;
    private static final int COLS = 6;

    private final GameAssets assets;
    private final AudioManager audio;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(WIDTH, HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final UiFonts fonts = new UiFonts();
    private final Vector2 pointer = new Vector2();
    private boolean wasTouched;
    private ItemType draggedType;
    private int draggedSlot = -1;
    private boolean draggedFromStorage;

    public StorageOverlay(GameAssets assets, AudioManager audio) {
        this.assets = assets;
        this.audio = audio;
        camera.position.set(WIDTH * 0.5f, HEIGHT * 0.5f, 0f);
        camera.update();
    }

    public void resetInput() {
        wasTouched = Gdx.input.isTouched();
        cancelDrag();
    }

    public boolean update(MissionState mission) {
        updatePointer();
        boolean touched = Gdx.input.isTouched();
        if (touched && !wasTouched && CLOSE.contains(pointer)) {
            wasTouched = touched;
            cancelDrag();
            return false;
        }
        if (!mission.hasMarsStorage()) {
            if (touched && !wasTouched && CRAFT.contains(pointer)
                && mission.craftMarsStorage()) audio.playCraft();
            wasTouched = touched;
            return true;
        }
        if (touched && !wasTouched) beginDrag(mission);
        if (!touched && wasTouched) endDrag(mission);
        wasTouched = touched;
        return true;
    }

    private void beginDrag(MissionState mission) {
        int storage = storageSlotAt(pointer.x, pointer.y);
        if (storage >= 0) {
            ItemType type = mission.getStorageSlot(storage);
            if (type != null && mission.getStoredCount(type) > 0) {
                draggedType = type;
                draggedSlot = storage;
                draggedFromStorage = true;
            }
            return;
        }
        int inventory = inventorySlotAt(pointer.x, pointer.y);
        if (inventory >= 0) {
            ItemType type = mission.getInventorySlot(inventory);
            if (type != null && mission.getCount(type) > 0 && !mission.isEquipped(type)) {
                draggedType = type;
                draggedSlot = inventory;
                draggedFromStorage = false;
            }
        }
    }

    private void endDrag(MissionState mission) {
        if (draggedType == null) return;
        boolean changed = false;
        int storageTarget = storageSlotAt(pointer.x, pointer.y);
        int inventoryTarget = inventorySlotAt(pointer.x, pointer.y);
        if (draggedFromStorage) {
            if (inventoryTarget >= 0) {
                changed = mission.moveStorageStackToInventory(draggedSlot, inventoryTarget);
            } else if (storageTarget >= 0) {
                mission.swapStorageSlots(draggedSlot, storageTarget);
                changed = draggedSlot != storageTarget;
            }
        } else {
            if (storageTarget >= 0) {
                changed = mission.moveInventoryStackToStorage(draggedSlot, storageTarget);
            } else if (inventoryTarget >= 0) {
                mission.swapInventorySlots(draggedSlot, inventoryTarget);
                changed = draggedSlot != inventoryTarget;
            }
        }
        if (changed) audio.playItemMove();
        cancelDrag();
    }

    public void render(SpriteBatch batch, MissionState mission) {
        updatePointer();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.005f, 0.008f, 0.84f);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        UiTheme.panel(shapes, PANEL.x, PANEL.y, PANEL.width, PANEL.height, UiTheme.WARNING);
        UiTheme.panel(shapes, CLOSE.x, CLOSE.y, CLOSE.width, CLOSE.height,
            CLOSE.contains(pointer) ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        if (!mission.hasMarsStorage()) {
            UiTheme.panel(shapes, CRAFT.x, CRAFT.y, CRAFT.width, CRAFT.height,
                CRAFT.contains(pointer) ? UiTheme.GREEN : UiTheme.CYAN);
        } else {
            drawGridShapes(mission, true);
            drawGridShapes(mission, false);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.heading.setColor(UiTheme.TEXT);
        fonts.heading.draw(batch, "BAU PRESSURIZADO ARES", 270f, 650f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, mission.hasMarsStorage()
            ? "ARRASTE PILHAS ENTRE O BAU E A MOCHILA"
            : "MODULO DE ARMAZENAMENTO AINDA NAO FABRICADO", 270f, 620f);
        fonts.micro.setColor(CLOSE.contains(pointer) ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "FECHAR", CLOSE.x, 644f, CLOSE.width, Align.center, false);
        batch.draw(assets.getMarsStorageChest(), 270f, 466f, 136f, 91f);
        if (!mission.hasMarsStorage()) {
            fonts.body.setColor(UiTheme.MUTED);
            fonts.body.draw(batch,
                "Monte um compartimento seguro para conservar recursos entre expedicoes.",
                344f, 420f, 592f, Align.center, true);
            fonts.label.setColor(UiTheme.TEXT);
            fonts.label.draw(batch, "FABRICAR // 2 LIGAS + 1 FIBRA",
                CRAFT.x, 310f, CRAFT.width, Align.center, false);
        } else {
            fonts.label.setColor(UiTheme.WARNING);
            fonts.label.draw(batch, "BAU // 18 SLOTS", GRID_X, 572f);
            fonts.label.setColor(UiTheme.CYAN_SOFT);
            fonts.label.draw(batch, "INVENTARIO // 24 SLOTS", GRID_X, 316f);
            drawGridItems(batch, mission, true);
            drawGridItems(batch, mission, false);
            if (draggedType != null) batch.draw(icon(draggedType), pointer.x - 27f,
                pointer.y - 27f, 54f, 54f);
        }
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawGridShapes(MissionState mission, boolean storage) {
        int count = storage ? MissionState.MARS_STORAGE_SIZE : MissionState.INVENTORY_SIZE;
        for (int i = 0; i < count; i++) {
            Rectangle rect = storage ? storageRect(i) : inventoryRect(i);
            ItemType type = storage ? mission.getStorageSlot(i) : mission.getInventorySlot(i);
            boolean occupied = type != null && (storage
                ? mission.getStoredCount(type) > 0
                : mission.getCount(type) > 0 && !mission.isEquipped(type));
            boolean hovered = rect.contains(pointer);
            shapes.setColor(hovered ? 0.070f : occupied ? 0.030f : 0.012f,
                hovered ? 0.11f : occupied ? 0.060f : 0.028f,
                hovered ? 0.13f : occupied ? 0.078f : 0.040f, 0.99f);
            shapes.rect(rect.x, rect.y, rect.width, rect.height);
            Color border = hovered ? UiTheme.WARNING : storage ? UiTheme.BORDER : UiTheme.CYAN_SOFT;
            shapes.setColor(border);
            shapes.rect(rect.x, rect.y, rect.width, 2f);
            shapes.rect(rect.x, rect.y + rect.height - 2f, rect.width, 2f);
            shapes.rect(rect.x, rect.y, 2f, rect.height);
            shapes.rect(rect.x + rect.width - 2f, rect.y, 2f, rect.height);
        }
    }

    private void drawGridItems(SpriteBatch batch, MissionState mission, boolean storage) {
        int count = storage ? MissionState.MARS_STORAGE_SIZE : MissionState.INVENTORY_SIZE;
        for (int i = 0; i < count; i++) {
            ItemType type = storage ? mission.getStorageSlot(i) : mission.getInventorySlot(i);
            int amount = type == null ? 0 : storage
                ? mission.getStoredCount(type) : mission.getCount(type);
            if (type == null || amount <= 0 || (!storage && mission.isEquipped(type))) continue;
            Rectangle rect = storage ? storageRect(i) : inventoryRect(i);
            batch.draw(icon(type), rect.x + 7f, rect.y + 7f, 42f, 42f);
            fonts.micro.setColor(UiTheme.TEXT);
            fonts.micro.draw(batch, String.valueOf(amount), rect.x + 34f, rect.y + 16f,
                18f, Align.center, false);
        }
    }

    private Rectangle storageRect(int index) {
        return gridRect(index, CHEST_TOP);
    }

    private Rectangle inventoryRect(int index) {
        return gridRect(index, INVENTORY_TOP);
    }

    private Rectangle gridRect(int index, float top) {
        int col = index % COLS;
        int row = index / COLS;
        return new Rectangle(GRID_X + col * (SLOT + GAP), top - row * (SLOT + GAP), SLOT, SLOT);
    }

    private int storageSlotAt(float x, float y) {
        for (int i = 0; i < MissionState.MARS_STORAGE_SIZE; i++)
            if (storageRect(i).contains(x, y)) return i;
        return -1;
    }

    private int inventorySlotAt(float x, float y) {
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++)
            if (inventoryRect(i).contains(x, y)) return i;
        return -1;
    }

    private Texture icon(ItemType type) {
        return switch (type) {
            case OXYGEN -> assets.getOxygen();
            case FOOD -> assets.getFood();
            case ICE_ROCK -> assets.getIceRock();
            case MEDKIT -> assets.getMedkit();
            case ANTENNA_PART -> assets.getAntennaPart();
            case ENERGY_PART -> assets.getEnergyPart();
            case EXTRACTION_PART -> assets.getExtractionPart();
            case GREENHOUSE_PART -> assets.getGreenhousePart();
            case WEAPON_PART_A -> assets.getWeaponPartA();
            case WEAPON_PART_B -> assets.getWeaponPartB();
            case WEAPON_PART_C -> assets.getWeaponPartC();
            case AMMO_CELL -> assets.getEnergyProjectile();
            case ALLOY_PLATE -> assets.getAlloyPlate();
            case QUANTUM_CORE -> assets.getQuantumCore();
            case FIBER_MESH -> assets.getFiberMesh();
            case MINING_TOOL -> assets.getMiningTool();
            case REPAIR_TOOL -> assets.getRepairTool();
            case ARMOR_HELMET -> assets.getArmorHelmet();
            case ARMOR_CHEST -> assets.getArmorChest();
            case ARMOR_BOOTS -> assets.getArmorBoots();
        };
    }

    private void cancelDrag() {
        draggedType = null;
        draggedSlot = -1;
        draggedFromStorage = false;
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
    }

    public void resize(int width, int height) { viewport.update(width, height, true); }

    public void dispose() {
        shapes.dispose();
        fonts.close();
    }
}
