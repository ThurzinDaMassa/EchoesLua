package com.orion.echoes.lua.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.orion.echoes.lua.LunarEchoesGame;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.entities.Obstacle;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.entities.RepairStation;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.CombatSystem;
import com.orion.echoes.lua.systems.CollectionSystem;
import com.orion.echoes.lua.systems.IceProcessor;
import com.orion.echoes.lua.systems.MissionSystem;
import com.orion.echoes.lua.systems.ObstacleSystem;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.systems.SurvivalSystem;
import com.orion.echoes.lua.ui.ModernHud;
import com.orion.echoes.lua.utils.GameConstants;
import com.orion.echoes.lua.utils.MoonSurfaceTextureFactory;

public class LunarScreen extends ScreenAdapter {

    private final LunarEchoesGame game;

    private final SpriteBatch batch;

    private final AudioManager audio;

    private OrthographicCamera camera;

    private Viewport viewport;

    private ShapeRenderer shapeRenderer;

    private Texture moonSurfaceTexture;

    private Player player;

    private LunarBase lunarBase;

    private Portal portal;

    private Array<CollectibleItem> items;

    private Array<Obstacle> obstacles;

    private Array<RepairStation> repairStations;

    private Array<Enemy> enemies;

    private PlayerStatus playerStatus;

    private CollectionSystem collectionSystem;

    private IceProcessor iceProcessor;

    private SurvivalSystem survivalSystem;

    private ParticleManager particleManager;

    private ObstacleSystem obstacleSystem;

    private MissionSystem missionSystem;

    private MissionState missionState;

    private CombatSystem combatSystem;

    private final Vector2 aimWorld = new Vector2();

    private RepairStation nearbyStation;

    private ModernHud hud;

    private boolean changingScreen;

    private boolean paused;

    private boolean defeatSoundPlayed;

    private float missionTime;

    private float autosaveTimer;

    private MissionState resumedMission;

    private PlayerStatus resumedStatus;

    private float resumedTime;

    public LunarScreen(
        LunarEchoesGame game
    ) {

        this(game, null, null, 0f);
    }

    public LunarScreen(
        LunarEchoesGame game,
        MissionState resumedMission,
        PlayerStatus resumedStatus,
        float resumedTime
    ) {

        this.game = game;

        this.batch =
            game.getBatch();

        this.audio =
            game.getAudio();

        this.resumedMission = resumedMission;
        this.resumedStatus = resumedStatus;
        this.resumedTime = resumedTime;
    }

    @Override
    public void show() {

        changingScreen = false;

        paused = false;

        defeatSoundPlayed = false;

        missionTime = resumedTime;
        autosaveTimer = 0f;

        createCamera();

        createRenderResources();

        createWorld();

        createSystems();

        createHud();

        positionCameraImmediately();

        audio.playAmbientMusic();
    }

    private void createCamera() {

        camera =
            new OrthographicCamera();

        viewport =
            new FitViewport(
                GameConstants.VIRTUAL_WIDTH,
                GameConstants.VIRTUAL_HEIGHT,
                camera
            );

        viewport.apply();

        camera.update();
    }

    private void createRenderResources() {

        shapeRenderer =
            new ShapeRenderer();

        moonSurfaceTexture =
            MoonSurfaceTextureFactory
                .createMoonSurfaceTexture();
    }

    private void createWorld() {

        player =
            new Player(
                GameConstants.PLAYER_START_X,
                GameConstants.PLAYER_START_Y,
                game.getAssets(),
                game.getSettings().getAstronautType()
            );

        boolean restoring = resumedMission != null && resumedStatus != null;

        playerStatus = restoring ? resumedStatus : new PlayerStatus();

        missionState = restoring ? resumedMission : new MissionState();

        lunarBase =
            new LunarBase(
                GameConstants.BASE_X,
                GameConstants.BASE_Y,
                game.getAssets()
            );

        createItems();

        createObstacles();

        createRepairStations();

        createEnemies();

        if (restoring) {
            game.getProgress().restoreWorld(items, enemies);
            player.setPosition(
                game.getProgress().getSavedPlayerX(GameConstants.PLAYER_START_X),
                game.getProgress().getSavedPlayerY(GameConstants.PLAYER_START_Y)
            );
            resumedMission = null;
            resumedStatus = null;
            resumedTime = 0f;
        }

        portal =
            new Portal(
                2380f,
                1190f,
                240f,
                300f,
                game.getAssets()
            );
    }

