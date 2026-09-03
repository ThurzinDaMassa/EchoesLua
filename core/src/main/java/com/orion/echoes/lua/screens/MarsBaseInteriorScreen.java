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
import com.orion.echoes.lua.entities.CraftingWorkbench;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.systems.DialogSystem;
import com.orion.echoes.lua.ui.InventoryOverlay;
import com.orion.echoes.lua.ui.StorageOverlay;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;
import com.orion.echoes.lua.utils.GameConstants;

/** Interior pressurizado da Base Ares com fabricação e armazenamento persistente. */
public final class MarsBaseInteriorScreen extends ScreenAdapter {
    private static final Rectangle WALKABLE = new Rectangle(126f, 96f, 1028f, 478f);
    private static final Rectangle AIRLOCK = new Rectangle(520f, 70f, 240f, 138f);
    private static final Rectangle STORAGE_CHEST = new Rectangle(218f, 286f, 210f, 140f);
    private static final Rectangle OFFICER = new Rectangle(574f, 300f, 126f, 178f);
    private static final Rectangle DIALOG_NEXT = new Rectangle(936f, 98f, 254f, 52f);
    private static final Rectangle PAUSE_RESUME = new Rectangle(382f, 264f, 244f, 58f);
    private static final Rectangle PAUSE_MENU = new Rectangle(654f, 264f, 244f, 58f);

    private final LunarEchoesGame game;
    private final MissionState mission;
    private final PlayerStatus status;
    private final float missionTime;
    private final int collectedItems;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private UiFonts fonts;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture interior;
    private Texture storageChestTexture;
    private Texture officerTexture;
    private Player player;
    private CraftingWorkbench workbench;
    private InventoryOverlay inventory;
    private StorageOverlay storage;
    private final Vector2 pointer = new Vector2();
    private boolean nearWorkbench;
    private boolean nearAirlock;
    private boolean nearStorageChest;
    private boolean nearOfficer;
    private boolean storageOpen;
    private boolean changingScreen;
    private boolean paused;
    private float autosaveTimer;
    private final DialogSystem dialog = new DialogSystem();
    private boolean authorizationDialog;

    public MarsBaseInteriorScreen(LunarEchoesGame game, MissionState mission,
                                  PlayerStatus status, float missionTime,
                                  int collectedItems) {
        this.game = game;
        this.mission = mission;
        this.status = status;
        this.missionTime = missionTime;
        this.collectedItems = collectedItems;
    }

