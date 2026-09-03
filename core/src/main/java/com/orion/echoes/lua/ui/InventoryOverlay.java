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
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.progress.MissionState;

/** Inventario de 24 slots com drag-and-drop, armadura, ferramentas e crafting. */
public final class InventoryOverlay {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;
    private static final float PANEL_X = 96f;
    private static final float PANEL_Y = 48f;
    private static final float PANEL_W = 1088f;
    private static final float PANEL_H = 624f;
    private static final float GRID_X = 482f;
    private static final float GRID_TOP_Y = 458f;
    private static final float SLOT = 56f;
    private static final float GAP = 8f;
    private static final int COLS = 6;
    private static final Rectangle CLOSE_BUTTON = new Rectangle(1010f, 600f, 132f, 42f);

    private static final ItemType[] RECIPES = {
        ItemType.ARMOR_HELMET, ItemType.ARMOR_CHEST, ItemType.ARMOR_BOOTS,
        ItemType.MINING_TOOL, ItemType.REPAIR_TOOL
    };

    private final GameAssets assets;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(WIDTH, HEIGHT, camera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final UiFonts fonts = new UiFonts();
    private final Vector2 pointer = new Vector2();
    private boolean open;
    private boolean wasTouched;
    private ItemType draggedType;
    private int draggedInventorySlot = -1;
    private boolean draggedFromEquipment;
    private ItemType hoveredRecipe;
    private ItemType hoveredItem;

    public InventoryOverlay(GameAssets assets) {
        this.assets = assets;
        camera.position.set(WIDTH * 0.5f, HEIGHT * 0.5f, 0f);
        camera.update();
    }

    public void toggle() {
        open = !open;
        wasTouched = false;
        if (!open) cancelDrag();
    }

    public boolean isOpen() { return open; }
    public void close() {
        open = false;
        wasTouched = false;
        cancelDrag();
    }

    public void update(MissionState mission) {
        if (!open) return;
        updatePointer();
        hoveredRecipe = recipeAt(pointer.x, pointer.y);
        hoveredItem = itemAt(mission, pointer.x, pointer.y);
        boolean touched = Gdx.input.isTouched();
        if (touched && !wasTouched && CLOSE_BUTTON.contains(pointer)) {
            close();
            return;
        }
        if (touched && !wasTouched) beginPointerAction(mission);
        if (!touched && wasTouched) endPointerAction(mission);
        wasTouched = touched;
    }

    private void beginPointerAction(MissionState mission) {
        ItemType recipe = recipeAt(pointer.x, pointer.y);
        if (recipe != null) {
            mission.craftEquipment(recipe);
            return;
        }
        ItemType equipped = equipmentAt(mission, pointer.x, pointer.y);
        if (equipped != null) {
            draggedType = equipped;
            draggedFromEquipment = true;
            return;
        }
        int slot = inventorySlotAt(pointer.x, pointer.y);
        if (slot >= 0) {
            ItemType type = mission.getInventorySlot(slot);
            if (type != null && mission.getCount(type) > 0 && !mission.isEquipped(type)) {
                draggedType = type;
                draggedInventorySlot = slot;
            }
        }
    }

    private void endPointerAction(MissionState mission) {
        if (draggedType == null) return;
        ItemType equipmentTarget = compatibleEquipmentTarget(pointer.x, pointer.y);
        if (equipmentTarget == draggedType) {
            mission.equip(draggedType);
        } else {
            int target = inventorySlotAt(pointer.x, pointer.y);
            if (target >= 0) {
                if (draggedFromEquipment) mission.unequip(draggedType);
                else mission.swapInventorySlots(draggedInventorySlot, target);
            }
        }
        cancelDrag();
    }

    private void cancelDrag() {
        draggedType = null;
        draggedInventorySlot = -1;
        draggedFromEquipment = false;
    }

    public void render(SpriteBatch batch, MissionState mission) {
        if (!open) return;
        updatePointer();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.010f, 0.78f);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        UiTheme.panel(shapes, PANEL_X, PANEL_Y, PANEL_W, PANEL_H, UiTheme.CYAN);
        UiTheme.panel(shapes, CLOSE_BUTTON.x, CLOSE_BUTTON.y, CLOSE_BUTTON.width,
            CLOSE_BUTTON.height, CLOSE_BUTTON.contains(pointer) ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        drawSectionPanel(126f, 78f, 306f, 498f, UiTheme.CYAN_SOFT);
        drawSectionPanel(456f, 248f, 698f, 328f, UiTheme.CYAN_SOFT);
        drawSectionPanel(456f, 78f, 698f, 150f, UiTheme.GREEN);
        drawEquipmentShapes(mission);
        drawGridShapes(mission);
        drawRecipeShapes(mission);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHeader(batch, mission);
        drawEquipmentText(batch, mission);
        drawGridItems(batch, mission);
        drawRecipes(batch, mission);
        if (draggedType != null) batch.draw(icon(draggedType), pointer.x - 28f, pointer.y - 28f, 56f, 56f);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawHeader(SpriteBatch batch, MissionState mission) {
        fonts.heading.setColor(UiTheme.TEXT);
        fonts.heading.draw(batch, "INVENTARIO EVA", 132f, 638f);
        fonts.micro.setColor(UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ORGANIZE A CARGA, EQUIPE MODULOS E FABRIQUE NOVOS ITENS", 132f, 606f);
        fonts.micro.setColor(CLOSE_BUTTON.contains(pointer) ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "FECHAR", CLOSE_BUTTON.x, 626f,
            CLOSE_BUTTON.width, Align.center, false);
    }

    private void drawSectionPanel(float x, float y, float width, float height, Color accent) {
        shapes.setColor(0.004f, 0.014f, 0.022f, 0.96f);
        shapes.rect(x, y, width, height);
        shapes.setColor(UiTheme.BORDER.r, UiTheme.BORDER.g, UiTheme.BORDER.b, 0.82f);
        shapes.rect(x, y, width, 1f);
        shapes.rect(x, y + height - 1f, width, 1f);
        shapes.setColor(accent.r, accent.g, accent.b, 0.88f);
        shapes.rect(x, y + height - 3f, 46f, 3f);
    }

    private void drawEquipmentShapes(MissionState mission) {
        for (int i = 0; i < 5; i++) drawSlotShape(equipmentRect(i), equipmentForSlot(mission, i) != null);
        float armor = mission.getArmorProtection() / 0.32f;
        UiTheme.bar(shapes, 154f, 96f, 250f, 9f, armor, UiTheme.GREEN);
    }

    private void drawGridShapes(MissionState mission) {
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++) {
            ItemType type = mission.getInventorySlot(i);
            boolean occupied = type != null && mission.getCount(type) > 0 && !mission.isEquipped(type);
            Rectangle rect = gridRect(i);
            drawSlotShape(rect, occupied);
            if (occupied) {
                shapes.setColor(0.002f, 0.008f, 0.013f, 0.94f);
                shapes.rect(rect.x + 38f, rect.y + 4f, 18f, 18f);
            }
        }
    }

    private void drawSlotShape(Rectangle rect, boolean occupied) {
        boolean hovered = rect.contains(pointer);
        shapes.setColor(hovered ? 0.055f : occupied ? 0.028f : 0.012f,
            hovered ? 0.12f : occupied ? 0.070f : 0.032f,
            hovered ? 0.15f : occupied ? 0.092f : 0.046f, 0.98f);
        shapes.rect(rect.x, rect.y, rect.width, rect.height);
        shapes.setColor(hovered ? UiTheme.CYAN : UiTheme.BORDER);
        shapes.rect(rect.x, rect.y, rect.width, 2f);
        shapes.rect(rect.x, rect.y + rect.height - 2f, rect.width, 2f);
        shapes.rect(rect.x, rect.y, 2f, rect.height);
        shapes.rect(rect.x + rect.width - 2f, rect.y, 2f, rect.height);
    }

    private void drawRecipeShapes(MissionState mission) {
        for (int i = 0; i < RECIPES.length; i++) {
            Rectangle rect = recipeRect(i);
            boolean enabled = mission.canCraftEquipment(RECIPES[i]);
            shapes.setColor(enabled ? 0.02f : 0.012f, enabled ? 0.11f : 0.034f,
                enabled ? 0.09f : 0.046f, 0.98f);
            shapes.rect(rect.x, rect.y, rect.width, rect.height);
            shapes.setColor(enabled ? UiTheme.GREEN : UiTheme.BORDER);
            shapes.rect(rect.x, rect.y, 3f, rect.height);
            if (rect.contains(pointer)) shapes.rect(rect.x, rect.y + rect.height - 2f, rect.width, 2f);
        }
    }

    private void drawEquipmentText(SpriteBatch batch, MissionState mission) {
        String[] labels = {"CAPACETE", "PEITORAL", "BOTAS", "MINERACAO", "REPARO"};
        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "EQUIPAMENTO", 154f, 548f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "5 ENCAIXES EVA", 154f, 524f);
        for (int i = 0; i < labels.length; i++) {
            Rectangle rect = equipmentRect(i);
            ItemType equipped = equipmentForSlot(mission, i);
            if (equipped != null) batch.draw(icon(equipped), rect.x + 8f, rect.y + 8f, 48f, 48f);
            fonts.micro.setColor(equipped != null ? UiTheme.GREEN : UiTheme.MUTED);
            fonts.micro.draw(batch, labels[i], rect.x + 76f, rect.y + 40f);
            fonts.micro.draw(batch, equipped != null ? "EQUIPADO" : "SLOT VAZIO", rect.x + 76f, rect.y + 20f);
        }
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "PROTECAO CONTRA DANO", 154f, 126f);
        fonts.label.setColor(UiTheme.GREEN);
        fonts.label.draw(batch, Math.round(mission.getArmorProtection() * 100f) + "%", 344f, 126f,
            60f, Align.right, false);
    }