    private void createItems() {

        items =
            new Array<>();

        items.add(
            new CollectibleItem(
                ItemType.OXYGEN,
                1120f,
                880f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.OXYGEN,
                2640f,
                1260f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.OXYGEN,
                1920f,
                1470f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.FOOD,
                1810f,
                430f,
                game.getAssets()
            )
        );

        items.add(new CollectibleItem(ItemType.MEDKIT, 980f, 1040f, game.getAssets()));
        items.add(new CollectibleItem(ItemType.MEDKIT, 2480f, 560f, game.getAssets()));

        items.add(
            new CollectibleItem(
                ItemType.FOOD,
                710f,
                1430f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.FOOD,
                2770f,
                650f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.ICE_ROCK,
                2300f,
                690f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.ICE_ROCK,
                1080f,
                330f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.ICE_ROCK,
                2020f,
                1330f,
                game.getAssets()
            )
        );

        items.add(
            new CollectibleItem(
                ItemType.ICE_ROCK,
                2860f,
                970f,
                game.getAssets()
            )
        );

        addMissionItem(ItemType.ANTENNA_PART, 860f, 1360f);
        addMissionItem(ItemType.ENERGY_PART, 1510f, 350f);
        addMissionItem(ItemType.EXTRACTION_PART, 2710f, 1480f);
        addMissionItem(ItemType.GREENHOUSE_PART, 2920f, 520f);
        addMissionItem(ItemType.WEAPON_PART_A, 1220f, 1550f);
        addMissionItem(ItemType.WEAPON_PART_B, 2150f, 420f);
        addMissionItem(ItemType.WEAPON_PART_C, 2860f, 1120f);
    }

    private void addMissionItem(ItemType type, float x, float y) {
        items.add(new CollectibleItem(type, x, y, game.getAssets()));
    }

    private void createRepairStations() {
        repairStations = new Array<>();
        repairStations.add(new RepairStation(RepairType.COMMUNICATION, 350f, 845f, game.getAssets()));
        repairStations.add(new RepairStation(RepairType.ENERGY, 780f, 835f, game.getAssets()));
        repairStations.add(new RepairStation(RepairType.EXTRACTION, 350f, 365f, game.getAssets()));
        repairStations.add(new RepairStation(RepairType.GREENHOUSE, 790f, 365f, game.getAssets()));
    }

    private void createEnemies() {
        enemies = new Array<>();
        enemies.add(new Enemy(1320f, 720f, game.getAssets()));
        enemies.add(new Enemy(2060f, 1180f, game.getAssets()));
        enemies.add(new Enemy(2670f, 760f, game.getAssets()));
        enemies.add(new Enemy(1750f, 1510f, game.getAssets()));
    }

    private void createObstacles() {

        obstacles =
            new Array<>();

        addObstacle(
            930f,
            820f,
            145f,
            115f
        );

        addObstacle(
            1000f,
            620f,
            120f,
            95f
        );

        addObstacle(
            1280f,
            1110f,
            180f,
            145f
        );

        addObstacle(
            1490f,
            1240f,
            120f,
            100f
        );

        addObstacle(
            1680f,
            250f,
            150f,
            118f
        );

        addObstacle(
            1840f,
            1030f,
            115f,
            95f
        );

        addObstacle(
            2020f,
            840f,
            165f,
            130f
        );

        addObstacle(
            2200f,
            1080f,
            135f,
            110f
        );

        addObstacle(
            2570f,
            420f,
            190f,
            150f
        );

        addObstacle(
            2740f,
            1070f,
            145f,
            120f
        );

        addObstacle(
            420f,
            1260f,
            175f,
            140f
        );

        addObstacle(
            850f,
            1520f,
            125f,
            100f
        );

        addObstacle(
            1650f,
            1500f,
            165f,
            125f
        );
    }

    private void addObstacle(
        float x,
        float y,
        float width,
        float height
    ) {

        obstacles.add(
            new Obstacle(
                x,
                y,
                width,
                height,
                game.getAssets()
            )
        );
    }

    private void createSystems() {

        collectionSystem =
            new CollectionSystem();

        iceProcessor =
            new IceProcessor();

        survivalSystem =
            new SurvivalSystem(game.getSettings().getDifficulty());

        particleManager =
            new ParticleManager();

        obstacleSystem =
            new ObstacleSystem();

        missionSystem =
            new MissionSystem(game.getSettings().getDifficulty(), missionState);

        combatSystem = new CombatSystem(game.getAssets(), game.getSettings().getDifficulty());
    }

