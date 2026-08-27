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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.entities.RepairStation;
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

    private float animationTime;
    private boolean inventoryOpen;

    public ModernHud(GameAssets assets) {
        this.assets = assets;
        camera = new OrthographicCamera();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f);
        camera.update();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
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
        boolean portalNearby
    ) {
        animationTime += Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventoryOpen = !inventoryOpen;
        }
        enableBlend();
        drawInterfaceShapes(status, baseEntranceNearby, mission, portal, paused,
            missionFailed, nearbyStation, portalNearby);
        drawInterfaceText(batch, status, player, baseEntranceNearby, mission, portal,
            missionTime, nearbyStation, portalNearby);
        disableBlend();
    }

    private void drawInterfaceShapes(
        PlayerStatus status,
        boolean insideBase,
        MissionSystem mission,
        Portal portal,
        boolean paused,
        boolean missionFailed,
        RepairStation nearbyStation,
        boolean portalNearby
    ) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        UiTheme.panel(shapes, 24f, 634f, 250f, 62f,
            insideBase ? UiTheme.GREEN : UiTheme.CYAN);
        UiTheme.panel(shapes, 294f, 634f, 682f, 62f, UiTheme.CYAN);
        UiTheme.panel(shapes, 996f, 634f, 260f, 62f,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);

        UiTheme.bar(shapes, 75f, 674f, 128f, 6f,
            status.getOxygen() / GameConstants.MAX_OXYGEN,
            oxygenColor(status.getOxygen()));
        UiTheme.bar(shapes, 75f, 657f, 128f, 5f,
            status.getHealth() / GameConstants.MAX_HEALTH,
            status.getHealth() < 30f ? UiTheme.DANGER : UiTheme.GREEN);
        UiTheme.bar(shapes, 75f, 640f, 128f, 5f,
            status.getEnergy() / GameConstants.MAX_ENERGY,
            UiTheme.GREEN);

        MissionState state = mission.getState();
        float missionProgress = (MathUtils.clamp(state.getRepairCount() / 4f, 0f, 1f)
            + (state.hasWeapon() ? 1f : 0f)
            + MathUtils.clamp(state.getEnemiesDefeated()
                / (float) MissionState.LUNAR_ENEMY_TARGET, 0f, 1f)) / 3f;
        UiTheme.bar(shapes, 1016f, 640f, 220f, 5f, missionProgress,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN);

        shapes.setColor(0.004f, 0.012f, 0.018f, 0.82f);
        shapes.rect(24f, 20f, 270f, 32f);
        shapes.setColor(UiTheme.CYAN_SOFT);
        shapes.rect(24f, 51f, 270f, 1f);

        UiTheme.panel(shapes, 1020f, 20f, 236f, 32f,
            inventoryOpen ? UiTheme.GREEN : UiTheme.CYAN_SOFT);

        if (inventoryOpen) {
            UiTheme.panel(shapes, 980f, 324f, 276f, 292f, UiTheme.CYAN_SOFT);
            for (int i = 0; i < ItemType.values().length; i++) {
                int column = i % 2;
                int row = i / 2;
                float cellX = 996f + column * 122f;
                float cellY = 546f - row * 38f;
                shapes.setColor(0.018f, 0.035f, 0.045f, 0.94f);
                shapes.rect(cellX, cellY, 114f, 32f);
                shapes.setColor(UiTheme.BORDER);
                shapes.rect(cellX, cellY, 114f, 1f);
            }
        }

        if (nearbyStation != null) {
            UiTheme.panel(shapes, 316f, 20f, 684f, 66f,
                state.isRepaired(nearbyStation.getType()) ? UiTheme.GREEN : UiTheme.CYAN);
        } else if (portalNearby) {
            UiTheme.panel(shapes, 316f, 20f, 684f, 66f,
                portal.isActive() ? UiTheme.PURPLE : UiTheme.WARNING);
        } else if (insideBase) {
            UiTheme.panel(shapes, 316f, 20f, 684f, 76f, UiTheme.GREEN);
        } else if (mission.getState().getLastMessage() != null) {
            shapes.setColor(0.004f, 0.014f, 0.022f, 0.94f);
            shapes.rect(316f, 20f, 684f, 44f);
            shapes.setColor(UiTheme.CYAN_SOFT);
            shapes.rect(316f, 20f, 3f, 44f);
            shapes.setColor(UiTheme.BORDER);
            shapes.rect(316f, 63f, 684f, 1f);
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
        boolean portalNearby
    ) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        label(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "EVA-01", 42f, 686f);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "O2", 42f, 676f);
        fonts.micro.draw(batch, "HP", 42f, 659f);
        fonts.micro.draw(batch, "EN", 42f, 642f);

        label(fonts.micro, UiTheme.TEXT);
        fonts.micro.draw(batch, Math.round(status.getOxygen()) + "%", 211f, 676f,
            44f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getHealth()) + "%", 211f, 659f,
            44f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getEnergy()) + "%", 211f, 642f,
            44f, Align.right, false);

        MissionState state = mission.getState();

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "ETAPA " + state.getLunarStage() + "/4 // "
            + state.getStageTitle(), 314f, 681f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, formatTime(missionTime), 856f, 681f, 58f, Align.right, false);

        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, state.getStageInstruction(status.getOxygen()),
            314f, 651f, 548f, Align.left, false);
        Texture requestedIcon = getRequestedIcon(state);
        batch.draw(requestedIcon, 924f, 644f, 38f, 38f);

        label(fonts.label, portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "PROGRESSO LUA", 1016f, 681f);
        label(fonts.micro, UiTheme.TEXT);
        fonts.micro.draw(batch, "R " + state.getRepairCount() + "/4   ARMA "
            + (state.hasWeapon() ? "ON" : state.getWeaponPartCount() + "/3")
            + "   H " + Math.min(state.getEnemiesDefeated(), MissionState.LUNAR_ENEMY_TARGET)
            + "/" + MissionState.LUNAR_ENEMY_TARGET, 1016f, 657f);

        if (inventoryOpen) drawInventory(batch, state);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD  SHIFT  MOUSE  ESC", 42f, 41f);

        label(fonts.micro, inventoryOpen ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, inventoryOpen ? "[ I ] FECHAR INVENTARIO"
            : "[ I ] ABRIR INVENTARIO", 1034f, 41f);

        if (nearbyStation != null) {
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
            case MEDKIT -> assets.getMedkit();
            case OXYGEN -> assets.getOxygen();
            case FOOD -> assets.getFood();
            case ICE_ROCK -> assets.getIceRock();
        };
    }

    private void drawMissionDetails(SpriteBatch batch, MissionState state) {
        label(fonts.label, UiTheme.PURPLE);
        fonts.label.draw(batch, "ROTA DA MISSAO", 44f, 584f);

        label(fonts.micro, state.getRepairCount() >= 4 ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "01  SISTEMAS       " + state.getRepairCount() + "/4", 44f, 550f);

        label(fonts.micro, state.hasWeapon() ? UiTheme.GREEN : UiTheme.TEXT);
        String weaponState = state.hasWeapon() ? "ONLINE"
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

    private void drawInventory(SpriteBatch batch, MissionState state) {
        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "INVENTARIO", 1000f, 594f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "CARGA EVA // 11 SLOTS", 1000f, 574f);
        ItemType[] types = ItemType.values();
        for (int i = 0; i < types.length; i++) {
            int column = i % 2;
            int row = i / 2;
            float x = 1002f + column * 122f;
            float y = 567f - row * 38f;
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
            case MEDKIT -> assets.getMedkit();
            case OXYGEN -> assets.getOxygen();
            case FOOD -> assets.getFood();
            case ICE_ROCK -> assets.getIceRock();
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
        drawKeyLine(batch, "ESC", "RETOMAR MISSAO", 320f);
        drawKeyLine(batch, "M", "ABORTAR E VOLTAR AO MENU", 282f);
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
        drawKeyLine(batch, "R", "REINICIAR PROTOCOLO", 320f);
        drawKeyLine(batch, "M", "VOLTAR AO MENU", 282f);
        batch.end();
    }

    private void drawKeyLine(SpriteBatch batch, String key, String action, float y) {
        label(fonts.label, UiTheme.CYAN);
        fonts.label.draw(batch, "[ " + key + " ]", 470f, y);
        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, action, 555f, y);
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
    }

    public void dispose() {
        shapes.dispose();
        fonts.close();
    }
}
