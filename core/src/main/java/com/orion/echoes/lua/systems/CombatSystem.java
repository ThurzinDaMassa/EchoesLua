package com.orion.echoes.lua.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Projectile;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.config.Difficulty;
import com.orion.echoes.lua.enums.ItemType;

/** Combate direcional com mira livre, cadencia e projeteis visuais. */
public class CombatSystem {
    private static final float FIRE_INTERVAL = 0.20f;
    private static final float PROJECTILE_DAMAGE = 42f;
    private static final float RELOAD_DURATION = 1.05f;
    private static final float INITIAL_SAFETY_DURATION = 2.75f;

    private final GameAssets assets;
    private final float enemyDamageMultiplier;
    private final Array<Projectile> projectiles = new Array<>();
    private final Vector2 aimDirection = new Vector2(1f, 0f);
    private float contactCooldown;
    private float fireCooldown;
    private float blockedMessageCooldown;
    private float reloadTimer;
    private float initialSafetyTimer = INITIAL_SAFETY_DURATION;
    private boolean threatAnnounced;

    public CombatSystem(GameAssets assets) {
        this(assets, Difficulty.STANDARD);
    }

    public CombatSystem(GameAssets assets, Difficulty difficulty) {
        this.assets = assets;
        this.enemyDamageMultiplier = difficulty.getEnemyDamageMultiplier();
    }

    public boolean update(float delta, Player player, PlayerStatus status,
                       MissionState mission, Array<Enemy> enemies,
                       ParticleManager particles, AudioManager audio,
                       float aimX, float aimY, boolean firing) {
        contactCooldown = Math.max(0f, contactCooldown - delta);
        fireCooldown = Math.max(0f, fireCooldown - delta);
        blockedMessageCooldown = Math.max(0f, blockedMessageCooldown - delta);
        initialSafetyTimer = Math.max(0f, initialSafetyTimer - delta);

        if (reloadTimer > 0f) {
            reloadTimer = Math.max(0f, reloadTimer - delta);
            if (reloadTimer == 0f) {
                int loaded = mission.reloadMagazine();
                if (loaded > 0) {
                    mission.notifyAction("ARMA RECARREGADA // "
                        + mission.getMagazineAmmo() + "/" + mission.getReserveAmmo());
                    audio.playWeaponReloadComplete();
                }
            }
        }

        boolean playerDamaged = updateEnemies(delta, player, status, mission, enemies, audio);

        aimDirection.set(aimX - player.getCenterX(), aimY - player.getCenterY());
        if (aimDirection.isZero(0.001f)) aimDirection.set(player.isFacingLeft() ? -1f : 1f, 0f);
        aimDirection.nor();

        if (firing && !mission.hasWeapon() && blockedMessageCooldown <= 0f) {
            mission.notifyAction("Arma indisponivel // encontre A, B e C e fabrique na base");
            blockedMessageCooldown = 1.25f;
        }

        if (firing && mission.hasWeapon() && fireCooldown <= 0f && reloadTimer <= 0f) {
            if (mission.consumeMagazineRound()) {
                fire(player, particles, audio);
                if (mission.getMagazineAmmo() == 0 && mission.getReserveAmmo() > 0)
                    startReload(mission, audio);
            } else if (mission.getReserveAmmo() > 0) {
                startReload(mission, audio);
            } else if (blockedMessageCooldown <= 0f) {
                mission.notifyAction("MUNICAO ESGOTADA // procure cargas no mapa");
                blockedMessageCooldown = 1.25f;
                fireCooldown = 0.22f;
            }
        }

        updateProjectiles(delta, mission, enemies, particles, audio);
        return playerDamaged;
    }

    public void requestReload(MissionState mission, AudioManager audio) {
        if (mission != null && mission.hasWeapon() && reloadTimer <= 0f
            && mission.getMagazineAmmo() < MissionState.MAGAZINE_SIZE
            && mission.getReserveAmmo() > 0) startReload(mission, audio);
    }

