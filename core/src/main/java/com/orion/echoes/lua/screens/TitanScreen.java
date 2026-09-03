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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.orion.echoes.lua.LunarEchoesGame;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.effects.EnemyDeathAnimation;
import com.orion.echoes.lua.entities.BossProjectile;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.entities.LootChest;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.entities.TitanEnemy;
import com.orion.echoes.lua.entities.TitanMinion;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.CollectionSystem;
import com.orion.echoes.lua.systems.CombatSystem;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.systems.SurvivalSystem;
import com.orion.echoes.lua.ui.InventoryOverlay;
import com.orion.echoes.lua.ui.UiFonts;
import com.orion.echoes.lua.ui.UiTheme;
import com.orion.echoes.lua.utils.GameConstants;

/** Terceiro destino completo: Tita, com atmosfera, ameaca e retorno persistente. */
public final class TitanScreen extends ScreenAdapter {
    private static final Rectangle RESUME = new Rectangle(382f, 264f, 244f, 58f);
    private static final Rectangle MENU = new Rectangle(654f, 264f, 244f, 58f);
    private final LunarEchoesGame game;
    private final MissionState mission;
    private final PlayerStatus status;
    private final float previousMissionTime;
    private final int collectedItems;
    private final Vector2 aim = new Vector2();
    private final Vector2 pointer = new Vector2();
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;
    private Viewport viewport;
    private UiFonts fonts;
    private Texture surface;
    private Texture oxygenBaseTexture;
    private Player player;
    private Portal returnPortal;
    private TitanEnemy boss;
    private Array<Enemy> enemies;
    private Array<CollectibleItem> items;
    private Array<LootChest> lootChests;
    private LootChest nearbyChest;
    private CollectibleItem titanCoreDrop;
    private final Array<BossProjectile> bossProjectiles = new Array<>();
    private CombatSystem combat;
    private CollectionSystem collection;
    private SurvivalSystem survival;
    private ParticleManager particles;
    private EnemyDeathAnimation deathAnimations;
    private InventoryOverlay inventory;
    private final Rectangle oxygenBaseBounds = new Rectangle(626f, 500f, 310f, 190f);
    private float time;
    private float autosave;
    private float damageFlash;
    private float bossAttackTimer = 1.8f;
    private int bossAttackPattern;
    private boolean paused;
    private boolean failed;
    private boolean changing;

    public TitanScreen(LunarEchoesGame game, MissionState mission, PlayerStatus status,
                       float previousMissionTime, int collectedItems) {
        this(game, mission, status, previousMissionTime, collectedItems, 520f, 660f);
    }

    public TitanScreen(LunarEchoesGame game, MissionState mission, PlayerStatus status,
                       float previousMissionTime, int collectedItems, float startX, float startY) {
        this.game = game;
        this.mission = mission;
        this.status = status;
        this.previousMissionTime = previousMissionTime;
        this.collectedItems = collectedItems;
        create(startX, startY);
    }

    private void create(float startX, float startY) {
        batch = game.getBatch();
        shapes = new ShapeRenderer();
        fonts = new UiFonts();
        camera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        viewport = new FitViewport(GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT, camera);
        surface = game.getAssets().getTitanSurface();
        oxygenBaseTexture = game.getAssets().getTitanOxygenBase();
        player = new Player(startX, startY, game.getAssets(), game.getSettings().getAstronautType());
        returnPortal = new Portal(300f, 530f, 220f, 272f, game.getAssets().getTitanPortal());
        returnPortal.activate();
        enemies = new Array<>();
        boss = new TitanEnemy(1010f, 790f, game.getAssets());
        enemies.add(boss);
        enemies.add(new TitanMinion(1480f, 980f, game.getAssets()));
        enemies.add(new TitanMinion(1780f, 520f, game.getAssets()));
        enemies.add(new TitanMinion(2260f, 1120f, game.getAssets()));
        enemies.add(new TitanMinion(2820f, 680f, game.getAssets()));
        enemies.add(new TitanMinion(3210f, 1510f, game.getAssets()));
        items = new Array<>();
        addTitanItems();
        createTitanChests();
        combat = new CombatSystem(game.getAssets(), game.getSettings().getDifficulty());
        collection = new CollectionSystem();
        survival = new SurvivalSystem(game.getSettings().getDifficulty());
        particles = new ParticleManager();
        deathAnimations = new EnemyDeathAnimation();
        inventory = new InventoryOverlay(game.getAssets());
        game.getProgress().restoreWorld("TITA", items, enemies);
        if (mission.isTitanEnemyDefeated()) {
            boss.defeat();
        } else if (!boss.isAlive()) {
            boss = new TitanEnemy(1010f, 790f, game.getAssets());
            enemies.set(0, boss);
        }
        if (mission.isTitanEnemyDefeated() && mission.getCount(ItemType.TITAN_CORE) == 0
            && !mission.isTitanCoreInstalled()) spawnTitanCore();
        camera.position.set(player.getCenterX(), player.getCenterY(), 0f);
        camera.update();
        mission.markEnteredTitan();
        save();
        game.getAudio().playTitanAmbient();
    }