    private void createHud() {

        hud =
            new ModernHud(game.getAssets());
    }

    @Override
    public void render(
        float delta
    ) {

        if (changingScreen) {
            return;
        }

        float safeDelta =
            Math.min(
                delta,
                1f / 30f
            );

        handleGlobalInput();

        if (changingScreen) {
            return;
        }

        if (
            !paused
                &&
                !survivalSystem
                    .isMissionFailed()
        ) {

            updateGame(
                safeDelta
            );
        }

        if (changingScreen) {
            return;
        }

        clearScreen();

        renderMoonSurface();

        renderWorld();

        renderParticles();

        renderPlayer();

        renderHud();

        if (paused) {

            hud.renderPauseText(
                batch
            );
        }

        if (
            survivalSystem
                .isMissionFailed()
        ) {

            hud.renderGameOverText(
                batch
            );
        }
    }

    private void handleGlobalInput() {

        if (
            survivalSystem
                .isMissionFailed()
        ) {

            if (
                !defeatSoundPlayed
            ) {

                audio.playDefeat();

                defeatSoundPlayed = true;
            }

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.R
                )
            ) {

                restartMission();

                return;
            }

            if (
                Gdx.input.isKeyJustPressed(
                    Input.Keys.M
                )
            ) {

                returnToMenu();

                return;
            }

