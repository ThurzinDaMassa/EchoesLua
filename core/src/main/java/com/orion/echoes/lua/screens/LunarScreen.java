package com.orion.echoes.lua.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
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
import com.orion.echoes.lua.systems.MissionSystem;
import com.orion.echoes.lua.systems.ObstacleSystem;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.systems.SurvivalSystem;
import com.orion.echoes.lua.ui.ModernHud;
import com.orion.echoes.lua.ui.MissionProtocolOverlay;
import com.orion.echoes.lua.utils.GameConstants;

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

    private SurvivalSystem survivalSystem;

    private ParticleManager particleManager;

    private ObstacleSystem obstacleSystem;

    private MissionSystem missionSystem;

    private MissionState missionState;

    private CombatSystem combatSystem;

    private final Vector2 aimWorld = new Vector2();

    private RepairStation nearbyStation;

    private ModernHud hud;
    private MissionProtocolOverlay protocolOverlay;
    private RepairStation activeRepairStation;
    private float repairSparkTimer;
    private float damageFlashTimer;

    private boolean changingScreen;

    private boolean paused;

    private boolean defeatSoundPlayed;

    private float missionTime;

    private float autosaveTimer;

    private MissionState resumedMission;

    private PlayerStatus resumedStatus;

    private float resumedTime;

    private final float forcedStartX;

    private final float forcedStartY;

    private final boolean showProtocolIntro;

    public LunarScreen(
        LunarEchoesGame game
    ) {

        this(game, null, null, 0f, Float.NaN, Float.NaN, true);
    }

    public LunarScreen(
        LunarEchoesGame game,
        MissionState resumedMission,
        PlayerStatus resumedStatus,
        float resumedTime
    ) {

        this(game, resumedMission, resumedStatus, resumedTime, Float.NaN, Float.NaN, false);
    }

    public LunarScreen(
        LunarEchoesGame game,
        MissionState resumedMission,
        PlayerStatus resumedStatus,
        float resumedTime,
        float forcedStartX,
        float forcedStartY
    ) {

        this(game, resumedMission, resumedStatus, resumedTime,
            forcedStartX, forcedStartY, false);
    }

    private LunarScreen(
        LunarEchoesGame game,
        MissionState resumedMission,
        PlayerStatus resumedStatus,
        float resumedTime,
        float forcedStartX,
        float forcedStartY,
        boolean showProtocolIntro
    ) {

        this.game = game;

        this.batch =
            game.getBatch();

        this.audio =
            game.getAudio();

        this.resumedMission = resumedMission;
        this.resumedStatus = resumedStatus;
        this.resumedTime = resumedTime;
        this.forcedStartX = forcedStartX;
        this.forcedStartY = forcedStartY;
        this.showProtocolIntro = showProtocolIntro;
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

        protocolOverlay = showProtocolIntro
            ? new MissionProtocolOverlay(game.getAssets()) : null;

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

        moonSurfaceTexture = game.getAssets().getLunarSurface();
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

        createObstacles();

        createRepairStations();

        createEnemies();

        portal =
            new Portal(
                2380f,
                1190f,
                240f,
                300f,
                game.getAssets()
            );

        createItems();

        if (restoring) {
            game.getProgress().restoreWorld(items, enemies);
            player.setPosition(
                game.getProgress().getSavedPlayerX(GameConstants.PLAYER_START_X),
                game.getProgress().getSavedPlayerY(GameConstants.PLAYER_START_Y)
            );
            if (!Float.isNaN(forcedStartX) && !Float.isNaN(forcedStartY)) {
                player.setPosition(forcedStartX, forcedStartY);
            }
            resumedMission = null;
            resumedStatus = null;
            resumedTime = 0f;
        }

    }

    private void createItems() {

        items = new Array<>();
        RandomXS128 random = new RandomXS128(missionState.getWorldSeed());
        ItemType[] distribution = {
            ItemType.OXYGEN, ItemType.OXYGEN, ItemType.OXYGEN,
            ItemType.FOOD, ItemType.FOOD, ItemType.FOOD,
            ItemType.MEDKIT, ItemType.MEDKIT,
            ItemType.ICE_ROCK, ItemType.ICE_ROCK, ItemType.ICE_ROCK, ItemType.ICE_ROCK,
            ItemType.ANTENNA_PART, ItemType.ENERGY_PART,
            ItemType.EXTRACTION_PART, ItemType.GREENHOUSE_PART,
            ItemType.WEAPON_PART_A, ItemType.WEAPON_PART_B, ItemType.WEAPON_PART_C
        };
        for (int i = 0; i < distribution.length; i++) {
            Vector2 spawn = findSafeItemSpawn(random, i);
            items.add(new CollectibleItem(distribution[i], spawn.x, spawn.y, game.getAssets()));
        }
    }

    private Vector2 findSafeItemSpawn(RandomXS128 random, int index) {
        Rectangle candidate = new Rectangle(0f, 0f, 76f, 76f);
        for (int attempt = 0; attempt < 260; attempt++) {
            candidate.x = 150f + random.nextFloat() * (GameConstants.WORLD_WIDTH - 376f);
            candidate.y = 140f + random.nextFloat() * (GameConstants.WORLD_HEIGHT - 330f);
            if (isSafeItemArea(candidate)) return new Vector2(candidate.x, candidate.y);
        }
        for (float y = 180f; y < GameConstants.WORLD_HEIGHT - 180f; y += 130f) {
            for (float x = 180f + index * 17f % 90f;
                 x < GameConstants.WORLD_WIDTH - 180f; x += 150f) {
                candidate.setPosition(x, y);
                if (isSafeItemArea(candidate)) return new Vector2(x, y);
            }
        }
        return new Vector2(180f, 180f);
    }

    private boolean isSafeItemArea(Rectangle candidate) {
        Rectangle expanded = new Rectangle(candidate);
        expanded.x -= 44f;
        expanded.y -= 44f;
        expanded.width += 88f;
        expanded.height += 88f;

        Rectangle baseArea = new Rectangle(GameConstants.BASE_X - 100f,
            GameConstants.BASE_Y - 100f, GameConstants.BASE_WIDTH + 200f,
            GameConstants.BASE_HEIGHT + 200f);
        if (expanded.overlaps(baseArea) || expanded.overlaps(expand(portal.getBounds(), 90f))) return false;
        Rectangle playerStart = new Rectangle(GameConstants.PLAYER_START_X - 100f,
            GameConstants.PLAYER_START_Y - 100f, 250f, 250f);
        if (expanded.overlaps(playerStart)) return false;
        for (Obstacle obstacle : obstacles) {
            if (expanded.overlaps(expand(obstacle.getBounds(), 36f))) return false;
        }
        for (RepairStation station : repairStations) {
            if (expanded.overlaps(expand(station.getBounds(), 55f))) return false;
        }
        for (Enemy enemy : enemies) {
            if (expanded.overlaps(expand(enemy.getBounds(), 80f))) return false;
        }
        for (CollectibleItem item : items) {
            if (expanded.overlaps(expand(item.getBounds(), 28f))) return false;
        }
        return true;
    }

    private Rectangle expand(Rectangle source, float margin) {
        return new Rectangle(source.x - margin, source.y - margin,
            source.width + margin * 2f, source.height + margin * 2f);
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

        if (protocolOverlay != null && protocolOverlay.isActive()) {
            clearScreen();
            protocolOverlay.update(safeDelta);
            if (protocolOverlay.isActive()) {
                protocolOverlay.render(batch);
                return;
            }
        }

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

        renderItemGuidance();

        renderWorld();

        renderParticles();

        renderPlayer();

        renderHud();

        hud.renderDamageIndicator(damageFlashTimer);

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

        damageFlashTimer = Math.max(0f, damageFlashTimer - delta);

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

        collectionSystem.update(
            player,
            playerStatus,
            items,
            particleManager,
            audio,
            missionState
        );

        updateMissionInteractions(delta);

        if (changingScreen) {
            return;
        }

        survivalSystem.updateExposed(delta, playerStatus);

        updateAimPosition();
        boolean firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (missionState.hasWeapon() && (firing || !player.isMoving())) {
            player.setFacingTowards(aimWorld.x);
        }

        boolean playerDamaged = combatSystem.update(
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

        if (playerDamaged) {
            damageFlashTimer = 0.46f;
            player.triggerDamageFlash();
            particleManager.emitDamageBurst(player.getCenterX(), player.getCenterY());
            audio.playPlayerDamage();
        }

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
            false,
            playerStatus.getOxygen()
                < GameConstants.CRITICAL_OXYGEN_THRESHOLD,
            portal
        );

        audio.update(
            delta,
            player.isMoving(),
            false,
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

    private void updateMissionInteractions(float delta) {
        if (lunarBase.isPlayerNearEntrance(player)
            && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            goToBaseInterior();
            return;
        }

        nearbyStation = null;
        for (RepairStation station : repairStations) {
            station.update(delta);
            if (station.isPlayerNear(player)) nearbyStation = station;
        }

        if (activeRepairStation != null) {
            if (!activeRepairStation.isPlayerNear(player)) {
                activeRepairStation.cancelRepair();
                activeRepairStation = null;
                missionState.notifyAction("Reparo cancelado // aproxime-se novamente");
                return;
            }
            repairSparkTimer -= delta;
            if (repairSparkTimer <= 0f) {
                particleManager.emitRepairSparks(activeRepairStation.getCenterX(),
                    activeRepairStation.getCenterY() + 18f, false);
                player.triggerCraftAnimation();
                repairSparkTimer = 0.14f;
            }
            if (activeRepairStation.isRepairComplete()) {
                RepairStation completed = activeRepairStation;
                if (missionState.repair(completed.getType())) {
                    particleManager.emitProcessingBurst(completed.getCenterX(), completed.getCenterY());
                    audio.playRepair();
                }
                completed.cancelRepair();
                activeRepairStation = null;
            }
            return;
        }

        if (nearbyStation != null && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (missionState.isRepaired(nearbyStation.getType())
                || missionState.getCount(nearbyStation.getType().getRequiredPart()) <= 0) {
                missionState.repair(nearbyStation.getType());
            } else {
                activeRepairStation = nearbyStation;
                activeRepairStation.startRepair();
                repairSparkTimer = 0f;
                player.triggerCraftAnimation();
                missionState.notifyAction("Reparando " + nearbyStation.getType().getLabel()
                    + " // mantenha-se proximo");
            }
        }

    }

    private void goToBaseInterior() {
        if (changingScreen) return;
        changingScreen = true;
        int collected = countCollectedItems();
        game.getProgress().saveMission(missionState, playerStatus, "LUA",
            missionTime, collected, player, items, enemies);
        audio.stopPortalLoop();
        game.changeScreen(new BaseInteriorScreen(
            game, missionState, playerStatus, missionTime, collected));
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
            moonSurfaceTexture.getWidth();

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
                    moonSurfaceTexture.getWidth(),
                    moonSurfaceTexture.getHeight(),
                    (tileX & 1) == 1,
                    (tileY & 1) == 1
                );
            }
        }

        batch.end();
    }

    private void renderItemGuidance() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        ItemType requested = missionState.getRequestedItem();
        CollectibleItem target = null;
        for (CollectibleItem item : items) {
            boolean missionTarget = !item.isCollected() && item.getType() == requested;
            item.renderGlow(shapeRenderer, missionTarget);
            if (missionTarget && target == null) target = item;
        }
        if (target != null) {
            drawTargetArrow(target.getCenterX(), target.getCenterY(),
                target.getBounds().height * 0.72f + 17f);
        } else if (requested != null && missionState.getCount(requested) > 0) {
            for (RepairStation station : repairStations) {
                if (!missionState.isRepaired(station.getType())
                    && station.getType().getRequiredPart() == requested) {
                    drawTargetArrow(station.getCenterX(), station.getCenterY(), 76f);
                    break;
                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawTargetArrow(float targetX, float targetY, float visibleOffset) {
        float margin = 58f;
        float left = camera.position.x - viewport.getWorldWidth() / 2f + margin;
        float right = camera.position.x + viewport.getWorldWidth() / 2f - margin;
        float bottom = camera.position.y - viewport.getWorldHeight() / 2f + margin;
        float top = camera.position.y + viewport.getWorldHeight() / 2f - margin;
        boolean visible = targetX >= left && targetX <= right && targetY >= bottom && targetY <= top;

        float pulse = 1f + MathUtils.sin(missionTime * 5f) * 0.12f;
        shapeRenderer.setColor(new Color(0.18f, 0.96f, 1f, 0.92f));
        if (visible) {
            float y = targetY + visibleOffset;
            shapeRenderer.triangle(targetX, y - 13f * pulse,
                targetX - 10f * pulse, y + 3f * pulse,
                targetX + 10f * pulse, y + 3f * pulse);
            return;
        }

        float indicatorX = MathUtils.clamp(targetX, left, right);
        float indicatorY = MathUtils.clamp(targetY, bottom, top);
        float angle = MathUtils.atan2(targetY - camera.position.y,
            targetX - camera.position.x);
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float sideX = -sin * 9f * pulse;
        float sideY = cos * 9f * pulse;
        shapeRenderer.triangle(
            indicatorX + cos * 15f * pulse, indicatorY + sin * 15f * pulse,
            indicatorX - cos * 7f * pulse + sideX, indicatorY - sin * 7f * pulse + sideY,
            indicatorX - cos * 7f * pulse - sideX, indicatorY - sin * 7f * pulse - sideY);
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
            lunarBase.isPlayerNearEntrance(player)
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

        if (missionState.hasWeapon()) {
            player.renderWeapon(batch, aimWorld.x, aimWorld.y);
        }

        batch.end();
    }

    private void renderHud() {

        hud.render(
            batch,
            playerStatus,
            player,
            lunarBase.isPlayerNearEntrance(player),
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

        if (protocolOverlay != null) protocolOverlay.resize(width, height);
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

        if (protocolOverlay != null) {
            protocolOverlay.dispose();
            protocolOverlay = null;
        }
    }
}
