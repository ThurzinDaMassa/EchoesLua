package com.orion.echoes.lua.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.orion.echoes.lua.LunarEchoesGame;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.entities.Obstacle;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.MarsSatellite;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.progress.MissionScore;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.systems.CombatSystem;
import com.orion.echoes.lua.systems.CollectionSystem;
import com.orion.echoes.lua.systems.ObstacleSystem;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;
import com.orion.echoes.lua.utils.GameConstants;

/** Destino jogável da progressão lunar. Mantém o estado conquistado na Lua. */
public class MarsScreen extends ScreenAdapter {
    private final LunarEchoesGame game;
    private final MissionState mission;
    private final PlayerStatus status;
    private final float lunarMissionTime;
    private final int collectedItems;
    private final float startX;
    private final float startY;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;
    private Player player;
    private UiFonts fonts;
    private Texture surfaceTexture;
    private Texture marsBaseTexture;
    private float time;
    private boolean changingScreen;
    private float autosaveTimer;
    private Array<MarsSatellite> satellites;
    private MarsSatellite nearbySatellite;
    private MarsSatellite activeRepairSatellite;
    private Rectangle marsBaseBounds;
    private float completionTimer = -1f;
    private Array<Obstacle> obstacles;
    private Array<Enemy> enemies;
    private Array<CollectibleItem> items;
    private ParticleManager particles;
    private CombatSystem combat;
    private CollectionSystem collection;
    private ObstacleSystem obstacleSystem;
    private final Vector2 aimWorld = new Vector2();
    private boolean inventoryOpen;
    private float repairSparkTimer;
    private float damageFlashTimer;

    private static final String[] SITE_NAMES = {
        "HERMES", "DEIMOS", "PHOBOS", "ARES RELAY"
    };

    public MarsScreen(LunarEchoesGame game, MissionState mission, PlayerStatus status,
                      float lunarMissionTime, int collectedItems) {
        this(game, mission, status, lunarMissionTime, collectedItems, 1120f, 620f);
    }

    public MarsScreen(LunarEchoesGame game, MissionState mission, PlayerStatus status,
                      float lunarMissionTime, int collectedItems, float startX, float startY) {
        this.game = game;
        this.mission = mission;
        this.status = status;
        this.lunarMissionTime = lunarMissionTime;
        this.collectedItems = collectedItems;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void show() {
        batch = game.getBatch();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        camera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        viewport = new FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera);
        player = new Player(startX, startY, game.getAssets(), game.getSettings().getAstronautType());
        surfaceTexture = game.getAssets().getMarsSurface();
        marsBaseTexture = game.getAssets().getMarsBase();
        satellites = new Array<>();
        // Quatro vertices regulares ao redor da base: leitura imediata e rota equilibrada.
        satellites.add(new MarsSatellite(0, 560f, 980f, game.getAssets()));
        satellites.add(new MarsSatellite(1, 1570f, 980f, game.getAssets()));
        satellites.add(new MarsSatellite(2, 560f, 180f, game.getAssets()));
        satellites.add(new MarsSatellite(3, 1570f, 180f, game.getAssets()));
        marsBaseBounds = new Rectangle(900f, 500f, 460f, 240f);
        createMarsGameplay();
        if ("MARTE".equals(game.getProgress().getSavedScene())) {
            game.getProgress().restoreWorld(items, enemies);
        }
        if (mission.getMarsSatellitesRepaired() == MissionState.MARS_SATELLITE_TARGET) {
            completionTimer = 1.25f;
        }
        positionCamera();
        game.getAudio().playMarsAmbient();
    }