    private void drawGridItems(SpriteBatch batch, MissionState mission) {
        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "CARGA // 24 SLOTS", GRID_X, 550f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "ARRASTE PARA REORGANIZAR", GRID_X, 528f);
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++) {
            ItemType type = mission.getInventorySlot(i);
            if (type == null || mission.getCount(type) <= 0 || mission.isEquipped(type)) continue;
            Rectangle rect = gridRect(i);
            batch.draw(icon(type), rect.x + 7f, rect.y + 7f, 42f, 42f);
            fonts.micro.setColor(UiTheme.TEXT);
            fonts.micro.draw(batch, String.valueOf(mission.getCount(type)), rect.x + 34f,
                rect.y + 16f, 18f, Align.center, false);
        }
        drawInspector(batch, mission);
    }

    private void drawRecipes(SpriteBatch batch, MissionState mission) {
        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "FABRICACAO RAPIDA", 478f, 210f);
        for (int i = 0; i < RECIPES.length; i++) {
            Rectangle rect = recipeRect(i);
            ItemType recipe = RECIPES[i];
            batch.draw(icon(recipe), rect.x + 9f, rect.y + 14f, 42f, 42f);
            fonts.micro.setColor(mission.canCraftEquipment(recipe) ? UiTheme.GREEN : UiTheme.MUTED);
            fonts.micro.draw(batch, compact(recipe), rect.x + 56f, rect.y + 43f);
            fonts.micro.draw(batch, mission.getCount(recipe) > 0 ? "CRIADO" : "CRAFTAR",
                rect.x + 56f, rect.y + 21f);
        }
        if (hoveredRecipe != null) {
            fonts.micro.setColor(UiTheme.TEXT);
            fonts.micro.draw(batch, MissionState.getItemLabel(hoveredRecipe) + " // "
                + mission.getRecipe(hoveredRecipe), 478f, 96f);
        } else {
            fonts.micro.setColor(UiTheme.MUTED);
            fonts.micro.draw(batch, "Abra baus para obter liga, nucleos e fibras.", 478f, 96f);
        }
    }

    private void drawInspector(SpriteBatch batch, MissionState mission) {
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "DETALHES", 918f, 516f);
        if (hoveredItem == null) {
            fonts.micro.draw(batch, "Passe o cursor sobre", 918f, 474f);
            fonts.micro.draw(batch, "um item para inspecionar.", 918f, 454f);
            return;
        }
        batch.draw(icon(hoveredItem), 926f, 410f, 84f, 84f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, MissionState.getItemLabel(hoveredItem).toUpperCase(),
            918f, 388f, 210f, Align.left, true);
        fonts.micro.setColor(UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, hoveredItem.isArmor() ? "MODULO DE ARMADURA"
            : hoveredItem.isTool() ? "FERRAMENTA EVA" : "ITEM DE CARGA", 918f, 344f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "QUANTIDADE  " + mission.getCount(hoveredItem), 918f, 320f);
        if (hoveredItem.isEquipment()) {
            fonts.micro.setColor(mission.isEquipped(hoveredItem) ? UiTheme.GREEN : UiTheme.WARNING);
            fonts.micro.draw(batch, mission.isEquipped(hoveredItem)
                ? "EQUIPADO" : "ARRASTE PARA O SLOT", 918f, 294f);
        }
    }

    private Rectangle gridRect(int index) {
        int col = index % COLS;
        int row = index / COLS;
        return new Rectangle(GRID_X + col * (SLOT + GAP), GRID_TOP_Y - row * (SLOT + GAP), SLOT, SLOT);
    }

    private int inventorySlotAt(float x, float y) {
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++) if (gridRect(i).contains(x, y)) return i;
        return -1;
    }

    private Rectangle equipmentRect(int index) {
        return new Rectangle(154f, 438f - index * 70f, 64f, 64f);
    }

    private ItemType equipmentForSlot(MissionState mission, int slot) {
        return switch (slot) {
            case 0 -> mission.getEquippedHelmet();
            case 1 -> mission.getEquippedChest();
            case 2 -> mission.getEquippedBoots();
            case 3 -> mission.getEquippedMiningTool();
            case 4 -> mission.getEquippedRepairTool();
            default -> null;
        };
    }

    private ItemType equipmentAt(MissionState mission, float x, float y) {
        for (int i = 0; i < 5; i++) if (equipmentRect(i).contains(x, y)) return equipmentForSlot(mission, i);
        return null;
    }

    private ItemType compatibleEquipmentTarget(float x, float y) {
        ItemType[] types = {ItemType.ARMOR_HELMET, ItemType.ARMOR_CHEST, ItemType.ARMOR_BOOTS,
            ItemType.MINING_TOOL, ItemType.REPAIR_TOOL};
        for (int i = 0; i < types.length; i++) if (equipmentRect(i).contains(x, y)) return types[i];
        return null;
    }

    private Rectangle recipeRect(int index) {
        return new Rectangle(478f + index * 132f, 112f, 124f, 68f);
    }

    private ItemType recipeAt(float x, float y) {
        for (int i = 0; i < RECIPES.length; i++) if (recipeRect(i).contains(x, y)) return RECIPES[i];
        return null;
    }

    private ItemType itemAt(MissionState mission, float x, float y) {
        ItemType equipped = equipmentAt(mission, x, y);
        if (equipped != null) return equipped;
        int slot = inventorySlotAt(x, y);
        if (slot >= 0) {
            ItemType type = mission.getInventorySlot(slot);
            if (type != null && mission.getCount(type) > 0 && !mission.isEquipped(type)) return type;
        }
        return recipeAt(x, y);
    }

    private String compact(ItemType type) {
        return switch (type) {
            case ARMOR_HELMET -> "CAPACETE";
            case ARMOR_CHEST -> "PEITORAL";
            case ARMOR_BOOTS -> "BOTAS";
            case MINING_TOOL -> "PICARETA";
            case REPAIR_TOOL -> "REPARO";
            default -> type.name();
        };
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
            case METHANE_SAMPLE -> assets.getMethaneSample();
            case TITAN_CORE -> assets.getTitanPowerCore();
        };
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