    private void startReload(MissionState mission, AudioManager audio) {
        reloadTimer = RELOAD_DURATION;
        mission.notifyAction("RECARREGANDO ARMA EVA...");
        audio.playWeaponReloadStart();
    }

    public boolean isReloading() { return reloadTimer > 0f; }
    public float getReloadProgress() {
        return reloadTimer <= 0f ? 1f : 1f - reloadTimer / RELOAD_DURATION;
    }

    public boolean update(float delta, Player player, PlayerStatus status,
                          MissionState mission, Array<Enemy> enemies,
                          Array<CollectibleItem> worldItems,
                          ParticleManager particles, AudioManager audio,
                          float aimX, float aimY, boolean firing) {
        int previousKills = mission.getEnemiesDefeated();
        boolean damaged = update(delta, player, status, mission, enemies, particles,
            audio, aimX, aimY, firing);
        int newKills = mission.getEnemiesDefeated() - previousKills;
        if (newKills > 0 && worldItems != null) {
            for (Enemy enemy : enemies) {
                if (enemy.consumePendingDrop()) {
                    worldItems.add(new CollectibleItem(ItemType.AMMO_CELL,
                        enemy.getCenterX() - 28f, enemy.getCenterY() - 28f, assets));
                }
            }
        }
        return damaged;
    }

    private boolean updateEnemies(float delta, Player player, PlayerStatus status,
                                  MissionState mission, Array<Enemy> enemies,
                                  AudioManager audio) {
        boolean damaged = false;
        boolean threatNearby = false;
        for (Enemy enemy : enemies) {
            enemy.update(delta, player);
            float distance = Vector2.dst(player.getCenterX(), player.getCenterY(),
                enemy.getCenterX(), enemy.getCenterY());
            if (enemy.isAlive() && distance < 520f) threatNearby = true;
            if (initialSafetyTimer <= 0f && enemy.overlaps(player) && contactCooldown <= 0f) {
                float armorMultiplier = 1f - mission.getArmorProtection();
                status.removeHealth(18f * enemyDamageMultiplier * armorMultiplier);
                contactCooldown = 1.10f;
                damaged = true;
            }
        }
        if (threatNearby && !threatAnnounced && initialSafetyTimer <= 0f) {
            threatAnnounced = true;
            audio.playEnemyAlert();
            mission.notifyAction("ALERTA // AMEACA HOSTIL PROXIMA");
        } else if (!threatNearby) {
            threatAnnounced = false;
        }
        return damaged;
    }

    private void fire(Player player, ParticleManager particles, AudioManager audio) {
        float muzzleX = player.getCenterX() + aimDirection.x * 54f;
        float muzzleY = player.getCenterY() - 4f + aimDirection.y * 54f;
        projectiles.add(new Projectile(muzzleX, muzzleY,
            aimDirection.x, aimDirection.y, assets));
        particles.emitMuzzleBurst(muzzleX, muzzleY, aimDirection.x, aimDirection.y);
        audio.playWeaponFire();
        player.triggerFireAnimation();
        fireCooldown = FIRE_INTERVAL;
    }

    private void updateProjectiles(float delta, MissionState mission,
                                   Array<Enemy> enemies, ParticleManager particles,
                                   AudioManager audio) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);

            if (projectile.isAlive()) {
                particles.emitProjectileTrail(projectile.getCenterX(), projectile.getCenterY(),
                    projectile.getDirectionX(), projectile.getDirectionY());
                for (Enemy enemy : enemies) {
                    if (enemy.isAlive() && projectile.getBounds().overlaps(enemy.getBounds())) {
                        enemy.damage(PROJECTILE_DAMAGE);
                        projectile.destroy();
                        particles.emitProjectileImpact(projectile.getCenterX(), projectile.getCenterY());
                        audio.playEnemyHit();
                        if (!enemy.isAlive()) mission.recordEnemyDefeated();
                        break;
                    }
                }
            }

            if (!projectile.isAlive()) projectiles.removeIndex(i);
        }
    }

    public void render(SpriteBatch batch) {
        for (Projectile projectile : projectiles) projectile.render(batch);
    }

    public void clear() {
        projectiles.clear();
    }
}