    @Override public void show() { }

    @Override public void render(float delta) {
        if (changing) return;
        float dt = Math.min(delta, 1f / 30f);
        handleInput();
        if (!paused && !failed && !inventory.isOpen()) update(dt);
        inventory.update(mission);
        drawWorld();
        drawHud();
        inventory.render(batch, mission);
        if (paused || failed) drawModal();
    }

    private void update(float dt) {
        player.update(dt, status);
        returnPortal.update(dt);
        aim.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(aim);
        boolean firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (mission.hasWeapon() && (firing || !player.isMoving())) player.setFacingTowards(aim.x);
        for (CollectibleItem item : items) item.update(dt);
        collection.update(player, status, items, particles, game.getAudio(), mission);
        boolean insideBase = oxygenBaseBounds.overlaps(player.getBounds());
        float healthBeforeCombat = status.getHealth();
        boolean damaged = combat.update(dt, player, status, mission, enemies, items, particles,
            game.getAudio(), aim.x, aim.y, firing);
        updateBossAttacks(dt, insideBase);
        for (Enemy enemy : enemies) {
            if (enemy.consumeDeathAnimation()) {
                deathAnimations.emit(enemy);
                particles.emitProjectileImpact(enemy.getCenterX(), enemy.getCenterY());
            }
        }
        deathAnimations.update(dt);
        if (insideBase && damaged) {
            status.setHealth(healthBeforeCombat);
            damaged = false;
        }
        if (!mission.isTitanEnemyDefeated() && !boss.isAlive()) {
            mission.recordTitanEnemyDefeated();
            spawnTitanCore();
            bossProjectiles.clear();
        }
        updateTitanChests(dt);
        if (damaged) {
            damageFlash = 0.46f;
            player.triggerDamageFlash();
            particles.emitDamageBurst(player.getCenterX(), player.getCenterY());
            game.getAudio().playPlayerDamage();
        }
        if (insideBase) {
            status.addOxygen(24f * dt);
            status.addEnergy(16f * dt);
        } else {
            survival.updateExposed(dt, status);
        }
        if (survival.isMissionFailed()) failed = true;
        particles.update(dt, player, insideBase, status.getOxygen() < 25f, null);
        game.getAudio().update(dt, player.isMoving(), insideBase, status.getOxygen() < 25f,
            false, 0f, 0f);
        damageFlash = Math.max(0f, damageFlash - dt);
        time += dt;
        autosave += dt;
        if (autosave >= 1f) { autosave = 0f; save(); }
        player.setPosition(MathUtils.clamp(player.getX(), 40f, GameConstants.WORLD_WIDTH - 90f),
            MathUtils.clamp(player.getY(), 40f, GameConstants.WORLD_HEIGHT - 110f));
        camera.position.x = MathUtils.clamp(MathUtils.lerp(camera.position.x,
            player.getCenterX(), 0.10f), GameConstants.VIRTUAL_WIDTH / 2f,
            GameConstants.WORLD_WIDTH - GameConstants.VIRTUAL_WIDTH / 2f);
        camera.position.y = MathUtils.clamp(MathUtils.lerp(camera.position.y,
            player.getCenterY(), 0.10f), GameConstants.VIRTUAL_HEIGHT / 2f,
            GameConstants.WORLD_HEIGHT - GameConstants.VIRTUAL_HEIGHT / 2f);
        camera.update();
    }

