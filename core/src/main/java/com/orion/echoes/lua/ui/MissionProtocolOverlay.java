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

/** Prologo narrativo em tres atos, exibido somente ao iniciar uma nova missao. */
public class MissionProtocolOverlay {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;
    private static final float SCENE_DURATION = 2.8f;
    private static final float DURATION = SCENE_DURATION * 3f;
    private static final float CROSS_FADE = 0.62f;

    private static final String[] KICKERS = {
        "03:17 UTC // SINAL DE EMERGENCIA RECEBIDO",
        "COLONIA SELENE // QUEDA TOTAL DE SISTEMAS",
        "PROTOCOLO ECHO // OPERADOR EVA-07 AUTORIZADO"
    };
    private static final String[] TITLES = {
        "A LUA PAROU DE RESPONDER",
        "SEM ENERGIA. SEM COMUNICACAO. SEM ROTA.",
        "TRAGA A COLONIA DE VOLTA"
    };
    private static final String[] SUBTITLES = {
        "O ultimo pulso chegou ha onze minutos.",
        "Quatro modulos permanecem avariados na superficie.",
        "Restaure os sistemas. Fabrique a arma. Abra o caminho para Marte."
    };

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final UiFonts fonts = new UiFonts();
    private final Texture[] scenes;
    private float elapsed;
    private boolean active = true;

    public MissionProtocolOverlay(GameAssets assets) {
        camera.setToOrtho(false, WIDTH, HEIGHT);
        scenes = new Texture[] {
            assets.getMissionIntroDistress(),
            assets.getMissionIntroFailure(),
            assets.getMissionIntroDeployment()
        };
    }

    public boolean update(float delta) {
        if (!active) return false;
        elapsed += Math.min(delta, 1f / 20f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || elapsed >= DURATION) active = false;
        return active;
    }

    public void render(SpriteBatch batch) {
        int scene = Math.min(2, (int) (elapsed / SCENE_DURATION));
        float localTime = elapsed - scene * SCENE_DURATION;
        float local = MathUtils.clamp(localTime / SCENE_DURATION, 0f, 1f);
        float globalFade = Math.min(
            MathUtils.clamp(elapsed / 0.55f, 0f, 1f),
            MathUtils.clamp((DURATION - elapsed) / 0.65f, 0f, 1f));
        float transition = scene < 2
            ? MathUtils.clamp((localTime - (SCENE_DURATION - CROSS_FADE)) / CROSS_FADE, 0f, 1f)
            : 0f;
        transition = Interpolation.smooth.apply(transition);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawScene(batch, scenes[scene], scene, local, globalFade * (1f - transition));
        if (scene < 2 && transition > 0f) {
            drawScene(batch, scenes[scene + 1], scene + 1, 0f, globalFade * transition);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        drawCinematicFrame(scene, local, globalFade);
        drawNarration(batch, scene, local, globalFade);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawScene(SpriteBatch batch, Texture texture, int scene, float local, float alpha) {
        float zoom = 1.025f + local * 0.035f;
        float w = WIDTH * zoom;
        float h = HEIGHT * zoom;
        float panX = scene == 0 ? -18f * local : scene == 1 ? 14f * local : -8f * local;
        float panY = scene == 2 ? -8f * local : 5f * local;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, (WIDTH - w) * 0.5f + panX, (HEIGHT - h) * 0.5f + panY, w, h);
    }

    private void drawCinematicFrame(int scene, float local, float alpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.001f, 0.005f, 0.010f, 0.20f * alpha);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        shapes.setColor(0.001f, 0.006f, 0.012f, 0.94f * alpha);
        shapes.rect(0f, 0f, WIDTH, 122f);
        shapes.rect(0f, 635f, WIDTH, 85f);
        shapes.setColor(0.01f, 0.03f, 0.045f, 0.88f * alpha);
        shapes.rect(0f, 122f, 940f, 220f);
        shapes.setColor(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.92f * alpha);
        shapes.rect(62f, 316f, 48f, 3f);
        for (int i = 0; i < 3; i++) {
            float segmentAlpha = i < scene ? 0.42f : i == scene ? 1f : 0.16f;
            shapes.setColor(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b,
                segmentAlpha * alpha);
            float filled = i == scene ? local : i < scene ? 1f : 0f;
            shapes.rect(62f + i * 48f, 104f, 36f * Math.max(0.12f, filled), 3f);
        }
        shapes.end();
    }

    private void drawNarration(SpriteBatch batch, int scene, float local, float alpha) {
        float textIn = Interpolation.fade.apply(MathUtils.clamp(local / 0.40f, 0f, 1f));
        float textAlpha = alpha * textIn;
        batch.begin();
        fonts.micro.setColor(0.38f, 0.92f, 1f, textAlpha);
        fonts.micro.draw(batch, KICKERS[scene], 62f, 300f);
        fonts.heading.setColor(1f, 1f, 1f, textAlpha);
        fonts.heading.draw(batch, TITLES[scene], 62f, 260f, 840f, Align.left, true);
        fonts.body.setColor(0.78f, 0.88f, 0.94f, textAlpha);
        fonts.body.draw(batch, SUBTITLES[scene], 62f, 186f, 820f, Align.left, true);
        fonts.micro.setColor(0.55f, 0.70f, 0.78f, alpha);
        fonts.micro.draw(batch, "ATO 0" + (scene + 1) + " / 03", 62f, 660f);
        fonts.micro.draw(batch, "[ ESPACO ] PULAR CINEMATICA", 1000f, 72f, 210f,
            Align.right, false);
        batch.end();
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
