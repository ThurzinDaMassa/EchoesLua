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
    private final Rectangle playButton = new Rectangle(86f, 294f, 420f, 58f);
    private final Rectangle instructionsButton = new Rectangle(86f, 222f, 420f, 58f);
    private final Rectangle optionsButton = new Rectangle(86f, 150f, 420f, 58f);
    private final Rectangle exitButton = new Rectangle(86f, 78f, 420f, 58f);
    private final Rectangle[] difficultyButtons = {
        new Rectangle(86f, 365f, 128f, 42f),
        new Rectangle(225f, 365f, 128f, 42f),
        new Rectangle(364f, 365f, 142f, 42f)
    };
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];

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

    private void handleInput() {
        if (optionsOpen) {
            handleOptionsInput();
            return;
        }
        if (instructionsOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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

        int difficultyChoice = difficultyAt(mouse);
        if (difficultyChoice >= 0) {
            game.getSettings().setDifficulty(Difficulty.values()[difficultyChoice]);
            game.getAudio().playMenuClick();
            return;
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            optionsOpen = false;
            game.getAudio().playMenuClick();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            game.getSettings().setDifficulty(game.getSettings().getDifficulty().previous());
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            game.getSettings().setDifficulty(game.getSettings().getDifficulty().next());
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            game.getSettings().adjustMasterVolume(0.1f);
            game.getSettings().applyTo(game.getAudio());
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            game.getSettings().adjustMasterVolume(-0.1f);
            game.getSettings().applyTo(game.getAudio());
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.getSettings().setMuted(!game.getSettings().isMuted());
            game.getSettings().applyTo(game.getAudio());
            game.getAudio().playMenuClick();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            game.getSettings().toggleFullscreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) game.getSettings().adjustMusicVolume(-0.1f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) game.getSettings().adjustMusicVolume(0.1f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) game.getSettings().adjustSoundVolume(-0.1f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) game.getSettings().adjustSoundVolume(0.1f);
        game.getSettings().applyTo(game.getAudio());
    }

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
        if ("MARTE".equals(game.getProgress().getSavedScene())) {
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
            shapes.setColor(selected ? new Color(0.025f, 0.18f, 0.23f, 0.98f)
                : hover ? UiTheme.PANEL_LIGHT : UiTheme.PANEL_SOLID);
            shapes.rect(button.x, button.y, button.width, button.height);
            shapes.setColor(selected ? UiTheme.CYAN : UiTheme.BORDER);
            shapes.rect(button.x, button.y, 3f, button.height);
            shapes.rect(button.x, button.y + button.height - 1f, button.width, 1f);
        }
        shapes.end();
        disableBlend();
    }

    private void drawButton(Rectangle button, int index, boolean primary) {
        boolean hover = hoveredButton == index;
        shapes.setColor(hover ? UiTheme.PANEL_LIGHT : UiTheme.PANEL_SOLID);
        shapes.rect(button.x, button.y, button.width, button.height);
        shapes.setColor(primary || hover ? UiTheme.CYAN : UiTheme.BORDER);
        shapes.rect(button.x, button.y, 4f, button.height);
        shapes.rect(button.x, button.y + button.height - 1f, button.width, 1f);
        if (hover) {
            shapes.setColor(0.05f, 0.84f, 1f, 0.08f);
            shapes.rect(button.x + 4f, button.y, button.width - 4f, button.height);
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
        fonts.body.draw(batch,
            "Repare a colonia. Fabrique a arma.\nAtravesse o portal rumo a Marte.",
            86f, 447f, 410f, Align.left, true);

        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "ESCOLHA A DIFICULDADE", 86f, 418f);
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

        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, game.getProgress().hasSavedMission()
            ? "ENTER NOVA   L CONTINUAR " + game.getProgress().getSavedScene() + "   DEL APAGAR SAVE"
            : "ENTER  INICIAR", 68f, 25f);
        fonts.micro.draw(batch,
            "MODO " + game.getSettings().getDifficulty().getLabel()
                + "   //   RECORDE " + game.getProgress().getBestScore(),
            770f, 42f, 430f, Align.right, false);
        set(fonts.micro, UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, "PORTAL DE EXTRACAO // ONLINE", 835f, 650f);
        fonts.micro.draw(batch, "ASTRONAUTA  1 TRIPLE T  2 WINSTON  3 SHREK  4 NEON  //  "
            + game.getSettings().getAstronautType().getLabel(), 650f, 18f, 560f, Align.right, false);

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
        instructionRow("E", "INTERAGIR / ENTRAR NA BASE / USAR BANCADA", 355f);
        instructionRow("M", "ABRIR MENU E GERENCIAR O SAVE", 307f);
        instructionRow("MOUSE / CLIQUE", "MIRAR / DISPARAR A ARMA", 259f);
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
        Difficulty difficulty = game.getSettings().getDifficulty();
        int volume = Math.round(game.getSettings().getMasterVolume() * 100f);

        batch.begin();
        optionRow("DIFICULDADE", "<  " + difficulty.getLabel() + "  >", 455f);
        optionRow("VOLUME GERAL", volume + "%", 405f);
        optionRow("MUSICA  Q / E", Math.round(game.getSettings().getMusicVolume() * 100f) + "%", 355f);
        optionRow("EFEITOS  Z / X", Math.round(game.getSettings().getSoundVolume() * 100f) + "%", 305f);
        optionRow("AUDIO", game.getSettings().isMuted() ? "DESATIVADO" : "ATIVO", 255f);
        optionRow("EXIBICAO", game.getSettings().isFullscreen() ? "TELA CHEIA" : "JANELA", 205f);
        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch,
            "ESQ/DIR  DIFICULDADE    CIMA/BAIXO  VOLUME    M  AUDIO    F  EXIBICAO",
            0f, 162f, WIDTH, Align.center, false);
        modalFooter("SALVAR E VOLTAR");
        batch.end();
    }

    private void optionRow(String name, String value, float y) {
        set(fonts.label, UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, name, 356f, y);
        set(fonts.body, UiTheme.TEXT);
        fonts.body.draw(batch, value, 620f, y, 300f, Align.right, false);
    }

    private void drawModalBase(String title, String subtitle) {
        enableBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.010f, 0.88f);
        shapes.rect(0f, 0f, WIDTH, HEIGHT);
        UiTheme.panel(shapes, 270f, 112f, 740f, 496f, UiTheme.CYAN);
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
        set(fonts.micro, UiTheme.MUTED);
        fonts.micro.draw(batch, "[ ESC ]  " + action, 0f, 140f,
            WIDTH, Align.center, false);
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
