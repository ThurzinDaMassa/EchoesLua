package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.orion.echoes.lua.assets.GameAssets;

/** Abertura cinematografica curta exibida antes da telemetria da missao. */
public class MissionProtocolOverlay {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;
    private static final float DURATION = 4.8f;

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final UiFonts fonts = new UiFonts();
    private final Texture image;
    private float elapsed;
    private boolean active = true;

    public MissionProtocolOverlay(GameAssets assets) {
        camera.setToOrtho(false, WIDTH, HEIGHT);
        image = assets.getMissionProtocolIntro();
    }

    public boolean update(float delta) {
        if (!active) return false;
        elapsed += Math.min(delta, 1f / 20f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || elapsed >= DURATION) {
            active = false;
        }
        return active;
    }

    public void render(SpriteBatch batch) {
        float progress = MathUtils.clamp(elapsed / DURATION, 0f, 1f);
        float fadeIn = MathUtils.clamp(elapsed / 0.65f, 0f, 1f);
        float fadeOut = MathUtils.clamp((DURATION - elapsed) / 0.65f, 0f, 1f);
        float alpha = Math.min(fadeIn, fadeOut);
        float zoom = Interpolation.smooth.apply(1.045f, 1f, progress);
        float imageWidth = WIDTH * zoom;
        float imageHeight = HEIGHT * zoom;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(image, (WIDTH - imageWidth) / 2f, (HEIGHT - imageHeight) / 2f,
            imageWidth, imageHeight);
        batch.setColor(Color.WHITE);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.008f, 0.016f, 0.40f * alpha);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        shapes.setColor(0.002f, 0.010f, 0.018f, 0.88f * alpha);
        shapes.rect(0f, 0f, WIDTH, 170f);
        shapes.rect(0f, 626f, WIDTH, 94f);
        shapes.setColor(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, alpha);
        shapes.rect(72f, 138f, (WIDTH - 144f) * progress, 3f);
        float scanX = 72f + (WIDTH - 144f) * progress;
        shapes.setColor(0.25f, 0.92f, 1f, 0.18f * alpha);
        shapes.rect(scanX - 2f, 170f, 4f, 456f);
        shapes.end();

        batch.begin();
        fonts.micro.setColor(0.36f, 0.92f, 1f, alpha);
        fonts.micro.draw(batch, "ORION DEEP SPACE // TRANSMISSAO SEGURA", 72f, 674f);
        fonts.heading.setColor(1f, 1f, 1f, alpha);
        fonts.heading.draw(batch, "PROTOCOLO ECHO LUNAR", 72f, 116f);
        fonts.body.setColor(0.86f, 0.93f, 0.98f, alpha);
        fonts.body.draw(batch,
            "Restaure os quatro sistemas da colonia e restabeleca a rota para Marte.",
            72f, 78f, 900f, Align.left, false);
        fonts.micro.setColor(0.60f, 0.72f, 0.80f, alpha);
        fonts.micro.draw(batch, "[ ESPACO ] PULAR", 1040f, 38f, 168f, Align.right, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public boolean isActive() { return active; }

    public void resize(int width, int height) {
        camera.setToOrtho(false, WIDTH, HEIGHT);
    }

    public void dispose() {
        shapes.dispose();
        fonts.close();
    }
}