    @Override
    public void render(float delta) {
        if (changingScreen) return;
        float safeDelta = Math.min(delta, 1f / 30f);
        handleInput();
        if (changingScreen) return;
        player.update(safeDelta, status);
        aimWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(aimWorld);
        boolean firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (mission.hasWeapon() && (firing || !player.isMoving())) {
            player.setFacingTowards(aimWorld.x);
        }
        obstacleSystem.handleCollisions(player, obstacles, game.getAudio());
        for (CollectibleItem item : items) item.update(safeDelta);
        collection.update(player, status, items, particles, game.getAudio(), mission);
        boolean playerDamaged = combat.update(safeDelta, player, status, mission, enemies, particles,
            game.getAudio(), aimWorld.x, aimWorld.y,
            firing);
        if (playerDamaged) {
            damageFlashTimer = 0.46f;
            player.triggerDamageFlash();
            particles.emitDamageBurst(player.getCenterX(), player.getCenterY());
            game.getAudio().playPlayerDamage();
        }
        damageFlashTimer = Math.max(0f, damageFlashTimer - safeDelta);
        updateSatellites(safeDelta);
        boolean insideBase = marsBaseBounds.overlaps(player.getBounds());
        if (insideBase) {
            status.addOxygen(14f * safeDelta);
        } else {
            status.removeOxygen(0.35f * safeDelta);
        }
        particles.update(safeDelta, player, insideBase, status.getOxygen() < 25f, null);
        game.getAudio().update(safeDelta, player.isMoving(), insideBase,
            status.getOxygen() < 25f, false, 0f, 0f);
        time += safeDelta;
        if (completionTimer >= 0f) {
            completionTimer -= safeDelta;
            if (completionTimer <= 0f) {
                finishMission();
                return;
            }
        }
        autosaveTimer += safeDelta;
        if (autosaveTimer >= 1f) {
            autosaveTimer = 0f;
            game.getProgress().saveMission(mission, status, "MARTE", lunarMissionTime + time,
                collectedItems, player, items, enemies);
        }
        updateCamera(safeDelta);

        Gdx.gl.glClearColor(0.035f, 0.008f, 0.008f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderMarsWorld();
        renderPlayer();
        renderHud();
    }

    private void createMarsGameplay() {
        obstacles = new Array<>();
        obstacles.add(new Obstacle(380f, 410f, 190f, 105f,
            game.getAssets().getMarsRockSedimentary()));
        obstacles.add(new Obstacle(620f, 820f, 145f, 160f,
            game.getAssets().getMarsRockBasalt()));
        obstacles.add(new Obstacle(1510f, 420f, 178f, 135f,
            game.getAssets().getMarsRockIronstone()));
        obstacles.add(new Obstacle(1750f, 920f, 155f, 118f,
            game.getAssets().getMarsRockIronstone()));
        obstacles.add(new Obstacle(2420f, 620f, 215f, 116f,
            game.getAssets().getMarsRockSedimentary()));
        obstacles.add(new Obstacle(2740f, 1280f, 150f, 168f,
            game.getAssets().getMarsRockBasalt()));

        enemies = new Array<>();
        enemies.add(new Enemy(680f, 1260f, game.getAssets().getEnemyMarsSkimmer()));
        enemies.add(new Enemy(1730f, 1280f, game.getAssets().getEnemyMarsSkimmer()));
        enemies.add(new Enemy(2470f, 430f, game.getAssets().getEnemyMarsSkimmer()));
        enemies.add(new Enemy(2820f, 1010f, game.getAssets().getEnemyMarsSkimmer()));

        items = new Array<>();
        RandomXS128 random = new RandomXS128(mission.getWorldSeed() ^ 0x4D415253L);
        ItemType[] marsItems = {
            ItemType.OXYGEN, ItemType.FOOD, ItemType.MEDKIT,
            ItemType.MEDKIT, ItemType.ICE_ROCK
        };
        for (int i = 0; i < marsItems.length; i++) {
            Vector2 spawn = findSafeMarsSpawn(random, i);
            items.add(new CollectibleItem(marsItems[i], spawn.x, spawn.y,
                game.getAssets(), true));
        }

        particles = new ParticleManager();
        combat = new CombatSystem(game.getAssets(), game.getSettings().getDifficulty());
        collection = new CollectionSystem();
        obstacleSystem = new ObstacleSystem();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventoryOpen = !inventoryOpen;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            changingScreen = true;
            game.getAudio().stopGameplayAudio();
            game.changeScreen(new MenuScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            game.getAudio().toggleMute();
            game.getSettings().setMuted(game.getAudio().isMuted());
        }
    }

    private Vector2 findSafeMarsSpawn(RandomXS128 random, int index) {
        Rectangle candidate = new Rectangle(0f, 0f, 76f, 76f);
        for (int attempt = 0; attempt < 240; attempt++) {
            candidate.x = 150f + random.nextFloat() * (GameConstants.WORLD_WIDTH - 376f);
            candidate.y = 140f + random.nextFloat() * (GameConstants.WORLD_HEIGHT - 330f);
            if (isSafeMarsItemArea(candidate)) return new Vector2(candidate.x, candidate.y);
        }
        return new Vector2(240f + index * 180f, 260f);
    }

    private boolean isSafeMarsItemArea(Rectangle candidate) {
        Rectangle expanded = new Rectangle(candidate);
        expanded.x -= 54f;
        expanded.y -= 54f;
        expanded.width += 108f;
        expanded.height += 108f;
        if (expanded.overlaps(expanded(marsBaseBounds, 90f))) return false;
        for (MarsSatellite satellite : satellites) {
            if (expanded.overlaps(expanded(satellite.getBounds(), 80f))) return false;
        }
        for (Obstacle obstacle : obstacles) {
            if (expanded.overlaps(expanded(obstacle.getBounds(), 42f))) return false;
        }
        for (Enemy enemy : enemies) {
            if (expanded.overlaps(expanded(enemy.getBounds(), 90f))) return false;
        }
        for (CollectibleItem item : items) {
            if (expanded.overlaps(expanded(item.getBounds(), 32f))) return false;
        }
        return true;
    }

    private Rectangle expanded(Rectangle source, float margin) {
        return new Rectangle(source.x - margin, source.y - margin,
            source.width + margin * 2f, source.height + margin * 2f);
    }

    private void updateSatellites(float delta) {
        nearbySatellite = null;
        for (MarsSatellite satellite : satellites) {
            satellite.update(delta);
            if (satellite.isPlayerNear(player)) nearbySatellite = satellite;
        }

        if (activeRepairSatellite != null) {
            if (!activeRepairSatellite.isPlayerNear(player)) {
                activeRepairSatellite.cancelRepair();
                activeRepairSatellite = null;
                mission.notifyAction("Reparo cancelado // volte ao satelite");
                return;
            }
            repairSparkTimer -= delta;
            if (repairSparkTimer <= 0f) {
                particles.emitRepairSparks(activeRepairSatellite.getCenterX(),
                    activeRepairSatellite.getCenterY() + 16f, true);
                player.triggerCraftAnimation();
                repairSparkTimer = 0.13f;
            }
            if (activeRepairSatellite.isRepairComplete()) {
                MarsSatellite completed = activeRepairSatellite;
                mission.repairMarsSatellite(completed.getIndex());
                completed.finishRepair();
                activeRepairSatellite = null;
                particles.emitProcessingBurst(completed.getCenterX(), completed.getCenterY());
                game.getAudio().playRepair();
                if (mission.getMarsSatellitesRepaired() == MissionState.MARS_SATELLITE_TARGET) {
                    completionTimer = 1.25f;
                }
            }
            return;
        }

        if (nearbySatellite != null && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (mission.isMarsSiteScanned(nearbySatellite.getIndex())) {
                mission.notifyAction("Satelite " + SITE_NAMES[nearbySatellite.getIndex()] + " ja esta online");
            } else {
                activeRepairSatellite = nearbySatellite;
                activeRepairSatellite.startRepair();
                repairSparkTimer = 0f;
                player.triggerCraftAnimation();
                mission.notifyAction("Reparando satelite "
                    + SITE_NAMES[nearbySatellite.getIndex()] + " // mantenha-se proximo");
            }
        }
    }

    private void finishMission() {
        if (changingScreen) return;
        changingScreen = true;
        float totalTime = lunarMissionTime + time;
        int score = MissionScore.calculate(totalTime, collectedItems,
            status.getOxygen(), game.getSettings().getDifficulty())
            + mission.getRepairCount() * 350
            + mission.getEnemiesDefeated() * 250
            + mission.getMarsSitesScanned() * 500;
        boolean newRecord = game.getProgress().recordVictory(score, totalTime);
        int bestScore = game.getProgress().getBestScore();
        game.getProgress().clearSavedMission();
        game.getAudio().stopGameplayAudio();
        game.changeScreen(new VictoryScreen(game, totalTime, status.getWater(),
            status.getFuel(), collectedItems, status.getOxygen(), score, bestScore, newRecord));
    }

    private void renderMarsWorld() {
        renderMarsSurface();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.10f, 0.55f, 0.72f, 0.14f);
        shapes.circle(1130f, 620f, 94f + MathUtils.sin(time * 3f) * 4f, 36);

        for (MarsSatellite satellite : satellites) {
            satellite.renderGlow(shapes,
                mission.isMarsSiteScanned(satellite.getIndex()), satellite == nearbySatellite);
        }
        for (CollectibleItem item : items) item.renderGlow(shapes, false);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        renderMarsStructures();
    }

