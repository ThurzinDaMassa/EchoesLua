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
import com.orion.echoes.lua.config.Difficulty;
import com.orion.echoes.lua.config.AstronautType;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;

public class MenuScreen extends ScreenAdapter {
    private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720f;
    private static final int STAR_COUNT = 110;

    private final LunarEchoesGame game;
    private final SpriteBatch batch;
    private final Vector2 mouse = new Vector2();
    private final Rectangle playButton = new Rectangle(86f, 258f, 420f, 56f);
    private final Rectangle instructionsButton = new Rectangle(86f, 194f, 420f, 56f);
    private final Rectangle optionsButton = new Rectangle(86f, 130f, 420f, 56f);
    private final Rectangle exitButton = new Rectangle(86f, 66f, 420f, 56f);
    private final Rectangle continueSaveButton = new Rectangle(86f, 18f, 200f, 34f);
    private final Rectangle newSaveButton = new Rectangle(298f, 18f, 208f, 34f);
    private final Rectangle modalCloseButton = new Rectangle(500f, 124f, 280f, 44f);
    private final Rectangle[] difficultyButtons = {
        new Rectangle(86f, 330f, 128f, 42f),
        new Rectangle(225f, 330f, 128f, 42f),
        new Rectangle(364f, 330f, 142f, 42f)
    };
    private final Rectangle[] characterButtons = {
        new Rectangle(650f, 10f, 128f, 34f), new Rectangle(786f, 10f, 128f, 34f),
        new Rectangle(922f, 10f, 128f, 34f), new Rectangle(1058f, 10f, 128f, 34f)
    };
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] hoverStrength = new float[4];

    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapes;
    private UiFonts fonts;
    private Texture portalTexture;
    private float animationTime;
    private boolean instructionsOpen;
    private boolean optionsOpen;
    private boolean changingScreen;
    private int hoveredButton = -1;

    public MenuScreen(LunarEchoesGame game) {
        this.game = game;
        batch = game.getBatch();
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f);
        camera.update();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        portalTexture = game.getAssets().getPortal();
        animationTime = 0f;
        instructionsOpen = false;
        optionsOpen = false;
        changingScreen = false;
        createStars();
        game.getAudio().playAmbientMusic();
    }

    private void createStars() {
        MathUtils.random.setSeed(73821L);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, WIDTH);
            starY[i] = MathUtils.random(0f, HEIGHT);
            starSize[i] = MathUtils.random(0.6f, 1.8f);
        }
    }

    @Override
    public void render(float delta) {
        if (changingScreen) return;
        animationTime += Math.min(delta, 1f / 20f);
        updateMouse();
        updateHoverSound();
        updateMenuAnimation(Math.min(delta, 1f / 20f));
        handleInput();
        if (changingScreen) return;

        clear();
        drawBackground();
        drawPortal();
        drawMenuPanels();
        drawMenuText();

        if (instructionsOpen) drawInstructions();
        if (optionsOpen) drawOptions();
    }

    private void updateMouse() {
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
    }

    private void updateHoverSound() {
        if (instructionsOpen || optionsOpen) {
            hoveredButton = -1;
            return;
        }
        int current = buttonAt(mouse);
        if (current >= 0 && current != hoveredButton) game.getAudio().playMenuHover();
        hoveredButton = current;
    }

    private int buttonAt(Vector2 point) {
        if (playButton.contains(point)) return 0;
        if (instructionsButton.contains(point)) return 1;
        if (optionsButton.contains(point)) return 2;
        if (exitButton.contains(point)) return 3;
        return -1;
    }

    private void updateMenuAnimation(float delta) {
        float response = 1f - (float) Math.pow(0.00005f, delta);
        for (int i = 0; i < hoverStrength.length; i++) {
            hoverStrength[i] = MathUtils.lerp(hoverStrength[i], hoveredButton == i ? 1f : 0f,
                response);
        }
    }

    private void handleInput() {
        if (optionsOpen) {
            handleOptionsInput();
            return;
        }
        if (instructionsOpen) {
            if (Gdx.input.justTouched() && modalCloseButton.contains(mouse)) {
                instructionsOpen = false;
                game.getAudio().playMenuClick();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) game.getSettings().setAstronautType(AstronautType.TRIPLE_T);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) game.getSettings().setAstronautType(AstronautType.WINSTON);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) game.getSettings().setAstronautType(AstronautType.SHREK);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) game.getSettings().setAstronautType(AstronautType.NEON);
        if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)
            && game.getProgress().hasSavedMission()) {
            game.getProgress().clearSavedMission();
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)
            && game.getProgress().hasSavedMission()) {
            continueGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            instructionsOpen = true;
            game.getAudio().playMenuClick();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            optionsOpen = true;
            game.getAudio().playMenuClick();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (!Gdx.input.justTouched()) return;

        if (game.getProgress().hasSavedMission() && continueSaveButton.contains(mouse)) {
            continueGame();
            return;
        }
        if (game.getProgress().hasSavedMission() && newSaveButton.contains(mouse)) {
            startGame();
            return;
        }

        int difficultyChoice = difficultyAt(mouse);
        if (difficultyChoice >= 0) {
            game.getSettings().setDifficulty(Difficulty.values()[difficultyChoice]);
            game.getAudio().playMenuClick();
            return;
        }
        for (int i = 0; i < characterButtons.length; i++) {
            if (characterButtons[i].contains(mouse)) {
                game.getSettings().setAstronautType(AstronautType.values()[i]);
                game.getAudio().playMenuClick();
                return;
            }
        }

        switch (buttonAt(mouse)) {
            case 0 -> startGame();
            case 1 -> {
                instructionsOpen = true;
                game.getAudio().playMenuClick();
            }
            case 2 -> {
                optionsOpen = true;
                game.getAudio().playMenuClick();
            }
            case 3 -> Gdx.app.exit();
            default -> { }
        }
    }

    private int difficultyAt(Vector2 point) {
        for (int i = 0; i < difficultyButtons.length; i++) {
            if (difficultyButtons[i].contains(point)) return i;
        }
        return -1;
    }

    private void handleOptionsInput() {
        if (!Gdx.input.justTouched()) return;
        if (modalCloseButton.contains(mouse)) {
            optionsOpen = false;
            game.getAudio().playMenuClick();
            return;
        }
        if (optionMinus(455f).contains(mouse)) {
            game.getSettings().setDifficulty(game.getSettings().getDifficulty().previous());
            game.getAudio().playMenuClick();
        } else if (optionPlus(455f).contains(mouse)) {
            game.getSettings().setDifficulty(game.getSettings().getDifficulty().next());
            game.getAudio().playMenuClick();
        } else if (optionMinus(405f).contains(mouse)) {
            game.getSettings().adjustMasterVolume(-0.1f);
        } else if (optionPlus(405f).contains(mouse)) {
            game.getSettings().adjustMasterVolume(0.1f);
        } else if (optionMinus(355f).contains(mouse)) {
            game.getSettings().adjustMusicVolume(-0.1f);
        } else if (optionPlus(355f).contains(mouse)) {
            game.getSettings().adjustMusicVolume(0.1f);
        } else if (optionMinus(305f).contains(mouse)) {
            game.getSettings().adjustSoundVolume(-0.1f);
        } else if (optionPlus(305f).contains(mouse)) {
            game.getSettings().adjustSoundVolume(0.1f);
        } else if (optionToggle(255f).contains(mouse)) {
            game.getSettings().setMuted(!game.getSettings().isMuted());
        } else if (optionToggle(205f).contains(mouse)) {
            game.getSettings().toggleFullscreen();
        }
        game.getSettings().applyTo(game.getAudio());
        game.getAudio().playMenuClick();
    }

    private Rectangle optionMinus(float y) { return new Rectangle(592f, y - 30f, 42f, 38f); }
    private Rectangle optionPlus(float y) { return new Rectangle(892f, y - 30f, 42f, 38f); }
    private Rectangle optionToggle(float y) { return new Rectangle(620f, y - 30f, 314f, 38f); }

    private void startGame() {
        if (changingScreen) return;
        changingScreen = true;
        game.getAudio().playMenuClick();
        game.getProgress().clearSavedMission();
        game.changeScreen(new LunarScreen(game));
    }

    private void continueGame() {
        if (changingScreen) return;
        changingScreen = true;
        game.getAudio().playMenuClick();
        MissionState mission = game.getProgress().loadMissionState();
        PlayerStatus status = game.getProgress().loadPlayerStatus();
        if ("TITA".equals(game.getProgress().getSavedScene())) {
            game.changeScreen(new TitanScreen(game, mission, status,
                game.getProgress().getSavedMissionTime(),
                game.getProgress().getSavedCollectedItems(),
                game.getProgress().getSavedPlayerX(520f),
                game.getProgress().getSavedPlayerY(660f)));
        } else if ("MARS_BASE".equals(game.getProgress().getSavedScene())) {
            game.changeScreen(new MarsBaseInteriorScreen(game, mission, status,
                game.getProgress().getSavedMissionTime(),
                game.getProgress().getSavedCollectedItems()));
        } else if ("MARTE".equals(game.getProgress().getSavedScene())) {
            game.changeScreen(new MarsScreen(game, mission, status,
                game.getProgress().getSavedMissionTime(),
                game.getProgress().getSavedCollectedItems(),
                game.getProgress().getSavedPlayerX(1120f),
                game.getProgress().getSavedPlayerY(620f)));
        } else if ("BASE".equals(game.getProgress().getSavedScene())) {
            game.changeScreen(new BaseInteriorScreen(game, mission, status,
                game.getProgress().getSavedMissionTime(),
                game.getProgress().getSavedCollectedItems(),
                game.getProgress().getSavedPlayerX(598f),
                game.getProgress().getSavedPlayerY(122f)));
        } else {
            game.changeScreen(new LunarScreen(game, mission, status,
                game.getProgress().getSavedMissionTime()));
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

        for (int i = 0; i < STAR_COUNT; i++) {
            float alpha = 0.28f + MathUtils.sin(animationTime * 1.15f + i * 0.73f) * 0.15f;
            shapes.setColor(0.42f, 0.72f, 0.92f, alpha);
            shapes.circle(starX[i], starY[i], starSize[i], 8);
        }

        shapes.setColor(0.025f, 0.13f, 0.19f, 0.42f);
        shapes.circle(981f, 370f, 286f, 72);
        shapes.setColor(0.008f, 0.035f, 0.062f, 0.95f);
        shapes.circle(981f, 370f, 226f, 72);

        shapes.setColor(0.04f, 0.22f, 0.29f, 0.16f);
        for (int x = 600; x < 1280; x += 48) shapes.rect(x, 0f, 1f, HEIGHT);
        for (int y = 0; y < 720; y += 48) shapes.rect(600f, y, 680f, 1f);
        shapes.end();
        disableBlend();
    }

    private void drawPortal() {
        float pulse = 0.93f + MathUtils.sin(animationTime * 2f) * 0.035f;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, pulse);
        batch.draw(portalTexture, 790f, 82f, 390f, 520f);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawMenuPanels() {
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.007f, 0.017f, 0.026f, 0.96f);
        shapes.rect(48f, 42f, 500f, 636f);
        shapes.setColor(UiTheme.BORDER);
        shapes.rect(48f, 42f, 1f, 636f);
        shapes.rect(547f, 42f, 1f, 636f);
        shapes.setColor(UiTheme.CYAN);
        shapes.rect(48f, 674f, 500f, 4f);
        shapes.rect(48f, 674f, 82f, 4f);

        drawButton(playButton, 0, true);
        drawButton(instructionsButton, 1, false);
        drawButton(optionsButton, 2, false);
        drawButton(exitButton, 3, false);
        for (int i = 0; i < difficultyButtons.length; i++) {
            Rectangle button = difficultyButtons[i];
            boolean selected = game.getSettings().getDifficulty().ordinal() == i;
            boolean hover = button.contains(mouse);
            float selectedPulse = selected ? 0.025f + MathUtils.sin(animationTime * 3f) * 0.008f : 0f;
            shapes.setColor(selected ? new Color(0.025f, 0.18f + selectedPulse, 0.23f, 0.98f)
                : hover ? UiTheme.PANEL_LIGHT : UiTheme.PANEL_SOLID);
            shapes.rect(button.x, button.y, button.width, button.height);
            shapes.setColor(selected ? UiTheme.CYAN : UiTheme.BORDER);
            shapes.rect(button.x, button.y, 3f, button.height);
            shapes.rect(button.x, button.y + button.height - 1f, button.width, 1f);
            if (selected) {
                shapes.setColor(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b, 0.16f);
                shapes.rect(button.x + 3f, button.y + 1f,
                    button.width - 3f, button.height - 2f);
            }
        }
        if (game.getProgress().hasSavedMission()) {
            UiTheme.panel(shapes, continueSaveButton.x, continueSaveButton.y,
                continueSaveButton.width, continueSaveButton.height, UiTheme.GREEN);
            UiTheme.panel(shapes, newSaveButton.x, newSaveButton.y,
                newSaveButton.width, newSaveButton.height, UiTheme.WARNING);
        }
        for (int i = 0; i < characterButtons.length; i++) {
            UiTheme.panel(shapes, characterButtons[i].x, characterButtons[i].y,
                characterButtons[i].width, characterButtons[i].height,
                game.getSettings().getAstronautType().ordinal() == i
                    ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        }
        shapes.end();
        disableBlend();
    }

    private void drawButton(Rectangle button, int index, boolean primary) {
        float hover = hoverStrength[index];
        shapes.setColor(
            MathUtils.lerp(UiTheme.PANEL_SOLID.r, UiTheme.PANEL_LIGHT.r, hover),
            MathUtils.lerp(UiTheme.PANEL_SOLID.g, UiTheme.PANEL_LIGHT.g, hover),
            MathUtils.lerp(UiTheme.PANEL_SOLID.b, UiTheme.PANEL_LIGHT.b, hover), 1f);
        shapes.rect(button.x, button.y, button.width, button.height);
        shapes.setColor(primary || hover > 0.05f ? UiTheme.CYAN : UiTheme.BORDER);
        shapes.rect(button.x, button.y, 4f, button.height);
        shapes.rect(button.x, button.y + button.height - 1f, button.width, 1f);
        if (hover > 0.01f) {
            shapes.setColor(0.05f, 0.84f, 1f, 0.10f * hover);
            shapes.rect(button.x + 4f, button.y, (button.width - 4f) * hover, button.height);
            shapes.setColor(0.42f, 0.94f, 1f, 0.48f * hover);
            shapes.rect(button.x + 4f, button.y, (button.width - 4f) * hover, 2f);
        }
    }

    private void drawMenuText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ORION DEEP SPACE PROGRAM // EVA-07", 86f, 640f);
        set(fonts.display, UiTheme.TEXT);
        fonts.display.draw(batch, "LUNAR", 82f, 583f);
        set(fonts.display, UiTheme.CYAN);
        fonts.display.draw(batch, "ECHOES", 82f, 530f);

        set(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "PROTOCOLO DE SOBREVIVENCIA LUNAR", 86f, 482f);
        set(fonts.body, UiTheme.MUTED);
        fonts.body.draw(batch, "Repare a colonia. Fabrique a arma.", 86f, 447f);
        fonts.body.draw(batch, "Atravesse o portal rumo a Marte.", 86f, 421f);

        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ESCOLHA A DIFICULDADE", 86f, 397f);
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            Rectangle button = difficultyButtons[i];
            set(fonts.label, game.getSettings().getDifficulty() == levels[i]
                ? UiTheme.CYAN : UiTheme.TEXT);
            fonts.label.draw(batch, levels[i].getLabel(), button.x, button.y + 27f,
                button.width, Align.center, false);
        }

        buttonText("INICIAR MISSAO", "01", playButton);
        buttonText("PROTOCOLO EVA", "02", instructionsButton);
        buttonText("CONFIGURACOES", "03", optionsButton);
        buttonText("ENCERRAR SISTEMA", "04", exitButton);

        if (game.getProgress().hasSavedMission()) {
            set(fonts.micro, UiTheme.GREEN);
            fonts.micro.draw(batch, "CONTINUAR " + game.getProgress().getSavedScene(),
                continueSaveButton.x, 40f, continueSaveButton.width, Align.center, false);
            set(fonts.micro, UiTheme.WARNING);
            fonts.micro.draw(batch, "NOVA MISSAO", newSaveButton.x, 40f,
                newSaveButton.width, Align.center, false);
        }
        fonts.micro.draw(batch,
            "MODO " + game.getSettings().getDifficulty().getLabel()
                + "   //   RECORDE " + game.getProgress().getBestScore(),
            770f, 62f, 430f, Align.right, false);
        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "PORTAL DE EXTRACAO // ONLINE", 835f, 650f);
        AstronautType[] astronauts = AstronautType.values();
        for (int i = 0; i < characterButtons.length; i++) {
            set(fonts.micro, game.getSettings().getAstronautType() == astronauts[i]
                ? UiTheme.GREEN : UiTheme.TEXT);
            fonts.micro.draw(batch, astronauts[i].getLabel(), characterButtons[i].x, 32f,
                characterButtons[i].width, Align.center, false);
        }

        batch.end();
    }

    private void buttonText(String text, String number, Rectangle button) {
        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, number, button.x + 22f, button.y + 35f);
        set(fonts.label, UiTheme.TEXT);
        fonts.label.draw(batch, text, button.x + 70f, button.y + 36f);
        set(fonts.label, button.contains(mouse)
            ? UiTheme.CYAN : UiTheme.MUTED);
        fonts.label.draw(batch, ">", button.x + button.width - 42f, button.y + 36f);
    }

    private void drawInstructions() {
        drawModalBase("PROTOCOLO EVA", "CONTROLES E OBJETIVO DA MISSAO");
        batch.begin();
        instructionRow("WASD / SETAS", "MOVIMENTO", 451f);
        instructionRow("SHIFT", "CORRER // CONSOME ENERGIA", 403f);
        instructionRow("E / F", "INVENTARIO E INTERACOES / ABRIR BAUS", 355f);
        instructionRow("ESC", "PAUSAR E USAR OS BOTOES DA TELA", 307f);
        instructionRow("MOUSE / R", "MIRAR, DISPARAR / RECARREGAR", 259f);
        set(fonts.label, UiTheme.CYAN);
        fonts.label.draw(batch, "OBJETIVO", 356f, 207f);
        set(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch, "Siga as 4 etapas do HUD e libere a viagem para Marte.",
            356f, 174f);
        modalFooter("VOLTAR");
        batch.end();
    }

    private void instructionRow(String key, String action, float y) {
        set(fonts.label, UiTheme.CYAN);
        fonts.label.draw(batch, "[ " + key + " ]", 356f, y);
        set(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch, action, 555f, y);
    }

    private void drawOptions() {
        drawModalBase("CONFIGURACOES", "PERFIL LOCAL // SALVAMENTO AUTOMATICO");
        drawOptionControls();
        Difficulty difficulty = game.getSettings().getDifficulty();
        int volume = Math.round(game.getSettings().getMasterVolume() * 100f);

        batch.begin();
        optionRow("DIFICULDADE", difficulty.getLabel(), 455f);
        optionRow("VOLUME GERAL", volume + "%", 405f);
        optionRow("MUSICA", Math.round(game.getSettings().getMusicVolume() * 100f) + "%", 355f);
        optionRow("EFEITOS", Math.round(game.getSettings().getSoundVolume() * 100f) + "%", 305f);
        optionRow("AUDIO", game.getSettings().isMuted() ? "DESATIVADO" : "ATIVO", 255f);
        optionRow("EXIBICAO", game.getSettings().isFullscreen() ? "TELA CHEIA" : "JANELA", 205f);
        set(fonts.label, UiTheme.TEXT);
        for (float y : new float[]{455f, 405f, 355f, 305f}) {
            fonts.label.draw(batch, "-", optionMinus(y).x, y - 3f, 42f, Align.center, false);
            fonts.label.draw(batch, "+", optionPlus(y).x, y - 3f, 42f, Align.center, false);
        }
        modalFooter("SALVAR E VOLTAR");
        batch.end();
    }

    private void drawOptionControls() {
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float y : new float[]{455f, 405f, 355f, 305f}) {
            UiTheme.panel(shapes, optionMinus(y).x, optionMinus(y).y, 42f, 38f, UiTheme.CYAN_SOFT);
            UiTheme.panel(shapes, optionPlus(y).x, optionPlus(y).y, 42f, 38f, UiTheme.CYAN_SOFT);
        }
        UiTheme.panel(shapes, optionToggle(255f).x, optionToggle(255f).y,
            optionToggle(255f).width, optionToggle(255f).height, UiTheme.GREEN);
        UiTheme.panel(shapes, optionToggle(205f).x, optionToggle(205f).y,
            optionToggle(205f).width, optionToggle(205f).height, UiTheme.CYAN);
        shapes.end();
        disableBlend();
    }

    private void optionRow(String name, String value, float y) {
        set(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, name, 356f, y);
        set(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch, value, 650f, y, 220f, Align.center, false);
    }

    private void drawModalBase(String title, String subtitle) {
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.010f, 0.88f);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        UiTheme.panel(shapes, 270f, 112f, 740f, 496f, UiTheme.CYAN);
        UiTheme.panel(shapes, modalCloseButton.x, modalCloseButton.y,
            modalCloseButton.width, modalCloseButton.height,
            modalCloseButton.contains(mouse) ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        shapes.setColor(UiTheme.BORDER);
        shapes.rect(334f, 498f, 612f, 1f);
        shapes.end();
        disableBlend();

        batch.begin();
        set(fonts.heading, UiTheme.TEXT);
        fonts.heading.draw(batch, title, 0f, 565f, WIDTH, Align.center, false);
        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, subtitle, 0f, 528f, WIDTH, Align.center, false);
        batch.end();
    }

    private void modalFooter(String action) {
        set(fonts.micro, modalCloseButton.contains(mouse) ? UiTheme.GREEN : UiTheme.TEXT);
        fonts.micro.draw(batch, action, modalCloseButton.x, 151f,
            modalCloseButton.width, Align.center, false);
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
