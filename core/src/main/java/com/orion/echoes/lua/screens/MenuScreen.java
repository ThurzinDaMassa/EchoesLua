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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.LunarEchoesGame;

public class MenuScreen extends ScreenAdapter {

    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;

    private static final int STAR_COUNT = 90;

    private final LunarEchoesGame game;
    private final SpriteBatch batch;

    private OrthographicCamera camera;
    private Viewport viewport;

    private ShapeRenderer shapes;
    private BitmapFont font;

    private Texture portalTexture;

    private final Rectangle playButton =
        new Rectangle(
            110f,
            275f,
            300f,
            62f
        );

    private final Rectangle instructionsButton =
        new Rectangle(
            110f,
            195f,
            300f,
            62f
        );

    private final Rectangle exitButton =
        new Rectangle(
            110f,
            115f,
            300f,
            62f
        );

    private final Vector2 mousePosition =
        new Vector2();

    private final float[] starX =
        new float[STAR_COUNT];

    private final float[] starY =
        new float[STAR_COUNT];

    private final float[] starSize =
        new float[STAR_COUNT];

    private float animationTime;

    private boolean instructionsOpen;
    private boolean changingScreen;

    public MenuScreen(
        LunarEchoesGame game
    ) {

        this.game = game;
        this.batch = game.getBatch();
    }

    @Override
    public void show() {

        camera =
            new OrthographicCamera();

        viewport =
            new FitViewport(
                WIDTH,
                HEIGHT,
                camera
            );

        camera.position.set(
            WIDTH / 2f,
            HEIGHT / 2f,
            0f
        );

        camera.update();

        shapes =
            new ShapeRenderer();

        font =
            new BitmapFont();

        font.getRegion()
            .getTexture()
            .setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

        portalTexture =
            game.getAssets()
                .getPortal();

        animationTime = 0f;

        instructionsOpen = false;
        changingScreen = false;

        createStars();
    }

    private void createStars() {

        for (
            int i = 0;
            i < STAR_COUNT;
            i++
        ) {

            starX[i] =
                MathUtils.random(
                    0f,
                    WIDTH
                );

            starY[i] =
                MathUtils.random(
                    0f,
                    HEIGHT
                );

            starSize[i] =
                MathUtils.random(
                    0.8f,
                    2.4f
                );
        }
    }

    @Override
    public void render(
        float delta
    ) {

        if (changingScreen) {
            return;
        }

        animationTime += delta;

        updateMouse();

        handleInput();

        /*
         * IMPORTANTE:
         *
         * Se handleInput mudou de tela,
         * paramos este frame imediatamente.
         */
        if (changingScreen) {
            return;
        }

        clear();

        renderSpaceBackground();

        renderPortalDecoration();

        renderMainPanel();

        renderButtons();

        renderTexts();

        if (instructionsOpen) {
            renderInstructions();
        }
    }

    private void updateMouse() {

        mousePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        viewport.unproject(
            mousePosition
        );
    }