    private void renderMarsSurface() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        final float tile = 627f;
        for (int x = 0; x < GameConstants.WORLD_WIDTH; x += (int) tile) {
            for (int y = 0; y < GameConstants.WORLD_HEIGHT; y += (int) tile) {
                batch.draw(surfaceTexture, x, y, tile, tile, 0, 0,
                    surfaceTexture.getWidth(), surfaceTexture.getHeight(),
                    ((x / (int) tile) & 1) == 1,
                    ((y / (int) tile) & 1) == 1);
            }
        }
        batch.end();
    }

    private void renderMarsStructures() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(marsBaseTexture, 790f, 395f, 680f, 454f);
        for (Obstacle obstacle : obstacles) obstacle.render(batch);
        for (CollectibleItem item : items) item.render(batch);
        for (Enemy enemy : enemies) enemy.render(batch);
        combat.render(batch);
        for (MarsSatellite satellite : satellites) {
            satellite.render(batch, mission.isMarsSiteScanned(satellite.getIndex()));
        }
        batch.setColor(Color.WHITE);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (MarsSatellite satellite : satellites) {
            satellite.renderRepairOverlay(shapes,
                mission.isMarsSiteScanned(satellite.getIndex()), satellite == nearbySatellite);
        }
        for (Enemy enemy : enemies) enemy.renderStatus(shapes);
        particles.render(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderPlayer() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        if (mission.hasWeapon()) player.renderWeapon(batch, aimWorld.x, aimWorld.y);
        batch.end();
    }

    private void renderHud() {
        shapes.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Color marsAccent = new Color(0.94f, 0.28f, 0.12f, 1f);
        UiTheme.panel(shapes, 24f, 640f, 280f, 56f, marsAccent);
        UiTheme.panel(shapes, 324f, 640f, 612f, 56f, marsAccent);
        UiTheme.panel(shapes, 956f, 620f, 300f, 76f, UiTheme.CYAN_SOFT);
        UiTheme.bar(shapes, 1032f, 671f, 142f, 5f,
            status.getOxygen() / GameConstants.MAX_OXYGEN,
            status.getOxygen() < 25f ? UiTheme.DANGER : UiTheme.CYAN);
        UiTheme.bar(shapes, 1032f, 652f, 142f, 5f,
            status.getHealth() / GameConstants.MAX_HEALTH,
            status.getHealth() < 30f ? UiTheme.DANGER : UiTheme.GREEN);
        UiTheme.bar(shapes, 1032f, 633f, 142f, 5f,
            status.getEnergy() / GameConstants.MAX_ENERGY, UiTheme.GREEN);
        UiTheme.panel(shapes, 24f, 22f, 520f, 58f, UiTheme.GREEN);
        if (nearbySatellite != null) {
            UiTheme.panel(shapes, 562f, 22f, 388f, 58f,
                mission.isMarsSiteScanned(nearbySatellite.getIndex()) ? UiTheme.GREEN : UiTheme.WARNING);
        }
        UiTheme.panel(shapes, 970f, 22f, 286f, 58f,
            inventoryOpen ? UiTheme.GREEN : marsAccent);
        if (inventoryOpen) {
            UiTheme.panel(shapes, 980f, 390f, 276f, 220f, marsAccent);
            for (int i = 0; i < 4; i++) {
                shapes.setColor(0.035f, 0.018f, 0.014f, 0.95f);
                shapes.rect(998f, 512f - i * 40f, 240f, 34f);
                shapes.setColor(UiTheme.BORDER);
                shapes.rect(998f, 512f - i * 40f, 240f, 1f);
            }
        }
        shapes.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fonts.label.setColor(0.94f, 0.34f, 0.18f, 1f);
        fonts.label.draw(batch, "MARTE // BASE ARES", 44f, 674f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "SUPERFICIE // SOL " + formatTime(time), 44f, 652f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, mission.getMarsSatellitesRepaired() == MissionState.MARS_SATELLITE_TARGET
            ? "PROTOCOLO ARES CONCLUIDO" : "REPARE OS SATELITES  "
                + mission.getMarsSatellitesRepaired() + "/" + MissionState.MARS_SATELLITE_TARGET, 344f, 674f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, mission.getMarsSatellitesRepaired() == MissionState.MARS_SATELLITE_TARGET
            ? "Transmitindo relatorio final..." : "Repare os quatro satelites marcados no mapa.",
            344f, 652f);
        fonts.label.setColor(UiTheme.CYAN_SOFT);
        fonts.label.draw(batch, "O2", 976f, 674f);
        fonts.micro.draw(batch, "HP", 976f, 655f);
        fonts.micro.draw(batch, "EN", 976f, 636f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, Math.round(status.getOxygen()) + "%", 1172f, 674f,
            56f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getHealth()) + "%", 1172f, 655f,
            56f, Align.right, false);
        fonts.micro.draw(batch, Math.round(status.getEnergy()) + "%", 1172f, 636f,
            56f, Align.right, false);

        fonts.label.setColor(UiTheme.GREEN);
        fonts.label.draw(batch, mission.getMarsSatellitesRepaired() == MissionState.MARS_SATELLITE_TARGET
            ? "BASE ARES // TODOS OS SISTEMAS ONLINE"
            : "RESTAURE OS SATELITES MARCIANOS " + mission.getMarsSatellitesRepaired()
                + "/" + MissionState.MARS_SATELLITE_TARGET,
            44f, 60f);
        fonts.micro.setColor(UiTheme.TEXT);
        fonts.micro.draw(batch,
            mission.getRepairCount() + " reparos lunares  //  arma preservada  //  autosave ativo",
            44f, 40f);
        if (nearbySatellite != null) {
            int index = nearbySatellite.getIndex();
            fonts.label.setColor(mission.isMarsSiteScanned(index) ? UiTheme.GREEN : UiTheme.WARNING);
            fonts.label.draw(batch, nearbySatellite.isRepairing()
                ? "REPARANDO " + SITE_NAMES[index] + " // "
                    + Math.round(nearbySatellite.getRepairProgress() * 100f) + "%"
                : mission.isMarsSiteScanned(index) ? SITE_NAMES[index] + " // ONLINE"
                : "[ E ] REPARAR SATELITE " + SITE_NAMES[index], 582f, 57f);
        }
        fonts.micro.setColor(inventoryOpen ? UiTheme.GREEN : marsAccent);
        fonts.micro.draw(batch, inventoryOpen ? "[ I ] FECHAR INVENTARIO"
            : "[ I ] ABRIR INVENTARIO", 990f, 60f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD MOVER  E INTERAGIR  M MENU", 990f, 40f);
        if (inventoryOpen) drawMarsInventory();
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        renderDamageIndicator();
    }

    private void renderDamageIndicator() {
        if (damageFlashTimer <= 0f) return;
        float strength = MathUtils.clamp(damageFlashTimer / 0.46f, 0f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.84f, 0.02f, 0.01f, 0.12f * strength);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        shapes.setColor(0.96f, 0.04f, 0.02f, 0.38f * strength);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, 12f);
        shapes.rect(0f, GameConstants.VIRTUAL_HEIGHT - 12f, GameConstants.VIRTUAL_WIDTH, 12f);
        shapes.rect(0f, 12f, 12f, GameConstants.VIRTUAL_HEIGHT - 24f);
        shapes.rect(GameConstants.VIRTUAL_WIDTH - 12f, 12f, 12f,
            GameConstants.VIRTUAL_HEIGHT - 24f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawMarsInventory() {
        fonts.label.setColor(new Color(0.94f, 0.34f, 0.18f, 1f));
        fonts.label.draw(batch, "CARGA MARCIANA", 1000f, 586f);
        ItemType[] types = { ItemType.OXYGEN, ItemType.FOOD,
            ItemType.MEDKIT, ItemType.ICE_ROCK };
        String[] names = { "OXIGENIO", "RACAO ARES", "KIT MEDICO", "AMOSTRA" };
        Texture[] icons = { game.getAssets().getMarsOxygen(), game.getAssets().getMarsFood(),
            game.getAssets().getMarsMedkit(), game.getAssets().getMarsIceSample() };
        for (int i = 0; i < types.length; i++) {
            float y = 535f - i * 40f;
            batch.draw(icons[i], 1004f, y - 16f, 24f, 24f);
            fonts.micro.setColor(mission.getCount(types[i]) > 0 ? UiTheme.TEXT : UiTheme.MUTED);
            fonts.micro.draw(batch, names[i], 1038f, y);
            fonts.micro.draw(batch, "x" + mission.getCount(types[i]), 1180f, y,
                42f, Align.right, false);
        }
    }

    private void positionCamera() {
        camera.position.set(player.getCenterX(), player.getCenterY(), 0f);
        camera.update();
    }

    private void updateCamera(float delta) {
        float targetX = MathUtils.clamp(player.getCenterX(), 640f, GameConstants.WORLD_WIDTH - 640f);
        float targetY = MathUtils.clamp(player.getCenterY(), 360f, GameConstants.WORLD_HEIGHT - 360f);
        float smoothing = 1f - (float) Math.pow(0.002f, delta);
        camera.position.x = MathUtils.lerp(camera.position.x, targetX, smoothing);
        camera.position.y = MathUtils.lerp(camera.position.y, targetY, smoothing);
        camera.update();
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (fonts != null) fonts.close();
    }
}
