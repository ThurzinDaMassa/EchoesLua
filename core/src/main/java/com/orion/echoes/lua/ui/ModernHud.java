package com.orion.echoes.lua.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.systems.IceProcessor;
import com.orion.echoes.lua.systems.MissionSystem;
import com.orion.echoes.lua.systems.PlayerStatus;

public class ModernHud {

    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;

    private final OrthographicCamera camera;

    private final Viewport viewport;

    private final ShapeRenderer shapes;

    private final BitmapFont font;

    private float animationTime;

    public ModernHud() {

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

        animationTime +=
            Gdx.graphics.getDeltaTime();

        enableBlend();

        renderStatusPanel(
            status,
            base.isPlayerInside(player)
        );

        renderResourceStrip(
            status
        );

        renderMissionPanel(
            status,
            mission,
            portal,
            missionTime
        );

        renderInteractionPrompt(
            player,
            base,
            status,
            processor
        );

        renderControls();

        if (
            status.getOxygen() < 30f
                &&
                !missionFailed
        ) {

            renderCriticalOxygen();
        }

        if (paused) {

            renderPauseOverlay(
                batch
            );
        }

        if (missionFailed) {

            renderGameOverOverlay(
                batch
            );
        }

        renderTexts(
            batch,
            status,
            player,
            base,
            processor,
            mission,
            portal,
            missionTime,
            paused,
            missionFailed
        );

        disableBlend();
    }

    private void renderStatusPanel(
        PlayerStatus status,
        boolean insideBase
    ) {

        shapes.setProjectionMatrix(
            camera.combined
        );

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        drawPanel(
            22f,
            594f,
            350f,
            104f
        );

        drawBar(
            112f,
            648f,
            230f,
            12f,
            status.getOxygen() / 100f,
            getOxygenColor(
                status.getOxygen()
            )
        );

        drawBar(
            112f,
            617f,
            230f,
            12f,
            status.getEnergy() / 100f,
            new Color(
                0.22f,
                0.90f,
                0.55f,
                1f
            )
        );

        shapes.setColor(
            insideBase
                ? new Color(
                0.20f,
                0.95f,
                0.60f,
                1f
            )
                : new Color(
                0.12f,
                0.68f,
                0.85f,
                1f
            )
        );

        shapes.rect(
            22f,
            594f,
            4f,
            104f
        );

        shapes.end();
    }

    private void renderResourceStrip(
        PlayerStatus status
    ) {

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        drawPanel(
            420f,
            648f,
            440f,
            50f
        );

        drawResourceCard(
            432f,
            657f,
            126f,
            32f
        );

        drawResourceCard(
            570f,
            657f,
            126f,
            32f
        );

        drawResourceCard(
            708f,
            657f,
            140f,
            32f
        );

        shapes.end();
    }

    private void renderMissionPanel(
        PlayerStatus status,
        MissionSystem mission,
        Portal portal,
        float missionTime
    ) {

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        drawPanel(
            900f,
            594f,
            358f,
            104f
        );

        float waterProgress =
            Math.min(
                1f,
                status.getWater()
                    /
                    (float)
                        mission.getRequiredWater()
            );

        float fuelProgress =
            Math.min(
                1f,
                status.getFuel()
                    /
                    (float)
                        mission.getRequiredFuel()
            );

        float progress =
            Math.min(
                waterProgress,
                fuelProgress
            );

        drawBar(
            922f,
            611f,
            314f,
            8f,
            progress,
            portal.isActive()
                ? new Color(
                0.48f,
                0.34f,
                1f,
                1f
            )
                : new Color(
                0.05f,
                0.82f,
                1f,
                1f
            )
        );

        shapes.setColor(
            portal.isActive()
                ? new Color(
                0.50f,
                0.34f,
                1f,
                1f
            )
                : new Color(
                0.04f,
                0.32f,
                0.44f,
                1f
            )
        );

        shapes.rect(
            900f,
            594f,
            4f,
            104f
        );

        shapes.end();
    }

    private void renderInteractionPrompt(
        Player player,
        LunarBase base,
        PlayerStatus status,
        IceProcessor processor
    ) {

        if (
            !base.isPlayerInside(player)
        ) {

            return;
        }

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        drawPanel(
            430f,
            22f,
            420f,
            52f
        );

        shapes.setColor(
            0.05f,
            0.86f,
            1f,
            1f
        );

        shapes.rect(
            444f,
            32f,
            34f,
            32f
        );

        shapes.end();
    }

    private void renderControls() {

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0f,
            0f,
            0f,
            0.28f
        );

        shapes.rect(
            20f,
            18f,
            275f,
            28f
        );

