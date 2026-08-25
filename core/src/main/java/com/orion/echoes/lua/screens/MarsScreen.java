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
    private Texture researchSiteTexture;
    private float time;
    private boolean changingScreen;
    private float autosaveTimer;
    private Rectangle[] researchSites;
    private int nearbySite = -1;
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

    private static final String[] SITE_NAMES = {
        "COMUNICACOES ARES", "REATOR SUBTERRANEO", "LABORATORIO DE SOLO"
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
        researchSiteTexture = game.getAssets().getMarsResearchSite();
        researchSites = new Rectangle[] {
            new Rectangle(520f, 1050f, 118f, 92f),
            new Rectangle(2260f, 1030f, 118f, 92f),
            new Rectangle(2070f, 300f, 118f, 92f)
        };
        marsBaseBounds = new Rectangle(900f, 500f, 460f, 240f);
        createMarsGameplay();
        if ("MARTE".equals(game.getProgress().getSavedScene())) {
            game.getProgress().restoreWorld(items, enemies);
        }
        if (mission.getMarsSitesScanned() == 3) completionTimer = 1.25f;
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
        if (mission.hasWeapon()) player.setFacingTowards(aimWorld.x);
        obstacleSystem.handleCollisions(player, obstacles, game.getAudio());
        for (CollectibleItem item : items) item.update(safeDelta);
        collection.update(player, status, items, particles, game.getAudio(), mission);
        combat.update(safeDelta, player, status, mission, enemies, particles,
            game.getAudio(), aimWorld.x, aimWorld.y,
            Gdx.input.isButtonPressed(Input.Buttons.LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SPACE));
        updateResearchSites();
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
        obstacles.add(new Obstacle(380f, 410f, 180f, 135f, game.getAssets()));
        obstacles.add(new Obstacle(620f, 820f, 150f, 120f, game.getAssets()));
        obstacles.add(new Obstacle(1510f, 420f, 190f, 145f, game.getAssets()));
        obstacles.add(new Obstacle(1750f, 920f, 145f, 115f, game.getAssets()));
        obstacles.add(new Obstacle(2420f, 620f, 205f, 150f, game.getAssets()));
        obstacles.add(new Obstacle(2740f, 1280f, 165f, 125f, game.getAssets()));

        enemies = new Array<>();
        enemies.add(new Enemy(680f, 1260f, game.getAssets()));
        enemies.add(new Enemy(1730f, 1280f, game.getAssets()));
        enemies.add(new Enemy(2470f, 430f, game.getAssets()));
        enemies.add(new Enemy(2820f, 1010f, game.getAssets()));

        items = new Array<>();
        items.add(new CollectibleItem(ItemType.OXYGEN, 430f, 980f, game.getAssets()));
        items.add(new CollectibleItem(ItemType.FOOD, 1560f, 1070f, game.getAssets()));
        items.add(new CollectibleItem(ItemType.MEDKIT, 2220f, 780f, game.getAssets()));
        items.add(new CollectibleItem(ItemType.MEDKIT, 2860f, 460f, game.getAssets()));
        items.add(new CollectibleItem(ItemType.ICE_ROCK, 2580f, 1420f, game.getAssets()));

        particles = new ParticleManager();
        combat = new CombatSystem(game.getAssets(), game.getSettings().getDifficulty());
        collection = new CollectionSystem();
        obstacleSystem = new ObstacleSystem();
    }

    private void handleInput() {
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

    private void updateResearchSites() {
        nearbySite = -1;
        for (int i = 0; i < researchSites.length; i++) {
            Rectangle area = new Rectangle(researchSites[i]);
            area.x -= 48f;
            area.y -= 48f;
            area.width += 96f;
            area.height += 96f;
            if (area.overlaps(player.getBounds())) nearbySite = i;
        }
        if (nearbySite >= 0 && Gdx.input.isKeyJustPressed(Input.Keys.E)
            && mission.scanMarsSite(nearbySite)) {
            game.getAudio().playRepair();
            if (mission.getMarsSitesScanned() == 3) completionTimer = 1.25f;
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

        for (int i = 0; i < researchSites.length; i++) {
            Rectangle site = researchSites[i];
            boolean scanned = mission.isMarsSiteScanned(i);
            Color accent = scanned ? UiTheme.GREEN : new Color(0.95f, 0.34f, 0.14f, 1f);
            shapes.setColor(accent.r, accent.g, accent.b, i == nearbySite ? 0.34f : 0.16f);
            shapes.circle(site.x + site.width / 2f, site.y + site.height / 2f,
                i == nearbySite ? 82f : 68f, 28);
        }
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
        for (int i = 0; i < researchSites.length; i++) {
            Rectangle site = researchSites[i];
            if (mission.isMarsSiteScanned(i)) {
                batch.setColor(0.72f, 1f, 0.82f, 1f);
            } else if (i == nearbySite) {
                batch.setColor(1f, 0.90f, 0.72f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }
            batch.draw(researchSiteTexture, site.x - 24f, site.y - 10f, 166f, 111f);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) enemy.renderStatus(shapes);
        particles.render(shapes);
        shapes.end();
    }

    private void renderPlayer() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        if (mission.hasWeapon() && !player.isFiringAnimation()) player.renderWeapon(batch, aimWorld.x, aimWorld.y);
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
        if (nearbySite >= 0) {
            UiTheme.panel(shapes, 562f, 22f, 388f, 58f,
                mission.isMarsSiteScanned(nearbySite) ? UiTheme.GREEN : UiTheme.WARNING);
        }
        shapes.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fonts.label.setColor(0.94f, 0.34f, 0.18f, 1f);
        fonts.label.draw(batch, "MARTE // BASE ARES", 44f, 674f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "SUPERFICIE // SOL " + formatTime(time), 44f, 652f);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, mission.getMarsSitesScanned() == 3
            ? "PROTOCOLO ARES CONCLUIDO" : "SINCRONIZE OS SISTEMAS  "
                + mission.getMarsSitesScanned() + "/3", 344f, 674f);
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, mission.getMarsSitesScanned() == 3
            ? "Transmitindo relatorio final..." : "Explore os tres pontos marcados no mapa.",
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
        fonts.label.draw(batch, mission.getMarsSitesScanned() == 3
            ? "BASE ARES // TODOS OS SISTEMAS ONLINE"
            : "SINCRONIZE OS SISTEMAS MARCIANOS " + mission.getMarsSitesScanned() + "/3",
            44f, 60f);
        fonts.micro.setColor(UiTheme.TEXT);
        fonts.micro.draw(batch,
            mission.getRepairCount() + " reparos lunares  //  arma preservada  //  autosave ativo",
            44f, 40f);
        if (nearbySite >= 0) {
            fonts.label.setColor(mission.isMarsSiteScanned(nearbySite) ? UiTheme.GREEN : UiTheme.WARNING);
            fonts.label.draw(batch, mission.isMarsSiteScanned(nearbySite)
                ? SITE_NAMES[nearbySite] + " // ONLINE"
                : "[ E ] SINCRONIZAR " + SITE_NAMES[nearbySite], 582f, 57f);
        }
        fonts.micro.setColor(UiTheme.MUTED);
        fonts.micro.draw(batch, "WASD MOVER  E INTERAGIR  M MENU", 970f, 42f,
            260f, Align.right, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
