package com.orion.echoes.lua.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.orion.echoes.lua.LunarEchoesGame;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.CraftingWorkbench;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;
import com.orion.echoes.lua.ui.InventoryOverlay;
import com.orion.echoes.lua.utils.GameConstants;

/** Playable pressurized interior of the lunar base. */
public final class BaseInteriorScreen extends ScreenAdapter {
    private static final float DEFAULT_START_X = 598f;
    private static final float DEFAULT_START_Y = 122f;
    private static final Rectangle WALKABLE = new Rectangle(126f, 96f, 1028f, 478f);
    private static final Rectangle AIRLOCK = new Rectangle(520f, 70f, 240f, 150f);
    private static final Rectangle WEAPON_CARD = new Rectangle(338f, 430f, 280f, 108f);
    private static final Rectangle ICE_CARD = new Rectangle(662f, 430f, 280f, 108f);
    private static final Rectangle CRAFT_BUTTON = new Rectangle(690f, 174f, 220f, 58f);
    private static final Rectangle CRAFT_CLOSE_BUTTON = new Rectangle(924f, 568f, 146f, 42f);
    private static final Rectangle PAUSE_RESUME = new Rectangle(382f, 264f, 244f, 58f);
    private static final Rectangle PAUSE_MENU = new Rectangle(654f, 264f, 244f, 58f);
    private static final float CRAFT_DURATION = 3f;

    private final LunarEchoesGame game;
    private final MissionState mission;
    private final PlayerStatus status;
    private final int collectedItems;
    private final float startX;
    private final float startY;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private UiFonts fonts;
    private InventoryOverlay inventoryOverlay;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture interiorTexture;
    private Player player;
    private CraftingWorkbench workbench;
    private ParticleManager particles;
    private float missionTime;
    private float autosaveTimer;
    private float transitionCooldown;
    private float animationTime;
    private boolean nearWorkbench;
    private boolean nearAirlock;
    private boolean changingScreen;
    private boolean paused;
    private boolean craftingMenuOpen;
    private int selectedRecipe;
    private int activeRecipe = -1;
    private float craftingTimer;
    private float shownOxygen = -1f;
    private float shownHealth = -1f;
    private float shownEnergy = -1f;
    private final Vector2 uiPointer = new Vector2();

    public BaseInteriorScreen(LunarEchoesGame game, MissionState mission,
                              PlayerStatus status, float missionTime,
                              int collectedItems) {
        this(game, mission, status, missionTime, collectedItems,
            DEFAULT_START_X, DEFAULT_START_Y);
    }

    public BaseInteriorScreen(LunarEchoesGame game, MissionState mission,
                              PlayerStatus status, float missionTime,
                              int collectedItems, float startX, float startY) {
        this.game = game;
        this.mission = mission;
        this.status = status;
        this.missionTime = missionTime;
        this.collectedItems = collectedItems;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void show() {
        batch = game.getBatch();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        inventoryOverlay = new InventoryOverlay(game.getAssets());
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        viewport = new FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera);
        viewport.apply();
        interiorTexture = game.getAssets().getBaseInterior();
        player = new Player(startX, startY, game.getAssets(),
            game.getSettings().getAstronautType());
        workbench = new CraftingWorkbench(790f, 230f, 390f, 260f, game.getAssets());
        particles = new ParticleManager();
        transitionCooldown = 0.45f;
        clampPlayerToRoom();
        camera.update();
        game.getAudio().playAmbientMusic();
    }

