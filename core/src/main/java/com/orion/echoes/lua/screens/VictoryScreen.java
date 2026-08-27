package com.orion.echoes.lua.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.LunarEchoesGame;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;

public class VictoryScreen extends ScreenAdapter {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;

    private final LunarEchoesGame game;
    private final SpriteBatch batch;
    private final float missionTime;
    private final int water;
    private final int fuel;
    private final int collectedItems;
    private final float oxygen;
    private final int score;
    private final int bestScore;
    private final boolean newRecord;

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private UiFonts fonts;
    private Texture portalTexture;
    private float animationTime;
    private float resultReveal;
    private int displayedScore;
    private boolean changingScreen;

    public VictoryScreen(
        LunarEchoesGame game,
        float missionTime,
        int water,
        int fuel,
        int collectedItems,
        float oxygen,
        int score,
        int bestScore,
        boolean newRecord
    ) {
        this.game = game;
        batch = game.getBatch();
        this.missionTime = missionTime;
        this.water = water;
        this.fuel = fuel;
        this.collectedItems = collectedItems;
        this.oxygen = oxygen;
        this.score = score;
        this.bestScore = bestScore;
        this.newRecord = newRecord;
    }

    @Override
    public void show() {
        changingScreen = false;
        animationTime = 0f;
        camera = new OrthographicCamera();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f);
        camera.update();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        portalTexture = game.getAssets().getPortal();
        game.getAudio().playVictory();
    }

    @Override
    public void render(float delta) {
        if (changingScreen) return;
        animationTime += delta;
        float safeDelta = Math.min(delta, 1f / 20f);
        resultReveal = MathUtils.lerp(resultReveal, 1f,
            1f - (float) Math.pow(0.00008f, safeDelta));
        displayedScore = Math.round(score * resultReveal);
        handleInput();
        if (changingScreen) return;
        clear();
        drawBackground();
        drawPortal();
        drawResultsPanel();
        drawResultsText();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            changingScreen = true;
            game.getAudio().playMenuClick();
            game.changeScreen(new LunarScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            game.getAudio().playMenuClick();
            game.changeScreen(new MenuScreen(game));
        }
    }

    private void clear() {
        Gdx.gl.glClearColor(UiTheme.SPACE.r, UiTheme.SPACE.g, UiTheme.SPACE.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawBackground() {
        enableBlend();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiTheme.SPACE);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);

        for (int i = 0; i < 72; i++) {
            float x = (i * 173f + 71f) % WIDTH;
            float y = (i * 97f + 43f) % HEIGHT;
            float pulse = 0.18f + MathUtils.sin(animationTime * 1.3f + i) * 0.08f;
            shapes.setColor(0.35f, 0.66f, 0.88f, pulse);
            shapes.circle(x, y, i % 5 == 0 ? 1.5f : 0.8f, 8);
        }

        shapes.setColor(0.04f, 0.16f, 0.24f, 0.34f);
        shapes.circle(1010f, 362f, 285f, 72);
        shapes.setColor(0.008f, 0.032f, 0.058f, 0.92f);
        shapes.circle(1010f, 362f, 228f, 72);
        shapes.end();
        disableBlend();
    }

    private void drawPortal() {
        float pulse = 0.94f + MathUtils.sin(animationTime * 2.4f) * 0.04f;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, pulse);
        batch.draw(portalTexture, 808f, 75f, 382f, 520f);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawResultsPanel() {
        float yOff = -24f * (1f - resultReveal);
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        UiTheme.panel(shapes, 54f, 52f + yOff, 650f, 616f,
            newRecord ? UiTheme.GREEN : UiTheme.CYAN);

        shapes.setColor(UiTheme.PANEL_LIGHT);
        shapes.rect(86f, 273f + yOff, 174f, 86f * resultReveal);
        shapes.rect(276f, 273f + yOff, 174f, 86f * resultReveal);
        shapes.rect(466f, 273f + yOff, 206f, 86f * resultReveal);
        shapes.setColor(newRecord ? UiTheme.GREEN : UiTheme.CYAN);
        shapes.rect(86f, 273f + yOff, 174f * resultReveal, 3f);
        shapes.rect(276f, 273f + yOff, 174f * resultReveal, 3f);
        shapes.rect(466f, 273f + yOff, 206f * resultReveal, 3f);

        shapes.setColor(UiTheme.BORDER);
        shapes.rect(86f, 392f + yOff, 586f * resultReveal, 1f);

        shapes.setColor(UiTheme.PANEL_LIGHT);
        shapes.rect(86f, 105f + yOff, 276f, 58f);
        shapes.rect(378f, 105f + yOff, 294f, 58f);
        shapes.setColor(UiTheme.CYAN);
        shapes.rect(86f, 105f + yOff, 4f, 58f);
        shapes.setColor(UiTheme.BORDER);
        shapes.rect(378f, 105f + yOff, 4f, 58f);
        shapes.end();
        disableBlend();
    }

    private void drawResultsText() {
        float yOff = -24f * (1f - resultReveal);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        set(fonts.micro, newRecord ? UiTheme.GREEN : UiTheme.CYAN);
        fonts.micro.draw(batch,
            newRecord ? "NOVO RECORDE // INTEGRACAO CONFIRMADA" : "INTEGRACAO CONFIRMADA // SINAL ESTAVEL",
            86f, 628f + yOff);
        set(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, "MISSAO CONCLUIDA", 86f, 583f + yOff);
        set(fonts.body, UiTheme.MUTED);
        fonts.body.draw(batch, "Lua restaurada. Base Ares sincronizada. Rota interplanetaria estavel.",
            86f, 546f + yOff, 570f, Align.left, true);

        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "PONTUACAO DA MISSAO", 86f, 482f + yOff);
        set(fonts.display, newRecord ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.display.draw(batch, String.valueOf(displayedScore), 82f, 438f + yOff);
        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "MELHOR REGISTRO  " + bestScore, 430f, 438f + yOff,
            242f, Align.right, false);

        stat("TEMPO", formatTime(missionTime), 104f, yOff);
        stat("ITENS", String.valueOf(collectedItems), 294f, yOff);
        stat("O2 RESIDUAL", Math.round(oxygen) + "%", 484f, yOff);

        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "RECURSOS PRESERVADOS NA TRANSICAO", 86f, 235f + yOff);
        set(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch, "AGUA  " + water + "     H2  " + fuel, 86f, 202f + yOff);

        set(fonts.label, UiTheme.CYAN);
        fonts.label.draw(batch, "[ R ]", 108f, 141f + yOff);
        set(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, "NOVA MISSAO", 170f, 141f + yOff);
        set(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "[ M ]", 400f, 141f + yOff);
        set(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, "MENU PRINCIPAL", 462f, 141f + yOff);

        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ROTA LUA > MARTE // ESTAVEL", 850f, 640f);
        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "ORION DEEP SPACE PROGRAM", 878f, 46f);
        batch.end();
    }

    private void stat(String label, String value, float x, float yOff) {
        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, label, x, 337f + yOff);
        set(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, value, x, 305f + yOff);
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    private void set(BitmapFont font, Color color) {
        font.setColor(color);
    }

    private void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void disableBlend() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (fonts != null) fonts.close();
    }
}