        shapes.end();
    }

    private void renderCriticalOxygen() {

        float pulse =
            0.25f
                +
                (float) Math.sin(
                    animationTime * 6f
                )
                    * 0.10f;

        shapes.begin(
            ShapeRenderer.ShapeType.Line
        );

        shapes.setColor(
            1f,
            0.12f,
            0.12f,
            pulse + 0.35f
        );

        shapes.rect(
            5f,
            5f,
            WIDTH - 10f,
            HEIGHT - 10f
        );

        shapes.end();
    }

    private void drawPanel(
        float x,
        float y,
        float width,
        float height
    ) {

        shapes.setColor(
            0.012f,
            0.028f,
            0.044f,
            0.91f
        );

        shapes.rect(
            x,
            y,
            width,
            height
        );

        shapes.setColor(
            0.05f,
            0.58f,
            0.72f,
            1f
        );

        shapes.rect(
            x,
            y + height - 2f,
            width,
            2f
        );
    }

    private void drawResourceCard(
        float x,
        float y,
        float width,
        float height
    ) {

        shapes.setColor(
            0.025f,
            0.065f,
            0.09f,
            0.92f
        );

        shapes.rect(
            x,
            y,
            width,
            height
        );

        shapes.setColor(
            0.04f,
            0.45f,
            0.58f,
            1f
        );

        shapes.rect(
            x,
            y,
            3f,
            height
        );
    }

    private void drawBar(
        float x,
        float y,
        float width,
        float height,
        float progress,
        Color color
    ) {

        progress =
            Math.max(
                0f,
                Math.min(
                    1f,
                    progress
                )
            );

        shapes.setColor(
            0.025f,
            0.045f,
            0.06f,
            1f
        );

        shapes.rect(
            x,
            y,
            width,
            height
        );

        shapes.setColor(
            color
        );

        shapes.rect(
            x + 1f,
            y + 1f,
            (width - 2f)
                * progress,
            height - 2f
        );
    }

    private Color getOxygenColor(
        float oxygen
    ) {

        if (oxygen <= 25f) {

            return new Color(
                1f,
                0.18f,
                0.18f,
                1f
            );
        }

        if (oxygen <= 50f) {

            return new Color(
                1f,
                0.62f,
                0.14f,
                1f
            );
        }

        return new Color(
            0.05f,
            0.84f,
            1f,
            1f
        );
    }

    private void renderTexts(
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

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        font.getData()
            .setScale(
                0.82f
            );

        font.setColor(
            new Color(
                0.42f,
                0.72f,
                0.80f,
                1f
            )
        );

        font.draw(
            batch,
            "EVA-01",
            42f,
            680f
        );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "O2",
            42f,
            659f
        );

        font.draw(
            batch,
            (int) status.getOxygen()
                + "%",
            308f,
            659f
        );

        font.draw(
            batch,
            "ENERGIA",
            42f,
            628f
        );

        font.draw(
            batch,
            (int) status.getEnergy()
                + "%",
            308f,
            628f
        );

        font.getData()
            .setScale(
                0.70f
            );

        if (
            base.isPlayerInside(
                player
            )
        ) {

            font.setColor(
                new Color(
                    0.20f,
                    0.95f,
                    0.60f,
                    1f
                )
            );

            font.draw(
                batch,
                "BASE SEGURA",
                42f,
                607f
            );

        } else {

            font.setColor(
                new Color(
                    0.55f,
                    0.68f,
                    0.72f,
                    1f
                )
            );

            font.draw(
                batch,
                "SUPERFICIE LUNAR",
                42f,
                607f
            );
        }

        renderResourceText(
            batch,
            "GELO",
            status.getIce(),
            446f
        );

        renderResourceText(
            batch,
            "AGUA",
            status.getWater(),
            584f
        );

        renderResourceText(
            batch,
            "H2",
            status.getFuel(),
            722f
        );

        font.getData()
            .setScale(
                0.74f
            );

        font.setColor(
            new Color(
                0.42f,
                0.66f,
                0.74f,
                1f
            )
        );

        font.draw(
            batch,
            "MISSAO",
            920f,
            678f
        );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "AGUA "
                + status.getWater()
                + "/"
                + mission.getRequiredWater()
                + "   H2 "
                + status.getFuel()
                + "/"
                + mission.getRequiredFuel(),
            920f,
            652f
        );

        font.setColor(
            portal.isActive()
                ? new Color(
                0.63f,
                0.48f,
                1f,
                1f
            )
                : new Color(
                0.55f,
                0.65f,
                0.70f,
                1f
            )
        );

        font.draw(
            batch,
            portal.isActive()
                ? "PORTAL ONLINE"
                : "PORTAL BLOQUEADO",
            920f,
            632f
        );

        font.setColor(
            new Color(
                0.46f,
                0.59f,
                0.64f,
                1f
            )
        );

        font.draw(
            batch,
            formatTime(
                missionTime
            ),
            1175f,
            678f
        );

        if (
            base.isPlayerInside(
                player
            )
        ) {

            font.getData()
                .setScale(
                    0.82f
                );

            font.setColor(
                new Color(
                    0.01f,
                    0.10f,
                    0.14f,
                    1f
                )
            );

            font.draw(
                batch,
                "E",
                456f,
                54f
            );

            font.setColor(
                Color.WHITE
            );

            font.draw(
                batch,
                status.getIce() > 0
                    ? "PROCESSAR GELO"
                    : "SEM GELO PARA PROCESSAR",
                496f,
                54f
            );
        }

        font.getData()
            .setScale(
                0.65f
            );

        font.setColor(
            new Color(
                0.55f,
                0.63f,
                0.66f,
                1f
            )
        );

        font.draw(
            batch,
            "WASD MOVER   |   ESC PAUSAR",
            30f,
            36f
        );

        if (
            !base.isPlayerInside(player)
                &&
                processor.getLastMessage() != null
        ) {

            font.setColor(
                new Color(
                    0.48f,
                    0.58f,
                    0.62f,
                    1f
                )
            );

            font.draw(
                batch,
                processor.getLastMessage(),
                360f,
                35f,
                560f,
                Align.center,
                false
            );
        }

        batch.end();
    }

    private void renderResourceText(
        SpriteBatch batch,
        String label,
        int value,
        float x
    ) {

        font.getData()
            .setScale(
                0.65f
            );

        font.setColor(
            new Color(
                0.43f,
                0.62f,
                0.68f,
                1f
            )
        );

        font.draw(
            batch,
            label,
            x,
            681f
        );

        font.getData()
            .setScale(
                0.92f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            String.valueOf(
                value
            ),
            x,
            660f
        );
    }

    private void renderPauseOverlay(
        SpriteBatch batch
    ) {

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0f,
            0f,
            0f,
            0.74f
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
            0.068f,
            0.98f
        );

        shapes.rect(
            430f,
            245f,
            420f,
            230f
        );

        shapes.setColor(
            0.05f,
            0.82f,
            1f,
            1f
        );

        shapes.rect(
            430f,
            471f,
            420f,
            4f
        );

        shapes.end();
    }

    private void renderGameOverOverlay(
        SpriteBatch batch
    ) {

        shapes.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapes.setColor(
            0f,
            0f,
            0f,
            0.80f
        );

        shapes.rect(
            0f,
            0f,
            WIDTH,
            HEIGHT
        );

        shapes.setColor(
            0.095f,
            0.015f,
            0.02f,
            0.98f
        );

        shapes.rect(
            390f,
            250f,
            500f,
            220f
        );

        shapes.setColor(
            1f,
            0.18f,
            0.18f,
            1f
        );

        shapes.rect(
            390f,
            466f,
            500f,
            4f
        );

        shapes.end();
    }

    public void renderPauseText(
        SpriteBatch batch
    ) {

        batch.begin();

        font.getData()
            .setScale(
                1.8f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "PAUSADO",
            0f,
            410f,
            WIDTH,
            Align.center,
            false
        );

        font.getData()
            .setScale(
                0.85f
            );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "ESC   CONTINUAR",
            0f,
            350f,
            WIDTH,
            Align.center,
            false
        );

        font.draw(
            batch,
            "M     VOLTAR AO MENU",
            0f,
            315f,
            WIDTH,
            Align.center,
            false
        );

        batch.end();
    }

    public void renderGameOverText(
        SpriteBatch batch
    ) {

        batch.begin();

        font.getData()
            .setScale(
                1.85f
            );

        font.setColor(
            new Color(
                1f,
                0.18f,
                0.18f,
                1f
            )
        );

        font.draw(
            batch,
            "MISSAO FALHOU",
            0f,
            405f,
            WIDTH,
            Align.center,
            false
        );

        font.getData()
            .setScale(
                0.86f
            );

        font.setColor(
            Color.WHITE
        );

        font.draw(
            batch,
            "RESERVA DE OXIGENIO ESGOTADA",
            0f,
            355f,
            WIDTH,
            Align.center,
            false
        );

        font.setColor(
            Color.LIGHT_GRAY
        );

        font.draw(
            batch,
            "R  REINICIAR     |     M  MENU",
            0f,
            310f,
            WIDTH,
            Align.center,
            false
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

        int remaining =
            total % 60;

        return String.format(
            "%02d:%02d",
            minutes,
            remaining
        );
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

    public void dispose() {

        shapes.dispose();

        font.dispose();
    }
}