    @Override
    public void render(float delta) {
        if (changingScreen) return;
        float safeDelta = Math.min(delta, 1f / 30f);
        animationTime += safeDelta;
        missionTime += safeDelta;
        transitionCooldown = Math.max(0f, transitionCooldown - safeDelta);
        workbench.update(safeDelta);
        updateCrafting(safeDelta);

        handleGlobalInput();
        if (changingScreen) return;
        if (paused) {
            renderInterior();
            renderHud();
            renderPauseMenu();
            return;
        }
        inventoryOverlay.update(mission);

        if (!craftingMenuOpen && !inventoryOverlay.isOpen()) {
            player.update(safeDelta, status);
            clampPlayerToRoom();
            if (workbench.blocks(player)) {
                player.restorePreviousPosition();
                clampPlayerToRoom();
            }
        }

        nearWorkbench = workbench.isPlayerNear(player);
        nearAirlock = AIRLOCK.overlaps(player.getBounds());
        handleInteraction();
        if (changingScreen) return;

        status.addOxygen(GameConstants.BASE_OXYGEN_RECHARGE_RATE * safeDelta);
        status.addEnergy(GameConstants.ENERGY_RECOVERY_RATE * 0.65f * safeDelta);
        updateHudAnimation(safeDelta);
        particles.update(safeDelta, player, false, false, null, false);
        game.getAudio().update(safeDelta, player.isMoving(), true,
            false, false, 0f, 0f);

        autosaveTimer += safeDelta;
        if (autosaveTimer >= 1f) {
            autosaveTimer = 0f;
            saveInterior();
        }

        renderInterior();
        if (craftingMenuOpen) {
            renderCraftingMenu();
        } else {
            renderHud();
        }
        inventoryOverlay.render(batch, mission);
    }

