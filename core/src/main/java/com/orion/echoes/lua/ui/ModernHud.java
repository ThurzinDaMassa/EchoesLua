package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
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

    private float animationTime;

    public ModernHud() {
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
        boolean missionFailed
    ) {
        animationTime += Gdx.graphics.getDeltaTime();
        boolean insideBase = base.isPlayerInside(player);

        enableBlend();
        drawInterfaceShapes(status, insideBase, processor, mission, portal, paused, missionFailed);
        drawInterfaceText(batch, status, player, insideBase, processor, mission, portal, missionTime);
        disableBlend();
    }

    private void drawInterfaceShapes(
        PlayerStatus status,
        boolean insideBase,
        IceProcessor processor,
        MissionSystem mission,
        Portal portal,
        boolean paused,
        boolean missionFailed
    ) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        UiTheme.panel(shapes, 24f, 576f, 342f, 120f,
            insideBase ? UiTheme.GREEN : UiTheme.CYAN);
        UiTheme.panel(shapes, 390f, 625f, 500f, 71f, UiTheme.CYAN);
        UiTheme.panel(shapes, 914f, 576f, 342f, 120f,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN_SOFT);

        UiTheme.bar(shapes, 112f, 638f, 220f, 9f,
            status.getOxygen() / GameConstants.MAX_OXYGEN,
            oxygenColor(status.getOxygen()));
        UiTheme.bar(shapes, 112f, 606f, 220f, 7f,
            status.getEnergy() / GameConstants.MAX_ENERGY,
            UiTheme.GREEN);

        drawResourceDivider(556f);
        drawResourceDivider(722f);

        float waterProgress = (float) status.getWater() / mission.getRequiredWater();
        float fuelProgress = (float) status.getFuel() / mission.getRequiredFuel();
        float missionProgress = (MathUtils.clamp(waterProgress, 0f, 1f)
            + MathUtils.clamp(fuelProgress, 0f, 1f)) * 0.5f;
        UiTheme.bar(shapes, 940f, 597f, 290f, 6f, missionProgress,
            portal.isActive() ? UiTheme.PURPLE : UiTheme.CYAN);

        shapes.setColor(0.004f, 0.012f, 0.018f, 0.82f);
        shapes.rect(24f, 20f, 360f, 31f);
        shapes.setColor(UiTheme.CYAN_SOFT);
        shapes.rect(24f, 50f, 360f, 1f);

        if (insideBase) {
            UiTheme.panel(shapes, 404f, 22f, 472f, 62f, UiTheme.GREEN);
            shapes.setColor(UiTheme.GREEN);
            shapes.rect(422f, 38f, 32f, 32f);
        } else if (processor.getLastMessage() != null) {
            shapes.setColor(0.004f, 0.014f, 0.022f, 0.94f);
            shapes.rect(390f, 20f, 500f, 44f);
            shapes.setColor(UiTheme.CYAN_SOFT);
            shapes.rect(390f, 20f, 3f, 44f);
            shapes.setColor(UiTheme.BORDER);
            shapes.rect(390f, 63f, 500f, 1f);
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
        float missionTime
    ) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "EVA // 01", 44f, 678f);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "OXIGENIO", 44f, 649f);
        fonts.micro.draw(batch, "ENERGIA", 44f, 617f);

        label(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, Math.round(status.getOxygen()) + "%", 290f, 651f,
            44f, Align.right, false);
        fonts.label.draw(batch, Math.round(status.getEnergy()) + "%", 290f, 619f,
            44f, Align.right, false);

        label(fonts.micro, insideBase ? UiTheme.GREEN : UiTheme.MUTED);
        String state = insideBase ? "AMBIENTE PRESSURIZADO"
            : player.isSprinting() ? "PROPULSAO EVA ATIVA" : "SUPERFICIE LUNAR";
        fonts.micro.draw(batch, state, 44f, 591f);

        drawResource(batch, "GELO", status.getIce(), 414f);
        drawResource(batch, "AGUA", status.getWater(), 580f);
        drawResource(batch, "H2", status.getFuel(), 746f);

        label(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "OBJETIVO DE EXTRACAO", 938f, 678f);
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, formatTime(missionTime), 1164f, 678f, 66f, Align.right, false);

        label(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch,
            "AGUA " + status.getWater() + "/" + mission.getRequiredWater()
                + "   H2 " + status.getFuel() + "/" + mission.getRequiredFuel(),
            938f, 646f);

        label(fonts.micro, portal.isActive() ? UiTheme.PURPLE : UiTheme.MUTED);
        fonts.micro.draw(batch,
            portal.isActive() ? "PORTAL ONLINE // DIRIJA-SE A EXTRACAO" : "PORTAL BLOQUEADO",
            938f, 618f);

        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD  MOVER", 42f, 41f);
        fonts.micro.draw(batch, "SHIFT  CORRER", 142f, 41f);
        fonts.micro.draw(batch, "ESC  PAUSA", 276f, 41f);

        if (insideBase) {
            label(fonts.label, UiTheme.PANEL_SOLID);
            fonts.label.draw(batch, "E", 422f, 61f, 32f, Align.center, false);
            label(fonts.label, UiTheme.TEXT);
            fonts.label.draw(batch,
                status.getIce() > 0 ? "PROCESSAR GELO" : "SEM GELO NO INVENTARIO",
                474f, 62f);
            label(fonts.micro, UiTheme.MUTED);
            fonts.micro.draw(batch, "CONVERSAO: GELO > AGUA + H2 + O2", 474f, 42f);
        } else if (processor.getLastMessage() != null) {
            label(fonts.micro, UiTheme.CYAN_SOFT);
            fonts.micro.draw(batch, processor.getLastMessage(), 420f, 48f,
                440f, Align.center, false);
        }

        batch.end();
    }

    private void drawResource(SpriteBatch batch, String name, int value, float x) {
        label(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, name, x, 680f);
        label(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, String.valueOf(value), x, 654f);
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