            return;
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            )
        ) {

            paused =
                !paused;

            if (paused) {
                audio.pauseAmbientMusic();
                audio.stopPortalLoop();
            } else {
                audio.resumeAmbientMusic();
            }
        }

        if (
            paused
                &&
                Gdx.input.isKeyJustPressed(
                    Input.Keys.M
                )
        ) {

            returnToMenu();
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.F1
            )
        ) {

            audio.toggleMute();
            game.getSettings().setMuted(audio.isMuted());
        }
    }

    private void updateGame(
        float delta
    ) {

        missionTime += delta;

        player.update(
            delta,
            playerStatus
        );

        obstacleSystem
            .handleCollisions(
                player,
                obstacles,
                audio
            );

        for (
            CollectibleItem item : items
        ) {

            item.update(
                delta
            );
        }

        portal.update(
            delta
        );

        boolean insideBase =
            lunarBase
                .isPlayerInside(
                    player
                );

        lunarBase.update(
            delta,
            player,
            playerStatus
        );

        collectionSystem.update(
            player,
            playerStatus,
            items,
            particleManager,
            audio,
            missionState
        );

        updateMissionInteractions(delta, insideBase);

        iceProcessor.update(
            delta,
            player,
            lunarBase,
            playerStatus,
            particleManager,
            audio
        );

        survivalSystem.update(
            delta,
            player,
            lunarBase,
            playerStatus
        );

        updateAimPosition();
        boolean firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (missionState.hasWeapon()) player.setFacingTowards(aimWorld.x);

        combatSystem.update(
            delta,
            player,
            playerStatus,
            missionState,
            enemies,
            particleManager,
            audio,
            aimWorld.x,
            aimWorld.y,
            firing
        );

        if (
            survivalSystem
                .isMissionFailed()
        ) {

            return;
        }

        missionSystem.update(
            player,
            playerStatus,
            portal,
            particleManager,
            audio
        );

        particleManager.update(
            delta,
            player,
            insideBase,
            playerStatus.getOxygen()
                < GameConstants.CRITICAL_OXYGEN_THRESHOLD,
            portal
        );

        audio.update(
            delta,
            player.isMoving(),
            insideBase,
            playerStatus.getOxygen()
                < GameConstants.CRITICAL_OXYGEN_THRESHOLD,
            portal.isActive(),
            getPortalProximity(),
            getPortalPan()
        );

        autosaveTimer += delta;
        if (autosaveTimer >= 1f) {
            autosaveTimer = 0f;
            game.getProgress().saveMission(missionState, playerStatus, "LUA",
                missionTime, countCollectedItems(), player, items, enemies);
        }

        updateCamera(
            delta
        );

        if (
            missionSystem
                .isMissionComplete()
        ) {

            goToMarsScreen();
        }
    }

    private void updateAimPosition() {
        aimWorld.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(aimWorld);
    }

    private void updateMissionInteractions(float delta, boolean insideBase) {
        nearbyStation = null;
        for (RepairStation station : repairStations) {
            station.update(delta);
            if (station.isPlayerNear(player)) nearbyStation = station;
        }

        if (nearbyStation != null && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (missionState.repair(nearbyStation.getType())) {
                particleManager.emitProcessingBurst(nearbyStation.getCenterX(), nearbyStation.getCenterY());
                audio.playRepair();
            }
        }

        if (insideBase && Gdx.input.isKeyJustPressed(Input.Keys.C)
            && missionState.craftWeapon()) {
            player.triggerCraftAnimation();
            particleManager.emitProcessingBurst(lunarBase.getCenterX(), lunarBase.getCenterY());
            audio.playCraft();
        }
    }

    private float getPortalProximity() {
        float distance = Vector2.dst(
            player.getCenterX(),
            player.getCenterY(),
            portal.getCenterX(),
            portal.getCenterY()
        );
        return 1f - MathUtils.clamp(distance / 1100f, 0f, 1f);
    }

    private float getPortalPan() {
        return MathUtils.clamp(
            (portal.getCenterX() - player.getCenterX()) / 700f,
            -1f,
            1f
        );
    }

    private void goToMarsScreen() {

        if (changingScreen) {
            return;
        }

        changingScreen = true;

        audio.stopGameplayAudio();

        game.changeScreen(
            new MarsScreen(
                game,
                missionState,
                playerStatus,
                missionTime,
                countCollectedItems()
            )
        );
    }

    private int countCollectedItems() {

        int count = 0;

        for (
            CollectibleItem item : items
        ) {

            if (
                item.isCollected()
            ) {

                count++;
            }
        }

        return count;
    }

    private void returnToMenu() {

        if (changingScreen) {
            return;
        }

        changingScreen = true;

        audio.stopGameplayAudio();

        game.changeScreen(
            new MenuScreen(
                game
            )
        );
    }

    private void restartMission() {

        audio.stopGameplayAudio();

        if (
            particleManager != null
        ) {

            particleManager.clear();
        }

        createWorld();

        createSystems();

        missionTime = 0f;

        paused = false;

        defeatSoundPlayed = false;

        changingScreen = false;

        positionCameraImmediately();

        audio.playAmbientMusic();
    }

    private void positionCameraImmediately() {

        camera.position.set(
            player.getCenterX(),
            player.getCenterY(),
            0f
        );

        limitCameraToWorld();

        camera.update();
    }

    private void updateCamera(
        float delta
    ) {

        float halfWidth =
            viewport.getWorldWidth()
                / 2f;

        float halfHeight =
            viewport.getWorldHeight()
                / 2f;

        float targetX =
            MathUtils.clamp(
                player.getCenterX(),
                halfWidth,
                GameConstants.WORLD_WIDTH
                    - halfWidth
            );

        float targetY =
            MathUtils.clamp(
                player.getCenterY(),
                halfHeight,
                GameConstants.WORLD_HEIGHT
                    - halfHeight
            );

        float smoothing =
            1f
                -
                (float) Math.pow(
                    0.001f,
                    delta
                );

        camera.position.x =
            MathUtils.lerp(
                camera.position.x,
                targetX,
                smoothing
            );

        camera.position.y =
            MathUtils.lerp(
                camera.position.y,
                targetY,
                smoothing
            );

        limitCameraToWorld();

        camera.update();
    }

    private void limitCameraToWorld() {

        float halfWidth =
            viewport.getWorldWidth()
                / 2f;

        float halfHeight =
            viewport.getWorldHeight()
                / 2f;

        camera.position.x =
            MathUtils.clamp(
                camera.position.x,
                halfWidth,
                GameConstants.WORLD_WIDTH
                    - halfWidth
            );

        camera.position.y =
            MathUtils.clamp(
                camera.position.y,
                halfHeight,
                GameConstants.WORLD_HEIGHT
                    - halfHeight
            );
    }

    private void clearScreen() {

        Gdx.gl.glClearColor(
            0.01f,
            0.014f,
            0.02f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );
    }

    private void renderMoonSurface() {

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        final int tileSize =
            MoonSurfaceTextureFactory.TEXTURE_SIZE;

        float left =
            camera.position.x
                -
                viewport.getWorldWidth()
                    / 2f;

        float bottom =
            camera.position.y
                -
                viewport.getWorldHeight()
                    / 2f;

        float right =
            camera.position.x
                +
                viewport.getWorldWidth()
                    / 2f;

        float top =
            camera.position.y
                +
                viewport.getWorldHeight()
                    / 2f;

        int startX =
            Math.max(
                0,
                (int) Math.floor(
                    left / tileSize
                )
                    * tileSize
                    - tileSize
            );

        int startY =
            Math.max(
                0,
                (int) Math.floor(
                    bottom / tileSize
                )
                    * tileSize
                    - tileSize
            );

        int endX =
            Math.min(
                (int)
                    GameConstants.WORLD_WIDTH,
                (int) Math.ceil(
                    right / tileSize
                )
                    * tileSize
                    + tileSize
            );

        int endY =
            Math.min(
                (int)
                    GameConstants.WORLD_HEIGHT,
                (int) Math.ceil(
                    top / tileSize
                )
                    * tileSize
                    + tileSize
            );

        for (
            int x = startX;
            x <= endX;
            x += tileSize
        ) {

            for (
                int y = startY;
                y <= endY;
                y += tileSize
            ) {

                int tileX = x / tileSize;
                int tileY = y / tileSize;
                batch.draw(
                    moonSurfaceTexture,
                    x,
                    y,
                    tileSize,
                    tileSize,
                    0,
                    0,
                    MoonSurfaceTextureFactory.TEXTURE_SIZE,
                    MoonSurfaceTextureFactory.TEXTURE_SIZE,
                    (tileX & 1) == 1,
                    (tileY & 1) == 1
                );
            }
        }

        batch.end();
    }

    private void renderWorld() {

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        for (
            Obstacle obstacle : obstacles
        ) {

            obstacle.render(
                batch
            );
        }

        lunarBase.render(
            batch,
            lunarBase
                .isPlayerInside(
                    player
                )
        );

        for (RepairStation station : repairStations) {
            station.render(batch, missionState.isRepaired(station.getType()));
        }

        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        combatSystem.render(batch);

        for (
            CollectibleItem item : items
        ) {

            item.render(
                batch
            );
        }

        portal.render(
            batch
        );

        batch.end();
    }

    private void renderParticles() {

        Gdx.gl.glEnable(
            GL20.GL_BLEND
        );

        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer
            .setProjectionMatrix(
                camera.combined
            );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        for (RepairStation station : repairStations) {
            station.renderStatus(
                shapeRenderer,
                missionState.isRepaired(station.getType()),
                station == nearbyStation
            );
        }

        for (Enemy enemy : enemies) {
            enemy.renderStatus(shapeRenderer);
        }

        particleManager.render(
            shapeRenderer
        );

        shapeRenderer.end();

        Gdx.gl.glDisable(
            GL20.GL_BLEND
        );
    }

    private void renderPlayer() {

        batch.setProjectionMatrix(
            camera.combined
        );

        batch.begin();

        player.render(
            batch
        );

        if (missionState.hasWeapon() && !player.isFiringAnimation()) {
            player.renderWeapon(batch, aimWorld.x, aimWorld.y);
        }

        batch.end();
    }

    private void renderHud() {

        hud.render(
            batch,
            playerStatus,
            player,
            lunarBase,
            iceProcessor,
            missionSystem,
            portal,
            missionTime,
            paused,
            survivalSystem
                .isMissionFailed(),
            nearbyStation,
            portal.isPlayerNear(player)
        );
    }

    @Override
    public void resize(
        int width,
        int height
    ) {

        if (
            viewport != null
        ) {

            viewport.update(
                width,
                height,
                false
            );
        }

        if (
            hud != null
        ) {

            hud.resize(
                width,
                height
            );
        }
    }

    @Override
    public void pause() {
        if (!changingScreen && survivalSystem != null
            && !survivalSystem.isMissionFailed()) {
            paused = true;
            audio.pauseAmbientMusic();
            audio.stopPortalLoop();
        }
    }

    @Override
    public void dispose() {

        if (
            moonSurfaceTexture != null
        ) {

            moonSurfaceTexture.dispose();

            moonSurfaceTexture = null;
        }

        if (
            shapeRenderer != null
        ) {

            shapeRenderer.dispose();

            shapeRenderer = null;
        }

        if (
            hud != null
        ) {

            hud.dispose();

            hud = null;
        }
    }
}