    private void handleInput() {
        if (paused || failed) {
            if (Gdx.input.justTouched()) {
                toHud(pointer);
                if (RESUME.contains(pointer) && !failed) paused = false;
                else if (RESUME.contains(pointer) && failed) returnToMars();
                else if (MENU.contains(pointer)) { changing = true; game.changeScreen(new MenuScreen(game)); }
            }
            return;
        }
        boolean e = Gdx.input.isKeyJustPressed(Input.Keys.E);
        if (inventory.isOpen() && (e || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            inventory.close(); return;
        }
        if (e && returnPortal.isPlayerNear(player)) {
            if (!mission.isTitanEnemyDefeated()) {
                mission.notifyAction("PORTAL BLOQUEADO // derrote o Predador de Metano");
            } else if (mission.getCount(ItemType.TITAN_CORE) == 0) {
                mission.notifyAction("PORTAL BLOQUEADO // colete o Nucleo de Tita");
            } else {
                returnToMars();
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && nearbyChest != null
            && !nearbyChest.isOpened()) {
            nearbyChest.markOpened();
            mission.openTitanChest(nearbyChest.getIndex());
            nearbyChest.spawnLoot(items, game.getAssets());
            particles.emitPickupBurst(ItemType.QUANTUM_CORE,
                nearbyChest.getCenterX(), nearbyChest.getCenterY());
            game.getAudio().playChestOpen();
            mission.notifyAction("BAU CRIOGENICO ABERTO // suprimentos liberados");
            return;
        }
        if (e) { inventory.toggle(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) combat.requestReload(mission, game.getAudio());
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) paused = true;
    }

    private void drawWorld() {
        Gdx.gl.glClearColor(0.08f, 0.035f, 0.008f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float tile = 640f;
        for (int x = 0; x < GameConstants.WORLD_WIDTH; x += (int)tile)
            for (int y = 0; y < GameConstants.WORLD_HEIGHT; y += (int)tile)
                batch.draw(surface, x, y, tile, tile);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.92f, 0.42f, 0.06f, 0.16f);
        shapes.ellipse(1560f, 260f, 620f, 210f, 48);
        shapes.ellipse(2690f, 1220f, 760f, 250f, 48);
        for (LootChest chest : lootChests) chest.renderGlow(shapes, chest == nearbyChest);
        for (CollectibleItem item : items) item.renderGlow(shapes, item.getType() == ItemType.METHANE_SAMPLE);
        shapes.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        returnPortal.render(batch);
        batch.draw(oxygenBaseTexture, 555f, 400f, 510f, 340f);
        for (LootChest chest : lootChests) chest.render(batch);
        for (CollectibleItem item : items) item.render(batch);
        for (Enemy enemy : enemies) enemy.render(batch);
        combat.render(batch);
        for (BossProjectile projectile : bossProjectiles) projectile.render(batch);
        deathAnimations.render(batch);
        player.render(batch);
        if (mission.hasWeapon()) player.renderWeapon(batch, aim.x, aim.y);
        batch.end();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) enemy.renderStatus(shapes);
        particles.render(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawHud() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Color amber = new Color(1f, 0.48f, 0.08f, 1f);
        UiTheme.panel(shapes, 24f, 634f, 290f, 62f, amber);
        UiTheme.panel(shapes, 332f, 634f, 604f, 62f, amber);
        UiTheme.panel(shapes, 954f, 610f, 302f, 86f, UiTheme.CYAN_SOFT);
        UiTheme.panel(shapes, 24f, 22f, 650f, 54f, amber);
        if (returnPortal.isPlayerNear(player)) {
            UiTheme.panel(shapes, 692f, 22f, 340f, 54f, UiTheme.GREEN);
        } else if (nearbyChest != null && !nearbyChest.isOpened()) {
            UiTheme.panel(shapes, 692f, 22f, 340f, 54f, UiTheme.WARNING);
        } else if (oxygenBaseBounds.overlaps(player.getBounds())) {
            UiTheme.panel(shapes, 692f, 22f, 340f, 54f, UiTheme.CYAN);
        }
        UiTheme.panel(shapes, 1050f, 22f, 206f, 54f, UiTheme.CYAN_SOFT);
        UiTheme.bar(shapes, 1034f, 671f, 150f, 5f, status.getOxygen() / 100f, UiTheme.CYAN);
        UiTheme.bar(shapes, 1034f, 650f, 150f, 5f, status.getHealth() / 100f, UiTheme.GREEN);
        UiTheme.bar(shapes, 1034f, 629f, 150f, 5f, status.getEnergy() / 100f, amber);
        if (boss.isAlive()) {
            UiTheme.panel(shapes, 188f, 532f, 904f, 84f, new Color(0.95f, 0.24f, 0.08f, 1f));
            UiTheme.bar(shapes, 242f, 546f, 796f, 10f,
                boss.getHealth() / boss.getMaxHealth(), new Color(1f, 0.36f, 0.06f, 1f));
        }
        shapes.end();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fonts.label.setColor(amber); fonts.label.draw(batch, "TITA // SETOR KRAKEN", 48f, 674f);
        fonts.micro.setColor(UiTheme.MUTED); fonts.micro.draw(batch, "ATMOSFERA DE METANO", 48f, 650f);
        fonts.label.setColor(UiTheme.TEXT); fonts.label.draw(batch, "EXPEDICAO TITA", 356f, 674f);
        fonts.micro.setColor(UiTheme.MUTED); fonts.micro.draw(batch, mission.getTitanObjective(), 356f, 650f);
        fonts.micro.setColor(UiTheme.CYAN_SOFT); fonts.micro.draw(batch, "O2", 978f, 674f);
        fonts.micro.draw(batch, "HP", 978f, 653f); fonts.micro.draw(batch, "EN", 978f, 632f);
        fonts.micro.setColor(UiTheme.TEXT);
        fonts.micro.draw(batch, Math.round(status.getOxygen()) + "%", 1190f, 674f);
        fonts.micro.draw(batch, Math.round(status.getHealth()) + "%", 1190f, 653f);
        fonts.micro.draw(batch, Math.round(status.getEnergy()) + "%", 1190f, 632f);
        fonts.label.setColor(amber);
        fonts.label.draw(batch, mission.getTitanObjective(), 48f, 56f);
        if (returnPortal.isPlayerNear(player)) {
            fonts.label.setColor(UiTheme.GREEN);
            fonts.label.draw(batch, mission.getCount(ItemType.TITAN_CORE) > 0
                ? "[ E ] RETORNAR A MARTE" : "PORTAL AGUARDANDO NUCLEO", 714f, 56f);
        } else if (nearbyChest != null && !nearbyChest.isOpened()) {
            fonts.label.setColor(UiTheme.WARNING);
            fonts.label.draw(batch, "[ F ] ABRIR BAU CRIOGENICO", 714f, 56f);
        } else if (oxygenBaseBounds.overlaps(player.getBounds())) {
            fonts.label.setColor(UiTheme.CYAN);
            fonts.label.draw(batch, "REFUGIO O2 // RECARREGANDO", 714f, 56f);
        }
        fonts.micro.setColor(UiTheme.CYAN_SOFT);
        fonts.micro.draw(batch, combat.isReloading() ? "RECARREGANDO "
            + Math.round(combat.getReloadProgress() * 100f) + "%" : "MUN "
            + mission.getMagazineAmmo() + "/" + mission.getReserveAmmo(), 1070f, 56f);
        if (boss.isAlive()) {
            fonts.label.setColor(UiTheme.TEXT);
            fonts.label.draw(batch, "PREDADOR DE METANO  //  "
                + Math.round(boss.getHealth()) + " / " + Math.round(boss.getMaxHealth()) + " HP",
                0f, 603f, GameConstants.VIRTUAL_WIDTH, Align.center, false);
            fonts.micro.setColor(bossAttackTimer < 0.55f ? UiTheme.WARNING : UiTheme.MUTED);
            String phase = boss.getHealth() < boss.getMaxHealth() * 0.35f
                ? "FASE 3 // TEMPESTADE KRAKEN" : boss.getHealth() < boss.getMaxHealth() * 0.70f
                ? "FASE 2 // RUPTURA CRIOGENICA" : "FASE 1 // CACADA";
            fonts.micro.draw(batch, phase, 0f, 574f,
                GameConstants.VIRTUAL_WIDTH, Align.center, false);
        }
        batch.end();
        if (damageFlash > 0f) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.9f, 0.02f, 0.01f, damageFlash * 0.28f);
            shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
            shapes.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawModal() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.002f, 0.006f, 0.009f, 0.82f);
        shapes.rect(0f, 0f, GameConstants.VIRTUAL_WIDTH, GameConstants.VIRTUAL_HEIGHT);
        UiTheme.panel(shapes, 286f, 206f, 708f, 300f, failed ? UiTheme.DANGER : UiTheme.CYAN);
        UiTheme.panel(shapes, RESUME.x, RESUME.y, RESUME.width, RESUME.height, UiTheme.GREEN);
        UiTheme.panel(shapes, MENU.x, MENU.y, MENU.width, MENU.height, UiTheme.WARNING);
        shapes.end();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fonts.heading.setColor(UiTheme.TEXT);
        fonts.heading.draw(batch, failed ? "SINAL EVA PERDIDO" : "EXPEDICAO PAUSADA", 0f, 420f,
            GameConstants.VIRTUAL_WIDTH, Align.center, false);
        fonts.label.setColor(UiTheme.TEXT);
        fonts.label.draw(batch, failed ? "RETORNAR AO CHECKPOINT" : "CONTINUAR",
            RESUME.x, RESUME.y + 36f, RESUME.width, Align.center, false);
        fonts.label.draw(batch, "VOLTAR AO MENU", MENU.x, MENU.y + 36f,
            MENU.width, Align.center, false);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void returnToMars() {
        if (changing) return;
        save();
        changing = true;
        game.getAudio().stopGameplayAudio();
        game.getAudio().playPortalActivation();
        game.changeScreen(new MarsScreen(game, mission, status,
            previousMissionTime + time, collectedItems, 2080f, 520f));
    }

    private void addTitanItems() {
        ItemType[] types = {
            ItemType.AMMO_CELL, ItemType.MEDKIT, ItemType.METHANE_SAMPLE, ItemType.FOOD,
            ItemType.OXYGEN, ItemType.MEDKIT, ItemType.AMMO_CELL, ItemType.AMMO_CELL,
            ItemType.FOOD, ItemType.MEDKIT, ItemType.OXYGEN, ItemType.OXYGEN,
            ItemType.ALLOY_PLATE, ItemType.FIBER_MESH, ItemType.QUANTUM_CORE,
            ItemType.MEDKIT, ItemType.AMMO_CELL, ItemType.OXYGEN
        };
        float[][] positions = {
            {1120,520},{930,980},{1420,710},{1710,1260},{1980,430},{2140,860},
            {2450,1450},{2680,540},{2960,1010},{3300,420},{3460,1680},{2740,760},
            {1240,1420},{1880,1740},{3100,1320},{2280,330},{3560,920},{760,1160}
        };
        for (int i = 0; i < types.length; i++) items.add(new CollectibleItem(types[i],
            positions[i][0], positions[i][1], game.getAssets()));
    }

    private void createTitanChests() {
        lootChests = new Array<>();
        float[][] positions = {{1280f, 420f}, {1880f, 1320f}, {2780f, 460f}, {3320f, 1580f}};
        for (int i = 0; i < positions.length; i++) {
            LootChest chest = new LootChest(i, positions[i][0], positions[i][1], false, true,
                game.getAssets(), mission.isTitanChestOpened(i));
            lootChests.add(chest);
            if (chest.isOpened()) chest.spawnLoot(items, game.getAssets());
        }
    }

    private void updateTitanChests(float delta) {
        nearbyChest = null;
        for (LootChest chest : lootChests) {
            chest.update(delta);
            if (!chest.isOpened() && chest.isPlayerNear(player)) nearbyChest = chest;
        }
    }

    private void spawnTitanCore() {
        if (mission.isTitanCoreInstalled() || mission.getCount(ItemType.TITAN_CORE) > 0
            || titanCoreDrop != null) return;
        titanCoreDrop = new CollectibleItem(ItemType.TITAN_CORE,
            boss.getCenterX() - 34f, boss.getCenterY() - 34f, game.getAssets());
        items.add(titanCoreDrop);
        particles.emitPickupBurst(ItemType.TITAN_CORE, boss.getCenterX(), boss.getCenterY());
    }

    private void updateBossAttacks(float delta, boolean insideBase) {
        for (int i = bossProjectiles.size - 1; i >= 0; i--) {
            BossProjectile projectile = bossProjectiles.get(i);
            projectile.update(delta);
            if (projectile.isAlive() && oxygenBaseBounds.overlaps(projectile.getBounds())) {
                particles.emitProjectileImpact(projectile.getCenterX(), projectile.getCenterY());
                projectile.destroy();
            } else if (projectile.hits(player)) {
                float damage = projectile.getDamage() * (1f - mission.getArmorProtection());
                if (!insideBase) status.removeHealth(damage);
                particles.emitDamageBurst(projectile.getCenterX(), projectile.getCenterY());
                projectile.destroy();
                if (!insideBase) {
                    damageFlash = 0.55f;
                    player.triggerDamageFlash();
                    game.getAudio().playPlayerDamage();
                }
            }
            if (!projectile.isAlive()) bossProjectiles.removeIndex(i);
        }
        if (!boss.isAlive() || insideBase) return;
        bossAttackTimer -= delta;
        if (bossAttackTimer > 0f) return;
        float dx = player.getCenterX() - boss.getCenterX();
        float dy = player.getCenterY() - boss.getCenterY();
        float hp = boss.getHealth() / boss.getMaxHealth();
        int pattern = bossAttackPattern++ % (hp < 0.70f ? 3 : 2);
        if (pattern == 0) {
            bossProjectiles.add(new BossProjectile(BossProjectile.Type.METHANE_ORB,
                boss.getCenterX(), boss.getCenterY(), dx, dy, game.getAssets()));
        } else if (pattern == 1) {
            float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            for (int offset = -16; offset <= 16; offset += 16) {
                float a = angle + offset;
                bossProjectiles.add(new BossProjectile(BossProjectile.Type.ICE_SHARD,
                    boss.getCenterX(), boss.getCenterY(), MathUtils.cosDeg(a), MathUtils.sinDeg(a),
                    game.getAssets()));
            }
        } else {
            int count = hp < 0.35f ? 14 : 10;
            for (int i = 0; i < count; i++) {
                float a = i * 360f / count + time * 23f;
                bossProjectiles.add(new BossProjectile(BossProjectile.Type.SHOCKWAVE,
                    boss.getCenterX(), boss.getCenterY(), MathUtils.cosDeg(a), MathUtils.sinDeg(a),
                    game.getAssets()));
            }
        }
        particles.emitProjectileImpact(boss.getCenterX(), boss.getCenterY());
        bossAttackTimer = hp < 0.35f ? 0.95f : hp < 0.70f ? 1.35f : 1.9f;
    }

    private void save() {
        game.getProgress().saveMission(mission, status, "TITA", previousMissionTime + time,
            collectedItems, player, items, enemies);
    }

    private void toHud(Vector2 target) {
        float x = (Gdx.input.getX() - viewport.getScreenX()) * GameConstants.VIRTUAL_WIDTH
            / Math.max(1f, viewport.getScreenWidth());
        float y = GameConstants.VIRTUAL_HEIGHT - (Gdx.input.getY() - viewport.getScreenY())
            * GameConstants.VIRTUAL_HEIGHT / Math.max(1f, viewport.getScreenHeight());
        target.set(x, y);
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        inventory.resize(width, height);
    }

    @Override public void dispose() {
        if (shapes != null) shapes.dispose();
        if (fonts != null) fonts.close();
        if (inventory != null) inventory.dispose();
    }
}