    private void handleGlobalInput() {
        if (paused) {
            if (Gdx.input.justTouched()) {
                uiPointer.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(uiPointer);
                if (PAUSE_RESUME.contains(uiPointer)) paused = false;
                else if (PAUSE_MENU.contains(uiPointer)) {
                    saveInterior();
                    changingScreen = true;
                    game.changeScreen(new MenuScreen(game));
                }
            }
            return;
        }
        boolean inventoryKey = Gdx.input.isKeyJustPressed(Input.Keys.E);
        if (inventoryOverlay.isOpen()
            && (inventoryKey || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            inventoryOverlay.close();
            return;
        }
        if (!craftingMenuOpen && inventoryKey && !nearWorkbench && !nearAirlock) {
            inventoryOverlay.toggle();
            return;
        }
        if (craftingMenuOpen && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (activeRecipe < 0) craftingMenuOpen = false;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = true;
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            game.getAudio().toggleMute();
            game.getSettings().setMuted(game.getAudio().isMuted());
        }
    }

    private void renderPauseMenu() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.003f, 0.007f, 0.011f, 0.80f);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        UiTheme.panel(shapes, 286f, 206f, 708f, 300f, UiTheme.CYAN);
        UiTheme.panel(shapes, PAUSE_RESUME.x, PAUSE_RESUME.y, PAUSE_RESUME.width,
            PAUSE_RESUME.height, UiTheme.GREEN);
        UiTheme.panel(shapes, PAUSE_MENU.x, PAUSE_MENU.y, PAUSE_MENU.width,
            PAUSE_MENU.height, UiTheme.WARNING);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.heading.setColor(UiTheme.TEXT);
        fonts.heading.draw(batch, "MISSAO PAUSADA", 326f, 414f);
        fonts.body.setColor(UiTheme.MUTED);
        fonts.body.draw(batch, "Escolha uma opcao usando os botoes.", 326f, 370f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, "CONTINUAR", PAUSE_RESUME.x, PAUSE_RESUME.y + 36f,
            PAUSE_RESUME.width, Align.center, false);
        fonts.label.draw(batch, "VOLTAR AO MENU", PAUSE_MENU.x, PAUSE_MENU.y + 36f,
            PAUSE_MENU.width, Align.center, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void handleInteraction() {
        if (craftingMenuOpen) {
            handleCraftingMenuInput();
            return;
        }
        if (transitionCooldown > 0f || !Gdx.input.isKeyJustPressed(Input.Keys.E)) return;
        if (nearWorkbench) {
            craftingMenuOpen = true;
            selectedRecipe = mission.hasWeapon() ? 1 : 0;
        } else if (nearAirlock) {
            returnToMoon();
        }
    }

    private void handleCraftingMenuInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || activeRecipe >= 0) return;
        uiPointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(uiPointer);
        if (CRAFT_CLOSE_BUTTON.contains(uiPointer)) {
            craftingMenuOpen = false;
        } else if (WEAPON_CARD.contains(uiPointer)) {
            selectedRecipe = 0;
        } else if (ICE_CARD.contains(uiPointer)) {
            selectedRecipe = 1;
        } else if (CRAFT_BUTTON.contains(uiPointer) && canCraftSelected()) {
            activeRecipe = selectedRecipe;
            craftingTimer = CRAFT_DURATION;
            mission.notifyAction(selectedRecipe == 0
                ? "Fabricando Arma EVA // 3 segundos"
                : "Processando gelo // 3 segundos");
        }
    }

    private boolean canCraftSelected() {
        return selectedRecipe == 0 ? mission.canCraftWeapon() : workbench.canProcessIce(status);
    }

    private void updateCrafting(float delta) {
        if (activeRecipe < 0) return;
        craftingTimer = Math.max(0f, craftingTimer - delta);
        if (craftingTimer > 0f) return;
        if (activeRecipe == 0) {
            workbench.craftWeapon(player, mission, particles, game.getAudio());
        } else {
            workbench.processIce(player, mission, status, particles, game.getAudio());
        }
        activeRecipe = -1;
    }

    private void returnToMoon() {
        saveInterior();
        changingScreen = true;
        float returnX = GameConstants.BASE_X + GameConstants.BASE_WIDTH / 2f
            - GameConstants.PLAYER_WIDTH / 2f;
        float returnY = GameConstants.BASE_Y - GameConstants.PLAYER_HEIGHT * 0.72f;
        game.changeScreen(new LunarScreen(game, mission, status, missionTime,
            returnX, returnY));
    }

    private void saveInterior() {
        game.getProgress().saveMission(mission, status, "BASE", missionTime,
            collectedItems, player, null, null);
    }

    private void clampPlayerToRoom() {
        float x = MathUtils.clamp(player.getX(), WALKABLE.x,
            WALKABLE.x + WALKABLE.width - player.getWidth());
        float y = MathUtils.clamp(player.getY(), WALKABLE.y,
            WALKABLE.y + WALKABLE.height - player.getHeight());
        if (x != player.getX() || y != player.getY()) player.setPosition(x, y);
    }

    private void renderInterior() {
        Gdx.gl.glClearColor(0.006f, 0.012f, 0.018f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(interiorTexture, 0f, 0f,
            GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        workbench.render(batch, nearWorkbench);
        player.render(batch);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float pulse = 0.12f + MathUtils.sin(animationTime * 3.2f) * 0.035f;
        shapes.setColor(UiTheme.CYAN.r, UiTheme.CYAN.g, UiTheme.CYAN.b,
            nearWorkbench ? 0.20f : pulse);
        shapes.rect(852f, 214f, 255f, 5f);
        shapes.setColor(UiTheme.GREEN.r, UiTheme.GREEN.g, UiTheme.GREEN.b,
            nearAirlock ? 0.32f : 0.12f);
        shapes.rect(570f, 89f, 140f, 5f);
        particles.render(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderCraftingMenu() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.010f, 0.78f);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        UiTheme.panel(shapes, 290f, 120f, 700f, 490f, UiTheme.CYAN);
        UiTheme.panel(shapes, WEAPON_CARD.x, WEAPON_CARD.y, WEAPON_CARD.width,
            WEAPON_CARD.height, selectedRecipe == 0 ? UiTheme.CYAN : UiTheme.CYAN_SOFT);
        UiTheme.panel(shapes, ICE_CARD.x, ICE_CARD.y, ICE_CARD.width,
            ICE_CARD.height, selectedRecipe == 1 ? UiTheme.CYAN : UiTheme.CYAN_SOFT);
        UiTheme.panel(shapes, 338f, 258f, 604f, 140f, UiTheme.CYAN_SOFT);
        Color buttonColor = canCraftSelected() ? UiTheme.GREEN : UiTheme.MUTED;
        UiTheme.panel(shapes, CRAFT_BUTTON.x, CRAFT_BUTTON.y, CRAFT_BUTTON.width,
            CRAFT_BUTTON.height, buttonColor);
        UiTheme.panel(shapes, CRAFT_CLOSE_BUTTON.x, CRAFT_CLOSE_BUTTON.y,
            CRAFT_CLOSE_BUTTON.width, CRAFT_CLOSE_BUTTON.height, UiTheme.CYAN_SOFT);
        if (activeRecipe >= 0) {
            UiTheme.bar(shapes, 370f, 216f, 270f, 8f,
                1f - craftingTimer / CRAFT_DURATION, UiTheme.CYAN);
        }
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.heading.setColor(UiTheme.TEXT);
        fonts.heading.draw(batch, "BANCADA DE FABRICACAO", 320f, 578f,
            640f, Align.center, false);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "SELECIONE UMA RECEITA // O TEMPO DE PRODUCAO E 3 SEGUNDOS",
            320f, 548f, 640f, Align.center, false);

        batch.draw(game.getAssets().getEvaWeapon(), 355f, 450f, 88f, 44f);
        fonts.label.setColor(selectedRecipe == 0 ? UiTheme.CYAN : UiTheme.TEXT);
        fonts.label.draw(batch, "ARMA EVA", 455f, 505f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, mission.hasWeapon() ? "JA FABRICADA" : "COMBATE DIRECIONAL",
            455f, 477f);

        batch.draw(game.getAssets().getIceRock(), 690f, 446f, 66f, 66f);
        fonts.label.setColor(selectedRecipe == 1 ? UiTheme.CYAN : UiTheme.TEXT);
        fonts.label.draw(batch, "PROCESSAR GELO", 770f, 505f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "SUPORTE DE VIDA", 770f, 477f);

        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, selectedRecipe == 0 ? "RECEITA // ARMA EVA"
            : "RECEITA // GELO PROCESSADO", 366f, 370f);
        fonts.body.setColor(UiTheme.TEXT);
        if (selectedRecipe == 0) {
            fonts.body.draw(batch, "Emissor A: " + mission.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_A)
                + "/1   Celula B: " + mission.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_B)
                + "/1   Mira C: " + mission.getCount(com.orion.echoes.lua.enums.ItemType.WEAPON_PART_C)
                + "/1", 366f, 330f);
            fonts.micro.setColor(mission.canCraftWeapon() ? UiTheme.GREEN : UiTheme.WARNING);
            fonts.micro.draw(batch, mission.hasWeapon() ? "ITEM JA DISPONIVEL NO EQUIPAMENTO"
                : mission.canCraftWeapon() ? "TODOS OS COMPONENTES DISPONIVEIS"
                : "COLETE AS TRES PECAS NA SUPERFICIE", 366f, 294f);
        } else {
            fonts.body.draw(batch, "Gelo lunar: " + status.getIce() + "/"
                + GameConstants.ICE_PROCESS_COST, 366f, 330f);
            fonts.micro.setColor(workbench.canProcessIce(status) ? UiTheme.GREEN : UiTheme.WARNING);
            fonts.micro.draw(batch, "+ AGUA   + COMBUSTIVEL   + OXIGENIO", 366f, 294f);
        }

        fonts.label.setColor(buttonColor);
        String buttonText = activeRecipe >= 0
            ? "FABRICANDO  " + String.format("%.1fs", craftingTimer)
            : canCraftSelected() ? "FABRICAR ITEM" : "RECURSOS INSUFICIENTES";
        fonts.label.draw(batch, buttonText, CRAFT_BUTTON.x, CRAFT_BUTTON.y + 38f,
            CRAFT_BUTTON.width, Align.center, false);
        fonts.micro.setColor(UiTheme.TEXT);
        fonts.micro.draw(batch, "FECHAR", CRAFT_CLOSE_BUTTON.x, CRAFT_CLOSE_BUTTON.y + 27f,
            CRAFT_CLOSE_BUTTON.width, Align.center, false);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, activeRecipe >= 0 ? "PROCESSO EM ANDAMENTO"
            : "SELECIONE A RECEITA E USE O BOTAO DE FABRICAR", 340f, 150f,
            600f, Align.center, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderHud() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        UiTheme.panel(shapes, 24f, 628f, 294f, 68f, UiTheme.GREEN);
        UiTheme.panel(shapes, 338f, 628f, 598f, 68f, UiTheme.CYAN);
        UiTheme.panel(shapes, 956f, 620f, 300f, 76f, UiTheme.CYAN_SOFT);
        UiTheme.bar(shapes, 1036f, 670f, 138f, 5f,
            shownOxygen / GameConstants.MAX_OXYGEN, UiTheme.CYAN);
        UiTheme.bar(shapes, 1036f, 651f, 138f, 5f,
            shownHealth / GameConstants.MAX_HEALTH, UiTheme.GREEN);
        UiTheme.bar(shapes, 1036f, 632f, 138f, 5f,
            shownEnergy / GameConstants.MAX_ENERGY, UiTheme.GREEN);
        UiTheme.panel(shapes, 320f, 20f, 640f, 72f,
            nearWorkbench ? UiTheme.CYAN : nearAirlock ? UiTheme.GREEN : UiTheme.CYAN_SOFT);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.label.setColor(UiTheme.GREEN);
        fonts.label.draw(batch, "BASE LUNAR // INTERIOR", 44f, 676f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "AMBIENTE PRESSURIZADO", 44f, 650f);

        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "CENTRO DE FABRICACAO EVA", 358f, 676f);
        fonts.micro.setColor(UiTheme.TEXT);
        fonts.micro.draw(batch, mission.hasWeapon()
            ? "Arma EVA online // bancada disponivel para processamento"
            : "Monte a arma e processe recursos na bancada", 358f, 650f);

        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "O2", 976f, 674f);
        fonts.micro.draw(batch, "HP", 976f, 655f);
        fonts.micro.draw(batch, "EN", 976f, 636f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, Math.round(status.getOxygen()) + "%", 1174f, 674f,
            56f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getHealth()) + "%", 1174f, 655f,
            56f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getEnergy()) + "%", 1174f, 636f,
            56f, Align.right, false);

        drawInteractionText();
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD MOVER   E INTERAGIR   ESC PAUSAR", 982f, 34f,
            260f, Align.right, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawInteractionText() {
        String title;
        String detail;
        Color color;
        if (nearWorkbench) {
            color = UiTheme.CYAN;
            if (mission.canCraftWeapon()) {
                title = "[ E ] FABRICAR ARMA EVA";
                detail = "PECAS A + B + C // COMPONENTES COMPLETOS";
            } else if (status.getIce() >= GameConstants.ICE_PROCESS_COST) {
                title = "[ E ] PROCESSAR GELO";
                detail = "GELO > AGUA + COMBUSTIVEL + O2";
            } else if (mission.hasWeapon()) {
                title = "BANCADA OPERACIONAL";
                detail = "COLETE GELO PARA PROCESSAR NOVOS RECURSOS";
            } else {
                title = "BANCADA // ARMA " + mission.getWeaponPartCount() + "/3";
                detail = "ENCONTRE AS PECAS A, B E C NA SUPERFICIE";
            }
        } else if (nearAirlock) {
            color = UiTheme.GREEN;
            title = "[ E ] SAIR PARA A SUPERFICIE";
            detail = "ECLUSA PRESSURIZADA // ESTADO SALVO";
        } else {
            color = UiTheme.CYAN_SOFT;
            title = "APROXIME-SE DA BANCADA OU DA ECLUSA";
            detail = mission.getLastMessage();
        }
        fonts.label.setColor(color);
        fonts.label.draw(batch, title, 340f, 70f, 600f, Align.center, false);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, detail, 340f, 43f, 600f, Align.center, false);
    }

    private void updateHudAnimation(float delta) {
        if (shownOxygen < 0f) {
            shownOxygen = status.getOxygen();
            shownHealth = status.getHealth();
            shownEnergy = status.getEnergy();
        }
        float response = 1f - (float) Math.pow(0.0008f, delta);
        shownOxygen = MathUtils.lerp(shownOxygen, status.getOxygen(), response);
        shownHealth = MathUtils.lerp(shownHealth, status.getHealth(), response);
        shownEnergy = MathUtils.lerp(shownEnergy, status.getEnergy(), response);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
        if (inventoryOverlay != null) inventoryOverlay.resize(width, height);
    }

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (fonts != null) fonts.close();
        if (inventoryOverlay != null) inventoryOverlay.dispose();
    }
}
