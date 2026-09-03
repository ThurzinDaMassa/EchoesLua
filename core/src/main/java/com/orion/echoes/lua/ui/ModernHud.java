package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.entities.RepairStation;
import com.orion.echoes.lua.entities.LootChest;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.MissionSystem;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.utils.GameConstants;

public class ModernHud {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final UiFonts fonts;
    private final GameAssets assets;
    private final InventoryOverlay inventoryOverlay;
    private final Vector2 pointer = new Vector2();
    private final Rectangle overlayPrimary = new Rectangle(423f, 258f, 210f, 52f);
    private final Rectangle overlaySecondary = new Rectangle(647f, 258f, 210f, 52f);

    private float animationTime;
    private boolean inventoryOpen;
    private float inventoryReveal;
    private float shownOxygen = -1f;
    private float shownHealth = -1f;
    private float shownEnergy = -1f;
    private boolean weaponReloading;
    private float weaponReloadProgress = 1f;

    public ModernHud(GameAssets assets) {
        this.assets = assets;
        camera = new OrthographicCamera();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f);
        camera.update();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        inventoryOverlay = new InventoryOverlay(assets);
    }

    public void setWeaponReload(boolean reloading, float progress) {
        weaponReloading = reloading;
        weaponReloadProgress = MathUtils.clamp(progress, 0f, 1f);
    }

    public void render(
        SpriteBatch batch,
        PlayerStatus status,
        Player player,
        boolean baseEntranceNearby,
        MissionSystem mission,
        Portal portal,
        float missionTime,
        boolean paused,
        boolean missionFailed,
        RepairStation nearbyStation,
        LootChest nearbyChest,
        boolean portalNearby
    ) {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 20f);
        animationTime += delta;
        updatePointer();
        boolean inventoryKey = Gdx.input.isKeyJustPressed(Input.Keys.E);
        boolean closeKey = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        if (inventoryOverlay.isOpen() && (inventoryKey || closeKey)) {
            inventoryOverlay.close();
        } else if (!paused && !missionFailed && inventoryKey
            && !baseEntranceNearby && nearbyStation == null && !portalNearby) {
            inventoryOverlay.toggle();
        }
        inventoryOpen = inventoryOverlay.isOpen();
        inventoryOverlay.update(mission.getState());
        inventoryOpen = inventoryOverlay.isOpen();
        if (shownOxygen < 0f) {
            shownOxygen = status.getOxygen();
            shownHealth = status.getHealth();
            shownEnergy = status.getEnergy();
        }
        float response = 1f - (float) Math.pow(0.0008f, delta);
        shownOxygen = MathUtils.lerp(shownOxygen, status.getOxygen(), response);
        shownHealth = MathUtils.lerp(shownHealth, status.getHealth(), response);
        shownEnergy = MathUtils.lerp(shownEnergy, status.getEnergy(), response);
        inventoryReveal = MathUtils.lerp(inventoryReveal, inventoryOpen ? 1f : 0f,
            1f - (float) Math.pow(0.0002f, delta));
        enableBlend();
        drawInterfaceShapes(status, baseEntranceNearby, mission, portal, paused,
            missionFailed, nearbyStation, nearbyChest, portalNearby);
        drawInterfaceText(batch, status, player, baseEntranceNearby, mission, portal,
            missionTime, nearbyStation, nearbyChest, portalNearby);
        disableBlend();
        inventoryOverlay.render(batch, mission.getState());
    }

    private void drawInterfaceShapes(
        PlayerStatus status,
        boolean insideBase,
        MissionSystem mission,
        Portal portal,
        boolean paused,
        boolean missionFailed,
        RepairStation nearbyStation,
        LootChest nearbyChest,
        boolean portalNearby
    ) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        UiTheme.panel(shapes, 24f, 624f, 246f, 72f,
            insideBase ? UiTheme.GREEN : UiTheme.CYAN);
        UiTheme.panel(shapes, 288f, 624f, 682f, 72f, UiTheme.CYAN);
        UiTheme.panel(shapes, 988f, 624f, 268f, 72f,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);
        UiTheme.panel(shapes, 1000f, 532f, 256f, 74f,
            weaponReloading ? UiTheme.CYAN : UiTheme.WARNING);
        if (weaponReloading) {
            UiTheme.bar(shapes, 1090f, 540f, 142f, 5f,
                weaponReloadProgress, UiTheme.CYAN);
        }
        UiTheme.panel(shapes, 1144f, 344f, 112f, 180f, UiTheme.CYAN_SOFT);

        UiTheme.bar(shapes, 72f, 658f, 126f, 5f,
            shownOxygen / GameConstants.MAX_OXYGEN,
            oxygenColor(status.getOxygen()));
        UiTheme.bar(shapes, 72f, 643f, 126f, 5f,
            shownHealth / GameConstants.MAX_HEALTH,
            status.getHealth() < 30f ? UiTheme.DANGER : UiTheme.GREEN);
        UiTheme.bar(shapes, 72f, 628f, 126f, 5f,
            shownEnergy / GameConstants.MAX_ENERGY,
            UiTheme.GREEN);

        MissionState state = mission.getState();
        float missionProgress = (MathUtils.clamp(state.getRepairCount() / 4f, 0f, 1f)
            + (state.isIceProcessed() ? 1f : 0f)
            + (state.hasWeapon() ? 1f : 0f)
            + MathUtils.clamp(state.getEnemiesDefeated()
                / (float) MissionState.LUNAR_ENEMY_TARGET, 0f, 1f)) / 4f;
        UiTheme.bar(shapes, 1008f, 627f, 226f, 4f, missionProgress,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN);

        shapes.setColor(0.004f, 0.012f, 0.018f, 0.82f);
        shapes.rect(24f, 20f, 250f, 32f);
        shapes.setColor(UiTheme.CYAN_SOFT);
        shapes.rect(24f, 51f, 250f, 1f);

        UiTheme.panel(shapes, 1000f, 20f, 256f, 34f,
            inventoryOpen ? UiTheme.GREEN : UiTheme.CYAN_SOFT);

        if (nearbyChest != null && !nearbyChest.isOpened()) {
            UiTheme.panel(shapes, 294f, 20f, 686f, 62f, UiTheme.WARNING);
        } else if (nearbyStation != null) {
            UiTheme.panel(shapes, 294f, 20f, 686f, 62f,
                state.isRepaired(nearbyStation.getType()) ? UiTheme.GREEN : UiTheme.CYAN);
        } else if (portalNearby) {
            UiTheme.panel(shapes, 294f, 20f, 686f, 62f,
                portal.isActive() ? UiTheme.PURPLE : UiTheme.WARNING);
        } else if (insideBase) {
            UiTheme.panel(shapes, 294f, 20f, 686f, 70f, UiTheme.GREEN);
        } else if (mission.getState().getLastMessage() != null) {
            shapes.setColor(0.004f, 0.014f, 0.022f, 0.94f);
            shapes.rect(294f, 20f, 686f, 44f);
            shapes.setColor(UiTheme.CYAN_SOFT);
            shapes.rect(294f, 20f, 3f, 44f);
            shapes.setColor(UiTheme.BORDER);
            shapes.rect(294f, 63f, 686f, 1f);
        }

        if (status.getOxygen() < GameConstants.CRITICAL_OXYGEN_THRESHOLD && !missionFailed) {
            float pulse = 0.08f + MathUtils.sin(animationTime * 6f) * 0.035f;
            shapes.setColor(0.9f, 0.02f, 0.04f, pulse);
            shapes.rect(0f, 0f, WIDTH, HEIGHT);
            shapes.setColor(UiTheme.DANGER);
            shapes.rect(0f, HEIGHT - 4f, WIDTH, 4f);
        }

        if (paused || missionFailed) {
            shapes.setColor(0.002f, 0.006f, 0.010f, 0.82f);
            shapes.rect(0f, 0f, WIDTH, HEIGHT);
            UiTheme.panel(shapes, 385f, 220f, 510f, 280f,
                missionFailed ? UiTheme.DANGER : UiTheme.CYAN);
            drawOverlayButton(overlayPrimary,
                missionFailed ? UiTheme.DANGER : UiTheme.CYAN,
                overlayPrimary.contains(pointer));
            drawOverlayButton(overlaySecondary, UiTheme.CYAN_SOFT,
                overlaySecondary.contains(pointer));
        }

        shapes.end();
    }

    private void drawResourceDivider(float x) {
        shapes.setColor(UiTheme.BORDER);
        shapes.rect(x, 641f, 1f, 38f);
    }

    private Color oxygenColor(float oxygen) {
        if (oxygen < GameConstants.CRITICAL_OXYGEN_THRESHOLD) return UiTheme.DANGER;
        if (oxygen < 50f) return UiTheme.WARNING;
        return UiTheme.CYAN;
    }

    private void drawInterfaceText(
        SpriteBatch batch,
        PlayerStatus status,
        Player player,
        boolean insideBase,
        MissionSystem mission,
        Portal portal,
        float missionTime,
        RepairStation nearbyStation,
        LootChest nearbyChest,
        boolean portalNearby
    ) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        label(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "EVA-01 // TELEMETRIA", 40f, 683f);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "O2", 40f, 662f);
        fonts.micro.draw(batch, "HP", 40f, 647f);
        fonts.micro.draw(batch, "EN", 40f, 632f);

        label(fonts.micro, UiTheme.TEXT);
        fonts.micro.draw(batch, Math.round(status.getOxygen()) + "%", 202f, 662f,
            44f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getHealth()) + "%", 202f, 647f,
            44f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getEnergy()) + "%", 202f, 632f,
            44f, Align.right, false);

        MissionState state = mission.getState();

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "ETAPA " + state.getLunarStage() + "/5 // "
            + state.getStageTitle(), 308f, 678f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "T+" + formatTime(missionTime), 842f, 678f, 90f, Align.right, false);

        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, state.getStageInstruction(status.getOxygen()),
            308f, 646f, 548f, Align.left, false);
        Texture requestedIcon = getRequestedIcon(state);
        batch.draw(requestedIcon, 914f, 638f, 42f, 42f);

        label(fonts.label, portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "PROGRESSO LUA", 1008f, 678f);
        label(fonts.micro, UiTheme.TEXT);
        fonts.micro.draw(batch, "REPAROS " + state.getRepairCount() + "/4   GELO "
            + (state.isIceProcessed() ? "OK" : "--"), 1008f, 654f);
        fonts.micro.draw(batch, "ARMA " + (state.hasWeapon() ? "ON" : state.getWeaponPartCount() + "/3")
            + "   HOSTIS " + Math.min(state.getEnemiesDefeated(), MissionState.LUNAR_ENEMY_TARGET)
            + "/" + MissionState.LUNAR_ENEMY_TARGET, 1008f, 638f);

        batch.draw(assets.getEvaWeapon(), 1014f, 556f, 68f, 34f);
        label(fonts.micro, weaponReloading ? UiTheme.CYAN : UiTheme.WARNING);
        fonts.micro.draw(batch, weaponReloading ? "RECARREGANDO" : "ARMA EVA", 1090f, 590f);
        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, state.getMagazineAmmo() + " / " + state.getReserveAmmo(),
            1090f, 565f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, weaponReloading
            ? Math.round(weaponReloadProgress * 100f) + "% // AGUARDE"
            : "PENTE   RESERVA   [ R ] RECARREGAR", 1090f, 550f);

        drawArmorStatus(batch, state);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD  SHIFT  MOUSE  ESC", 40f, 41f);

        label(fonts.micro, inventoryOpen ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, inventoryOpen ? "[ E ] FECHAR INVENTARIO"
            : "[ E ] ABRIR INVENTARIO", 1018f, 42f);

        if (nearbyChest != null && !nearbyChest.isOpened()) {
            label(fonts.label, UiTheme.WARNING);
            fonts.label.draw(batch, "[ F ] INSPECIONAR BAU DE SUPRIMENTOS",
                338f, 65f, 640f, Align.center, false);
            label(fonts.micro, UiTheme.MUTED);
            fonts.micro.draw(batch, "CONTEUDO DESCONHECIDO // MATERIAIS DE FABRICACAO",
                338f, 42f, 640f, Align.center, false);
        } else if (nearbyStation != null) {
            boolean repaired = state.isRepaired(nearbyStation.getType());
            boolean repairing = nearbyStation.isRepairing();
            label(fonts.label, repaired ? UiTheme.GREEN
                : repairing ? UiTheme.WARNING : UiTheme.CYAN);
            fonts.label.draw(batch,
                repairing ? "REPARANDO " + nearbyStation.getType().getLabel()
                    + " // " + Math.round(nearbyStation.getRepairProgress() * 100f) + "%"
                    : repaired ? nearbyStation.getType().getLabel() + " // ONLINE"
                    : "[ E ] REPARAR " + nearbyStation.getType().getLabel(),
                338f, 65f, 640f, Align.center, false);
            label(fonts.micro, repaired ? UiTheme.GREEN
                : repairing ? UiTheme.WARNING : UiTheme.MUTED);
            fonts.micro.draw(batch, repairing ? "MANTENHA-SE PROXIMO // 3 SEGUNDOS"
                : repaired ? "SISTEMA OPERACIONAL"
                : "REQUER PECA CORRESPONDENTE", 338f, 42f, 640f, Align.center, false);
        } else if (portalNearby) {
            label(fonts.label, portal.isActive() ? UiTheme.PURPLE : UiTheme.WARNING);
            fonts.label.draw(batch, portal.isActive() ? "PORTAL ONLINE // ENTRE PARA VIAJAR"
                : "PORTAL BLOQUEADO", 338f, 65f, 640f, Align.center, false);
            label(fonts.micro, UiTheme.MUTED);
            fonts.micro.draw(batch, portal.isActive() ? "DESTINO: BASE ARES // MARTE"
                : state.getCurrentObjective(status.getOxygen()), 338f, 42f,
                640f, Align.center, false);
        } else if (insideBase) {
            label(fonts.label, UiTheme.TEXT);
            fonts.label.draw(batch, "[ E ] ENTRAR NA BASE LUNAR",
                338f, 76f, 640f, Align.center, false);
            label(fonts.micro, state.canCraftWeapon() ? UiTheme.GREEN : UiTheme.MUTED);
            fonts.micro.draw(batch, state.canCraftWeapon()
                ? "BANCADA PRONTA // COMPONENTES DA ARMA COMPLETOS"
                : "CRAFTING E PROCESSAMENTO DISPONIVEIS NO INTERIOR",
                338f, 51f, 640f, Align.center, false);
            fonts.micro.draw(batch, "AMBIENTE PRESSURIZADO // SUPORTE DE VIDA", 338f, 34f, 640f, Align.center, false);
        } else if (state.getLastMessage() != null) {
            label(fonts.micro, UiTheme.CYAN_SOFT);
            fonts.micro.draw(batch, state.getLastMessage(), 338f, 48f,
                640f, Align.center, false);
        }

        batch.end();
    }

    private void drawArmorStatus(SpriteBatch batch, MissionState state) {
        label(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ARMADURA", 1154f, 506f);
        ItemType[] armor = {ItemType.ARMOR_HELMET, ItemType.ARMOR_CHEST, ItemType.ARMOR_BOOTS};
        for (int i = 0; i < armor.length; i++) {
            boolean equipped = state.isEquipped(armor[i]);
            Texture icon = getItemIcon(armor[i]);
            if (equipped) batch.setColor(Color.WHITE);
            else batch.setColor(0.30f, 0.38f, 0.42f, 0.45f);
            batch.draw(icon, 1178f, 450f - i * 42f, 42f, 42f);
        }
        batch.setColor(Color.WHITE);
        label(fonts.micro, state.getArmorProtection() > 0f ? UiTheme.GREEN : UiTheme.MUTED);
        fonts.micro.draw(batch, Math.round(state.getArmorProtection() * 100f) + "%", 1154f, 360f,
            92f, Align.center, false);
    }

    private Texture getRequestedIcon(MissionState state) {
        ItemType requested = state.getRequestedItem();
        if (requested == null) {
            if (!state.hasWeapon()) return assets.getEvaWeapon();
            if (state.getEnemiesDefeated() < MissionState.LUNAR_ENEMY_TARGET) {
                return assets.getEnemySentinel();
            }
            return assets.getPortal();
        }
        return switch (requested) {
            case ANTENNA_PART -> assets.getAntennaPart();
            case ENERGY_PART -> assets.getEnergyPart();
            case EXTRACTION_PART -> assets.getExtractionPart();
            case GREENHOUSE_PART -> assets.getGreenhousePart();
            case WEAPON_PART_A -> assets.getWeaponPartA();
            case WEAPON_PART_B -> assets.getWeaponPartB();
            case WEAPON_PART_C -> assets.getWeaponPartC();
            case AMMO_CELL -> assets.getEnergyProjectile();
            case MEDKIT -> assets.getMedkit();
            case OXYGEN -> assets.getOxygen();
            case FOOD -> assets.getFood();
            case ICE_ROCK -> assets.getIceRock();
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

    private void drawMissionDetails(SpriteBatch batch, MissionState state) {
        label(fonts.label, UiTheme.PURPLE);
        fonts.label.draw(batch, "ROTA DA MISSAO", 44f, 584f);

        label(fonts.micro, state.getRepairCount() >= 4 ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "01  SISTEMAS       " + state.getRepairCount() + "/4", 44f, 550f);

        label(fonts.micro, state.hasWeapon() ? UiTheme.GREEN : UiTheme.TEXT);
        String weaponState = state.hasWeapon() ? "ONLINE  //  MUN " + state.getCount(ItemType.AMMO_CELL)
            : "A" + state.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_A)
                + " B" + state.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_B)
                + " C" + state.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_C);
        fonts.micro.draw(batch, "02  ARMA EVA       " + weaponState, 44f, 520f);

        label(fonts.micro, state.getEnemiesDefeated() >= MissionState.LUNAR_ENEMY_TARGET
            ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "03  AMEACA         "
            + Math.min(state.getEnemiesDefeated(), MissionState.LUNAR_ENEMY_TARGET)
            + "/" + MissionState.LUNAR_ENEMY_TARGET, 44f, 490f);

        label(fonts.micro, state.getLunarStage() >= 4 ? UiTheme.PURPLE : UiTheme.MUTED);
        fonts.micro.draw(batch, "04  PORTAL         "
            + (state.getLunarStage() >= 4 ? "LIBERADO" : "BLOQUEADO"), 44f, 460f);
    }

    private void drawInventory(SpriteBatch batch, MissionState state, float drawerX) {
        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "INVENTARIO", drawerX + 20f, 596f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "CARGA EVA // 11 SLOTS", drawerX + 20f, 574f);
        ItemType[] types = ItemType.values();
        for (int i = 0; i < types.length; i++) {
            int column = i % 2;
            int row = i / 2;
            float x = drawerX + 18f + column * 122f;
            float y = 547f - row * 38f;
            Texture icon = getItemIcon(types[i]);
            batch.draw(icon, x, y - 15f, 22f, 22f);
            label(fonts.micro, state.getCount(types[i]) > 0 ? UiTheme.TEXT : UiTheme.MUTED);
            fonts.micro.draw(batch, compactItemLabel(types[i]), x + 28f, y,
                48f, Align.left, false);
            fonts.micro.draw(batch, "x" + state.getCount(types[i]), x + 76f, y,
                28f, Align.right, false);
        }
    }

    private String compactItemLabel(ItemType type) {
        return switch (type) {
            case OXYGEN -> "O2";
            case FOOD -> "ALIM";
            case ICE_ROCK -> "GELO";
            case MEDKIT -> "MED";
            case ANTENNA_PART -> "ANT";
            case ENERGY_PART -> "ENER";
            case EXTRACTION_PART -> "EXT";
            case GREENHOUSE_PART -> "EST";
            case WEAPON_PART_A -> "ARMA A";
            case WEAPON_PART_B -> "ARMA B";
            case WEAPON_PART_C -> "ARMA C";
            case AMMO_CELL -> "MUN";
            case ALLOY_PLATE -> "LIGA";
            case QUANTUM_CORE -> "NUCLEO";
            case FIBER_MESH -> "FIBRA";
            case MINING_TOOL -> "PICA";
            case REPAIR_TOOL -> "REPAR";
            case ARMOR_HELMET -> "CAP";
            case ARMOR_CHEST -> "PEIT";
            case ARMOR_BOOTS -> "BOTAS";
            case METHANE_SAMPLE -> "CH4";
            case TITAN_CORE -> "NUCLEO";
        };
    }

    private Texture getItemIcon(ItemType type) {
        return switch (type) {
            case ANTENNA_PART -> assets.getAntennaPart();
            case ENERGY_PART -> assets.getEnergyPart();
            case EXTRACTION_PART -> assets.getExtractionPart();
            case GREENHOUSE_PART -> assets.getGreenhousePart();
            case WEAPON_PART_A -> assets.getWeaponPartA();
            case WEAPON_PART_B -> assets.getWeaponPartB();
            case WEAPON_PART_C -> assets.getWeaponPartC();
            case AMMO_CELL -> assets.getEnergyProjectile();
            case MEDKIT -> assets.getMedkit();
            case OXYGEN -> assets.getOxygen();
            case FOOD -> assets.getFood();
            case ICE_ROCK -> assets.getIceRock();
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

    private void drawResource(SpriteBatch batch, String name, String value, float x) {
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, name, x, 680f);
        label(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, value, x, 654f);
    }

    private void label(BitmapFont font, Color color) {
        font.setColor(color);
    }

    public void renderPauseText(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        label(fonts.micro, UiTheme.CYAN);
        fonts.micro.draw(batch, "SISTEMA EVA // ESTADO SEGURO", 0f, 455f,
            WIDTH, Align.center, false);
        label(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, "MISSAO PAUSADA", 0f, 410f,
            WIDTH, Align.center, false);
        label(fonts.body, UiTheme.MUTED);
        fonts.body.draw(batch, "A telemetria foi temporariamente suspensa.", 0f, 365f,
            WIDTH, Align.center, false);
        drawButtonLabel(batch, "CONTINUAR", overlayPrimary, UiTheme.CYAN);
        drawButtonLabel(batch, "VOLTAR AO MENU", overlaySecondary, UiTheme.CYAN_SOFT);
        batch.end();
    }

    public void renderDamageIndicator(float remaining) {
        if (remaining <= 0f) return;
        float strength = MathUtils.clamp(remaining / 0.46f, 0f, 1f);
        enableBlend();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.82f, 0.02f, 0.02f, 0.12f * strength);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        shapes.setColor(0.94f, 0.03f, 0.02f, 0.36f * strength);
        shapes.rect(0f, 0f, WIDTH, 12f);
        shapes.rect(0f, HEIGHT - 12f, WIDTH, 12f);
        shapes.rect(0f, 12f, 12f, HEIGHT - 24f);
        shapes.rect(WIDTH - 12f, 12f, 12f, HEIGHT - 24f);
        shapes.end();
        disableBlend();
    }

    public void renderGameOverText(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        label(fonts.micro, UiTheme.DANGER);
        fonts.micro.draw(batch, "ALERTA CRITICO // SUPORTE DE VIDA", 0f, 455f,
            WIDTH, Align.center, false);
        label(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, "MISSAO ENCERRADA", 0f, 410f,
            WIDTH, Align.center, false);
        label(fonts.body, UiTheme.MUTED);
        fonts.body.draw(batch, "Reserva de oxigenio esgotada.", 0f, 365f,
            WIDTH, Align.center, false);
        drawButtonLabel(batch, "TENTAR NOVAMENTE", overlayPrimary, UiTheme.DANGER);
        drawButtonLabel(batch, "MENU PRINCIPAL", overlaySecondary, UiTheme.CYAN_SOFT);
        batch.end();
    }

    private void drawOverlayButton(Rectangle button, Color accent, boolean hovered) {
        shapes.setColor(hovered ? 0.035f : 0.014f,
            hovered ? 0.085f : 0.035f, hovered ? 0.11f : 0.05f, 0.98f);
        shapes.rect(button.x, button.y, button.width, button.height);
        shapes.setColor(accent.r, accent.g, accent.b, hovered ? 1f : 0.62f);
        shapes.rect(button.x, button.y, 3f, button.height);
        shapes.rect(button.x, button.y + button.height - 2f, button.width, 2f);
    }

    private void drawButtonLabel(SpriteBatch batch, String text, Rectangle button, Color accent) {
        label(fonts.label, button.contains(pointer) ? accent : UiTheme.TEXT);
        fonts.label.draw(batch, text, button.x, button.y + 34f,
            button.width, Align.center, false);
    }

    public int pollOverlayAction() {
        updatePointer();
        if (!Gdx.input.justTouched()) return 0;
        if (overlayPrimary.contains(pointer)) return 1;
        if (overlaySecondary.contains(pointer)) return 2;
        return 0;
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void disableBlend() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        inventoryOverlay.resize(width, height);
    }

    public boolean isInventoryOpen() { return inventoryOverlay.isOpen(); }
    public void closeInventory() {
        inventoryOverlay.close();
        inventoryOpen = false;
    }

    public void dispose() {
        shapes.dispose();
        fonts.close();
        inventoryOverlay.dispose();
    }
}