    @Override public void show() {
        batch = game.getBatch();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        viewport = new FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera);
        interior = game.getAssets().getBaseInterior();
        storageChestTexture = game.getAssets().getMarsStorageChest();
        officerTexture = game.getAssets().getNpcAresOfficer();
        player = new Player(598f, 154f, game.getAssets(), game.getSettings().getAstronautType());
        workbench = new CraftingWorkbench(874f, 286f, 210f, 178f, game.getAssets());
        inventory = new InventoryOverlay(game.getAssets());
        storage = new StorageOverlay(game.getAssets(), game.getAudio());
        game.getAudio().playMarsAmbient();
        save();
    }

    @Override public void render(float delta) {
        if (changingScreen) return;
        float dt = Math.min(delta, 1f / 30f);
        handleInput();
        if (changingScreen) return;
        if (paused) {
            drawWorld();
            drawHud();
            drawPause();
            return;
        }
        if (storageOpen) storageOpen = storage.update(mission);
        inventory.update(mission);
        if (!storageOpen && !inventory.isOpen() && !dialog.isOpen()) {
            player.update(dt, status);
            clampPlayer();
            workbench.update(dt);
            nearWorkbench = workbench.isPlayerNear(player);
            nearAirlock = AIRLOCK.overlaps(player.getBounds());
            nearStorageChest = STORAGE_CHEST.overlaps(player.getBounds());
            nearOfficer = expanded(OFFICER, 76f).overlaps(player.getBounds());
        }
        status.addOxygen(16f * dt);
        status.addEnergy(12f * dt);
        autosaveTimer += dt;
        if (autosaveTimer >= 1f) { autosaveTimer = 0f; save(); }
        drawWorld();
        drawHud();
        if (storageOpen) drawStorage();
        inventory.render(batch, mission);
        if (dialog.isOpen()) drawDialog();
    }

    private void handleInput() {
        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || dialogButtonPressed()) {
                dialog.next();
                game.getAudio().playMenuClick();
                if (dialog.consumeFinished()) {
                    if (authorizationDialog) mission.completeTitanDialogue();
                    authorizationDialog = false;
                    save();
                }
            }
            return;
        }
        if (paused) {
            if (Gdx.input.justTouched()) {
                updatePointer();
                if (PAUSE_RESUME.contains(pointer)) paused = false;
                else if (PAUSE_MENU.contains(pointer)) {
                    save();
                    changingScreen = true;
                    game.changeScreen(new MenuScreen(game));
                }
            }
            return;
        }
        boolean e = Gdx.input.isKeyJustPressed(Input.Keys.E);
        boolean esc = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
        if (inventory.isOpen() && (e || esc)) { inventory.close(); return; }
        if (storageOpen && (e || esc)) { storageOpen = false; return; }
        if (storageOpen) return;
        if (e && nearStorageChest && mission.hasMarsStorage()) {
            storageOpen = true;
            storage.resetInput();
            game.getAudio().playChestOpen();
            return;
        }
        if (e && nearWorkbench && !mission.hasMarsStorage()) {
            storageOpen = true;
            storage.resetInput();
            game.getAudio().playChestOpen();
            return;
        }
        if (e && nearAirlock) { returnToMars(); return; }
        if (e && nearOfficer) {
            talkToOfficer();
            return;
        }
        if (e) inventory.toggle();
        else if (esc) paused = true;
    }

    private void drawWorld() {
        Gdx.gl.glClearColor(0.025f, 0.010f, 0.008f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0.92f, 0.62f, 0.52f, 1f);
        batch.draw(interior, 0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        batch.setColor(Color.WHITE);
        workbench.render(batch, nearWorkbench);
        batch.setColor(1f, 1f, 1f, mission.hasMarsStorage() ? 1f : 0.28f);
        batch.draw(storageChestTexture, STORAGE_CHEST.x, STORAGE_CHEST.y,
            STORAGE_CHEST.width, STORAGE_CHEST.height);
        batch.setColor(Color.WHITE);
        batch.draw(officerTexture, OFFICER.x, OFFICER.y, OFFICER.width, OFFICER.height);
        batch.setColor(Color.WHITE);
        player.render(batch);
        batch.end();
    }

    private void drawHud() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        UiTheme.panel(shapes, 24f, 634f, 1232f, 62f, new Color(0.94f, 0.30f, 0.13f, 1f));
        UiTheme.panel(shapes, 24f, 22f, 1232f, 54f, UiTheme.CYAN_SOFT);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, "BASE ARES // MODULO DE LOGISTICA", 48f, 675f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "AMBIENTE PRESSURIZADO  //  AUTOSAVE ATIVO", 48f, 652f);
        fonts.label.setColor(nearWorkbench || nearStorageChest || nearAirlock || nearOfficer
            ? UiTheme.WARNING : UiTheme.CYAN_SOFT);
        String action = nearOfficer ? "[ E ] FALAR COM OFICIAL VEGA"
            : nearStorageChest && mission.hasMarsStorage()
            ? "[ E ] ABRIR BAU DE CARGA"
            : nearWorkbench && !mission.hasMarsStorage() ? "[ E ] FABRICAR BAU DE CARGA"
            : nearAirlock ? "[ E ] SAIR PARA A SUPERFICIE DE MARTE"
            : "[ E ] ABRIR INVENTARIO";
        fonts.label.draw(batch, action, 48f, 55f);
        batch.end();
    }

    private void talkToOfficer() {
        if (mission.getMarsSatellitesRepaired() < MissionState.MARS_SATELLITE_TARGET) {
            dialog.start("OFICIAL VEGA",
                "Os quatro satelites ainda estao fora da rede. Restaure-os antes da expedicao.",
                "Volte quando o quadrante Ares estiver completamente online.");
        } else if (!mission.isTitanDialogueComplete()) {
            authorizationDialog = true;
            dialog.start("OFICIAL VEGA",
                "Recebemos um pulso sob a atmosfera de Tita. Nao e um eco natural.",
                "O portal exige sua autorizacao e uma prova: combate em Marte ou amostra de metano.",
                "Valide uma das provas, atravesse o portal e neutralize a ameaca em Tita.");
        } else if (!mission.isTitanPortalUnlocked()
            && mission.getCount(ItemType.METHANE_SAMPLE) > 0) {
            mission.deliverMethaneSample();
            game.getAudio().playPortalActivation();
            dialog.start("OFICIAL VEGA",
                "Amostra confirmada. Assinatura criogenica valida.",
                "Portal de Tita liberado. Boa sorte, astronauta.");
        } else {
            dialog.start("OFICIAL VEGA", mission.getTitanObjective(), mission.getTitanPortalStatus());
        }
    }

    private void drawDialog() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.010f, 0.62f);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        UiTheme.panel(shapes, 74f, 82f, 1132f, 206f, new Color(0.94f, 0.30f, 0.13f, 1f));
        UiTheme.panel(shapes, DIALOG_NEXT.x, DIALOG_NEXT.y, DIALOG_NEXT.width,
            DIALOG_NEXT.height, UiTheme.CYAN);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fonts.micro.setColor(new Color(0.94f, 0.40f, 0.20f, 1f));
        fonts.micro.draw(batch, "TRANSMISSAO ARES // " + dialog.getSpeaker(), 104f, 250f);
        fonts.body.setColor(UiTheme.TEXT);
        fonts.body.draw(batch, dialog.getLine(), 104f, 210f, 780f, Align.left, true);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, (dialog.getIndex() + 1) + " / " + dialog.getLineCount(), 104f, 122f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, "CONTINUAR", DIALOG_NEXT.x, DIALOG_NEXT.y + 34f,
            DIALOG_NEXT.width, Align.center, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean dialogButtonPressed() {
        if (!Gdx.input.justTouched()) return false;
        updatePointer();
        return DIALOG_NEXT.contains(pointer);
    }

    private Rectangle expanded(Rectangle source, float margin) {
        return new Rectangle(source.x - margin, source.y - margin,
            source.width + margin * 2f, source.height + margin * 2f);
    }

    private void drawStorage() {
        storage.render(batch, mission);
    }

    private void drawPause() {
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
        fonts.heading.draw(batch, "BASE ARES PAUSADA", 326f, 414f);
        fonts.body.setColor(UiTheme.MUTED);
        fonts.body.draw(batch, "Escolha uma opcao pelos botoes.", 326f, 370f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, "CONTINUAR", PAUSE_RESUME.x, PAUSE_RESUME.y + 36f,
            PAUSE_RESUME.width, Align.center, false);
        fonts.label.draw(batch, "VOLTAR AO MENU", PAUSE_MENU.x, PAUSE_MENU.y + 36f,
            PAUSE_MENU.width, Align.center, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void clampPlayer() {
        player.setPosition(MathUtils.clamp(player.getX(), WALKABLE.x,
                WALKABLE.x + WALKABLE.width - GameConstants.PLAYER_WIDTH),
            MathUtils.clamp(player.getY(), WALKABLE.y,
                WALKABLE.y + WALKABLE.height - GameConstants.PLAYER_HEIGHT));
    }

    private void returnToMars() {
        save();
        changingScreen = true;
        game.getAudio().stopGameplayAudio();
        game.changeScreen(new MarsScreen(game, mission, status, missionTime,
            collectedItems, 1110f, 430f));
    }

    private void save() {
        game.getProgress().saveMission(mission, status, "MARS_BASE", missionTime,
            collectedItems, player, null, null);
    }

    private void updatePointer() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(pointer);
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        inventory.resize(width, height);
        storage.resize(width, height);
    }

    @Override public void dispose() {
        if (shapes != null) shapes.dispose();
        if (fonts != null) fonts.close();
        if (inventory != null) inventory.dispose();
        if (storage != null) storage.dispose();
    }
}