    private void handleInput() {

        if (instructionsOpen) {

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )
            ) {

                instructionsOpen = false;
            }

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ENTER
            )
        ) {

            startGame();
            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.I
            )
        ) {

            instructionsOpen = true;
            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            Gdx.app.exit();
            return;
        }

        if (
            Gdx.input.justTouched()
        ) {

            if (
                playButton.contains(
                    mousePosition
                )
            ) {

                startGame();
                return;
            }

            if (
                instructionsButton.contains(
                    mousePosition
                )
            ) {

                instructionsOpen = true;
                return;
            }

            if (
                exitButton.contains(
                    mousePosition
                )
            ) {

                Gdx.app.exit();
            }
        }
    }

    private void startGame() {

        if (changingScreen) {
            return;
        }

        changingScreen = true;

        game.setScreen(
            new LunarScreen(
                game
            )
        );

        /*
         * NAO chamar dispose() aqui.
         *
         * Evita liberar recursos
         * durante a troca de tela.
         */
    }

    private void clear() {

        Gdx.gl.glClearColor(
            0.006f,
            0.012f,
            0.022f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );
    }

    private void renderSpaceBackground() {

        shapes.setProjectionMatrix(
            camera.combined
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0.006f,
            0.012f,
            0.022f,
            1f
        );

        shapes.rect(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        for (
            int i = 0;
            i < STAR_COUNT;
            i++
        ) {

            float pulse =
                0.45f
                    +
                    MathUtils.sin(
                        animationTime
                            * 1.5f
                            + i
                    )
                        * 0.18f;

            shapes.setColor(
                0.55f,
                0.78f,
                1f,
                pulse
            );

            shapes.circle(
                starX[i],
                starY[i],
                starSize[i]
            );
        }

        shapes.setColor(
            0.02f,
            0.15f,
            0.22f,
            0.35f
        );

        shapes.circle(
            1000f,
            380f,
            310f
        );

        shapes.setColor(
            0.02f,
            0.08f,
            0.14f,
            0.65f
        );

        shapes.circle(
            1000f,
            380f,
            250f
        );

        shapes.end();
    }

    private void renderPortalDecoration() {

        float pulse =
            0.78f
                +
                MathUtils.sin(
                    animationTime * 2f
                )
                    * 0.08f;

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        batch.setColor(
            1f,
            1f,
            1f,
            pulse
        );

        batch.draw(
            portalTexture,
            785f,
            90f,
            390f,
            520f
        );

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void renderMainPanel() {

        enableBlend();

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0.015f,
            0.035f,
            0.055f,
            0.94f
        );

        shapes.rect(
            70f,
            70f,
            390f,
            580f
        );

        shapes.setColor(
            0.04f,
            0.80f,
            1f,
            1f
        );

        shapes.rect(
            70f,
            646f,
            390f,
            4f
        );

        shapes.setColor(
            0.02f,
            0.22f,
            0.32f,
            1f
        );

        shapes.rect(
            95f,
            397f,
            340f,
            1f
        );

        shapes.end();

        shapes.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapes.setColor(
            0.04f,
            0.34f,
            0.46f,
            1f
        );

        shapes.rect(
            70f,
            70f,
            390f,
            580f
        );

        shapes.end();

        disableBlend();
    }

    private void renderButtons() {

        renderButton(
            playButton,
            true
        );

        renderButton(
            instructionsButton,
            false
        );

        renderButton(
            exitButton,
            false
        );
    }

    private void renderButton(
        Rectangle button,
        boolean primary
    ) {

        boolean hovered =
            button.contains(
                mousePosition
            );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        if (hovered) {

            shapes.setColor(
                0.025f,
                0.22f,
                0.30f,
                1f
            );

        } else {

            shapes.setColor(
                0.025f,
                0.075f,
                0.105f,
                1f
            );
        }

        shapes.rect(
            button.x,
            button.y,
            button.width,
            button.height
        );

        if (primary) {

            shapes.setColor(
                0.05f,
                0.86f,
                1f,
                1f
            );

        } else {

            shapes.setColor(
                0.04f,
                0.38f,
                0.50f,
                1f
            );
        }

        shapes.rect(
            button.x,
            button.y,
            4f,
            button.height
        );

        shapes.end();
    }

    private void renderTexts() {

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        font.getData()
            .setScale(
                2.15f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "LUNAR",
            105f,
            590f
        );

        font.setColor(
            0.05f,
            0.86f,
            1f,
            1f
        );

        font.draw(
            batch,
            "ECHOES",
            105f,
            545f
        );

        font.getData()
            .setScale(
                0.90f
            );

        font.setColor(
            0.45f,
            0.62f,
            0.70f,
            1f
        );

        font.draw(
            batch,
            "LUNAR SURVIVAL PROTOCOL",
            107f,
            492f
        );

        font.getData()
            .setScale(
                0.78f
            );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "Explore a superficie lunar.",
            107f,
            455f
        );

        font.draw(
            batch,
            "Colete recursos.",
            107f,
            432f
        );

        font.draw(
            batch,
            "Ative o portal de extracao.",
            107f,
            409f
        );

        drawButtonText(
            "INICIAR MISSAO",
            playButton
        );

        drawButtonText(
            "INSTRUCOES",
            instructionsButton
        );

        drawButtonText(
            "SAIR",
            exitButton
        );

        font.getData()
            .setScale(
                0.68f
            );

        font.setColor(
            0.35f,
            0.48f,
            0.55f,
            1f
        );

        font.draw(
            batch,
            "ENTER - JOGAR",
            110f,
            95f
        );

        font.draw(
            batch,
            "EVA SYSTEM // ORION",
            885f,
            78f
        );

        batch.end();
    }

    private void drawButtonText(
        String text,
        Rectangle button
    ) {

        font.getData()
            .setScale(
                1f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            text,
            button.x,
            button.y + 39f,
            button.width,
            Align.center,
            false
        );
    }

    private void renderInstructions() {

        enableBlend();

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0f,
            0f,
            0f,
            0.82f
        );

        shapes.rect(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        shapes.setColor(
            0.015f,
            0.045f,
            0.07f,
            0.98f
        );

        shapes.rect(
            330f,
            150f,
            620f,
            420f
        );

        shapes.setColor(
            0.05f,
            0.86f,
            1f,
            1f
        );

        shapes.rect(
            330f,
            566f,
            620f,
            4f
        );

        shapes.end();

        disableBlend();

        batch.begin();

        font.getData()
            .setScale(
                1.55f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "PROTOCOLO EVA",
            0f,
            520f,
            WIDTH,
            Align.center,
            false
        );

        font.getData()
            .setScale(
                0.92f
            );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "WASD / SETAS     Movimento",
            410f,
            450f
        );

        font.draw(
            batch,
            "E                Processar gelo na base",
            410f,
            410f
        );

        font.draw(
            batch,
            "ESC              Pausar",
            410f,
            370f
        );

        font.draw(
            batch,
            "R                Reiniciar apos falha",
            410f,
            330f
        );

        font.setColor(
            0.05f,
            0.86f,
            1f,
            1f
        );

        font.draw(
            batch,
            "OBJETIVO",
            410f,
            275f
        );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "Produza agua e H2 para ativar o portal.",
            410f,
            240f
        );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "ESC - VOLTAR",
            0f,
            180f,
            WIDTH,
            Align.center,
            false
        );

        batch.end();
    }

    private void enableBlend() {

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );
    }

    private void disableBlend() {

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        );
    }

    @Override
    public void resize(
        int width,
        int height
    ) {

        viewport.update(
            width,
            height,
            true
        );
    }

    @Override
    public void dispose() {

        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }

        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}
