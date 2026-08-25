package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
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

import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.entities.RepairStation;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.IceProcessor;
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
        LunarBase base,
        IceProcessor processor,
        MissionSystem mission,
        Portal portal,
        float missionTime,
        boolean paused,
        boolean missionFailed,
        RepairStation nearbyStation,
        boolean portalNearby
    ) {
        animationTime += Gdx.graphics.getDeltaTime();
        boolean insideBase = base.isPlayerInside(player);

        enableBlend();
        drawInterfaceShapes(status, insideBase, processor, mission, portal, paused,
            missionFailed, nearbyStation, portalNearby);
        drawInterfaceText(batch, status, player, insideBase, processor, mission, portal,
            missionTime, nearbyStation, portalNearby);
        disableBlend();
    }

    private void drawInterfaceShapes(
        PlayerStatus status,
        boolean insideBase,
        IceProcessor processor,
        MissionSystem mission,
        Portal portal,
        boolean paused,
        boolean missionFailed,
        RepairStation nearbyStation,
        boolean portalNearby
    ) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        UiTheme.panel(shapes, 24f, 610f, 300f, 86f,
            insideBase ? UiTheme.GREEN : UiTheme.CYAN);
        UiTheme.panel(shapes, 344f, 630f, 592f, 66f, UiTheme.CYAN);
        UiTheme.panel(shapes, 956f, 630f, 300f, 66f,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);
        UiTheme.panel(shapes, 24f, 426f, 260f, 184f, UiTheme.PURPLE);

        UiTheme.bar(shapes, 92f, 670f, 154f, 7f,
            status.getOxygen() / GameConstants.MAX_OXYGEN,
            oxygenColor(status.getOxygen()));
        UiTheme.bar(shapes, 92f, 651f, 154f, 6f,
            status.getHealth() / GameConstants.MAX_HEALTH,
            status.getHealth() < 30f ? UiTheme.DANGER : UiTheme.GREEN);
        UiTheme.bar(shapes, 92f, 632f, 154f, 6f,
            status.getEnergy() / GameConstants.MAX_ENERGY,
            UiTheme.GREEN);

        MissionState state = mission.getState();
        float missionProgress = (MathUtils.clamp(state.getRepairCount() / 4f, 0f, 1f)
            + (state.hasWeapon() ? 1f : 0f)
            + MathUtils.clamp(state.getEnemiesDefeated(), 0, 1)) / 3f;
        UiTheme.bar(shapes, 980f, 636f, 250f, 5f, missionProgress,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN);

        shapes.setColor(0.004f, 0.012f, 0.018f, 0.82f);
        shapes.rect(24f, 20f, 300f, 32f);
        shapes.setColor(UiTheme.CYAN_SOFT);
        shapes.rect(24f, 51f, 300f, 1f);

        if (nearbyStation != null) {
            UiTheme.panel(shapes, 344f, 20f, 592f, 66f,
                state.isRepaired(nearbyStation.getType()) ? UiTheme.GREEN : UiTheme.CYAN);
        } else if (portalNearby) {
            UiTheme.panel(shapes, 344f, 20f, 592f, 66f,
                portal.isActive() ? UiTheme.PURPLE : UiTheme.WARNING);
        } else if (insideBase) {
            UiTheme.panel(shapes, 344f, 20f, 592f, 76f, UiTheme.GREEN);
        } else if (mission.getState().getLastMessage() != null) {
            shapes.setColor(0.004f, 0.014f, 0.022f, 0.94f);
            shapes.rect(344f, 20f, 592f, 44f);
            shapes.setColor(UiTheme.CYAN_SOFT);
            shapes.rect(344f, 20f, 3f, 44f);
            shapes.setColor(UiTheme.BORDER);
            shapes.rect(344f, 63f, 592f, 1f);
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
        IceProcessor processor,
        MissionSystem mission,
        Portal portal,
        float missionTime,
        RepairStation nearbyStation,
        boolean portalNearby
    ) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "EVA-01", 44f, 682f);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "O2", 44f, 674f);
        fonts.micro.draw(batch, "HP", 44f, 655f);
        fonts.micro.draw(batch, "EN", 44f, 636f);

        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, Math.round(status.getOxygen()) + "%", 252f, 680f,
            40f, Align.right, false);
        fonts.label.draw(batch, Math.round(status.getHealth()) + "%", 252f, 661f,
            40f, Align.right, false);
        fonts.label.draw(batch, Math.round(status.getEnergy()) + "%", 252f, 642f,
            40f, Align.right, false);

        label(fonts.micro, insideBase ? UiTheme.GREEN : UiTheme.MUTED);
        String environmentState = insideBase ? "AMBIENTE PRESSURIZADO"
            : player.isSprinting() ? "PROPULSAO EVA ATIVA" : "SUPERFICIE LUNAR";
        fonts.micro.draw(batch, environmentState, 44f, 618f);

        MissionState state = mission.getState();

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "ETAPA " + state.getLunarStage() + "/4 // "
            + state.getStageTitle(), 364f, 680f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, formatTime(missionTime), 790f, 680f, 62f, Align.right, false);

        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, state.getStageInstruction(status.getOxygen()),
            364f, 650f, 490f, Align.left, false);
        Texture requestedIcon = getRequestedIcon(state);
        batch.draw(requestedIcon, 876f, 642f, 42f, 42f);

        label(fonts.label, portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "PROGRESSO LUA", 976f, 680f);
        label(fonts.micro, UiTheme.TEXT);
        fonts.micro.draw(batch, "R " + state.getRepairCount() + "/4   ARMA "
            + (state.hasWeapon() ? "ON" : state.getWeaponPartCount() + "/3")
            + "   H " + Math.min(state.getEnemiesDefeated(), 1) + "/1", 976f, 655f);

        drawMissionDetails(batch, state);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD  SHIFT  MOUSE+CLIQUE  ESC", 42f, 41f);

        if (nearbyStation != null) {
            boolean repaired = state.isRepaired(nearbyStation.getType());
            label(fonts.label, repaired ? UiTheme.GREEN : UiTheme.CYAN);
            fonts.label.draw(batch,
                repaired ? nearbyStation.getType().getLabel() + " // ONLINE"
                    : "[ E ] REPARAR " + nearbyStation.getType().getLabel(),
                366f, 65f, 548f, Align.center, false);
            label(fonts.micro, repaired ? UiTheme.GREEN : UiTheme.MUTED);
            fonts.micro.draw(batch, repaired ? "SISTEMA OPERACIONAL"
                : "REQUER PECA CORRESPONDENTE", 366f, 42f, 548f, Align.center, false);
        } else if (portalNearby) {
            label(fonts.label, portal.isActive() ? UiTheme.PURPLE : UiTheme.WARNING);
            fonts.label.draw(batch, portal.isActive() ? "PORTAL ONLINE // ENTRE PARA VIAJAR"
                : "PORTAL BLOQUEADO", 366f, 65f, 548f, Align.center, false);
            label(fonts.micro, UiTheme.MUTED);
            fonts.micro.draw(batch, portal.isActive() ? "DESTINO: BASE ARES // MARTE"
                : state.getCurrentObjective(status.getOxygen()), 366f, 42f,
                548f, Align.center, false);
        } else if (insideBase) {
            label(fonts.label, UiTheme.TEXT);
            fonts.label.draw(batch,
                status.getIce() > 0 ? "[ E ] PROCESSAR GELO" : "BASE // SUPORTE DE VIDA ONLINE",
                366f, 76f, 548f, Align.center, false);
            label(fonts.micro, UiTheme.MUTED);
            fonts.micro.draw(batch, state.canCraftWeapon()
                ? "[ C ] FABRICAR ARMA // COMPONENTES COMPLETOS"
                : state.hasWeapon() ? "ARMA EVA ONLINE // MOUSE PARA MIRAR E DISPARAR"
                : "[ C ] CRAFT BLOQUEADO // PARTES " + state.getWeaponPartCount() + "/3",
                366f, 51f, 548f, Align.center, false);
            fonts.micro.draw(batch, "GELO > AGUA + H2 + O2", 366f, 34f, 548f, Align.center, false);
        } else if (state.getLastMessage() != null) {
            label(fonts.micro, UiTheme.CYAN_SOFT);
            fonts.micro.draw(batch, state.getLastMessage(), 366f, 48f,
                548f, Align.center, false);
        }

        batch.end();
    }

    private Texture getRequestedIcon(MissionState state) {
        ItemType requested = state.getRequestedItem();
        if (requested == null) {
            if (!state.hasWeapon()) return assets.getEvaWeapon();
            if (state.getEnemiesDefeated() < 1) return assets.getEnemySentinel();
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

        label(fonts.micro, state.getEnemiesDefeated() >= 1 ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, "03  AMEACA         "
            + Math.min(state.getEnemiesDefeated(), 1) + "/1", 44f, 490f);

        label(fonts.micro, state.getLunarStage() >= 4 ? UiTheme.PURPLE : UiTheme.MUTED);
        fonts.micro.draw(batch, "04  PORTAL         "
            + (state.getLunarStage() >= 4 ? "LIBERADO" : "BLOQUEADO"), 44f, 460f);
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
