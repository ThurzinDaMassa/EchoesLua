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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.LunarEchoesGame;

public class VictoryScreen extends ScreenAdapter {

    private static final float WIDTH =
        1280f;

    private static final float HEIGHT =
        720f;

    private final LunarEchoesGame game;

    private final SpriteBatch batch;

    private final float missionTime;

    private final int water;

    private final int fuel;

    private final int collectedItems;

    private final float oxygen;

    private OrthographicCamera camera;

    private Viewport viewport;

    private ShapeRenderer shapes;

    private BitmapFont font;

    private Texture portalTexture;

    private float animationTime;

    private boolean changingScreen;

    public VictoryScreen(
        LunarEchoesGame game,
        float missionTime,
        int water,
        int fuel,
        int collectedItems,
        float oxygen
    ) {

        this.game = game;

        this.batch =
            game.getBatch();

        this.missionTime =
            missionTime;

        this.water =
            water;

        this.fuel =
            fuel;

        this.collectedItems =
            collectedItems;

        this.oxygen =
            oxygen;
    }

    @Override
    public void show() {

        changingScreen = false;

        animationTime = 0f;

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

        game.getAudio()
            .playVictory();
    }

    @Override
    public void render(
        float delta
    ) {

        if (changingScreen) {
            return;
        }

        animationTime +=
            delta;

        handleInput();

        if (changingScreen) {
            return;
        }

        clear();

        renderBackground();

        renderPortal();

        renderText();
    }

    private void handleInput() {

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.R
            )
        ) {

            changingScreen = true;

            game.getAudio()
                .playMenuClick();

            game.setScreen(
                new LunarScreen(
                    game
                )
            );

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.M
            )
                ||
                Gdx.input.isKeyJustPressed(
                    Input.Keys.ESCAPE
                )
        ) {

            changingScreen = true;

            game.getAudio()
                .playMenuClick();

            game.setScreen(
                new MenuScreen(
                    game
                )
            );
        }
    }

    private void clear() {

        Gdx.gl.glClearColor(
            0.004f,
            0.010f,
            0.018f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );
    }

    private void renderBackground() {

        shapes.setProjectionMatrix(
            camera.combined
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0.004f,
            0.010f,
            0.018f,
            1f
        );

        shapes.rect(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        shapes.end();
    }

    private void renderPortal() {

        float pulse =
            0.78f
                +
                MathUtils.sin(
                    animationTime
                        * 2.5f
                )
                    * 0.10f;

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
            790f,
            55f,
            400f,
            545f
        );

        batch.setColor(
            Color.WHITE
        );

        batch.end();
    }

    private void renderText() {

        batch.begin();

        font.getData()
            .setScale(
                2f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "MISSAO CONCLUIDA",
            100f,
            570f
        );

        font.getData()
            .setScale(
                1f
            );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "Tempo: "
                + formatTime(
                missionTime
            ),
            100f,
            450f
        );

        font.draw(
            batch,
            "Itens coletados: "
                + collectedItems,
            100f,
            410f
        );

        font.draw(
            batch,
            "Agua: "
                + water,
            100f,
            370f
        );

        font.draw(
            batch,
            "H2: "
                + fuel,
            100f,
            330f
        );

        font.draw(
            batch,
            "O2 final: "
                + (int) oxygen
                + "%",
            100f,
            290f
        );

        font.draw(
            batch,
            "R - Nova missao",
            100f,
            180f
        );

        font.draw(
            batch,
            "M - Menu principal",
            100f,
            140f
        );

        batch.end();
    }

    private String formatTime(
        float seconds
    ) {

        int total =
            (int) seconds;

        int minutes =
            total / 60;

        int secondsLeft =
            total % 60;

        return String.format(
            "%02d:%02d",
            minutes,
            secondsLeft
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

        if (
            shapes != null
        ) {

            shapes.dispose();
        }

        if (
            font != null
        ) {

            font.dispose();
        }
    }
}
