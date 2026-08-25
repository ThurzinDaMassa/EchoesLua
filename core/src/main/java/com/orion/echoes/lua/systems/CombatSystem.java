package com.orion.echoes.lua.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Projectile;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.config.Difficulty;

/** Combate direcional com mira livre, cadencia e projeteis visuais. */
public class CombatSystem {
    private static final float FIRE_INTERVAL = 0.20f;
    private static final float PROJECTILE_DAMAGE = 42f;

    private final GameAssets assets;
    private final float enemyDamageMultiplier;
    private final Array<Projectile> projectiles = new Array<>();
    private final Vector2 aimDirection = new Vector2(1f, 0f);
    private float contactCooldown;
    private float fireCooldown;
    private float blockedMessageCooldown;

    public CombatSystem(GameAssets assets) {
        this(assets, Difficulty.STANDARD);
    }

    public CombatSystem(GameAssets assets, Difficulty difficulty) {
        this.assets = assets;
        this.enemyDamageMultiplier = difficulty.getEnemyDamageMultiplier();
    }

    public void update(float delta, Player player, PlayerStatus status,
                       MissionState mission, Array<Enemy> enemies,
                       ParticleManager particles, AudioManager audio,
                       float aimX, float aimY, boolean firing) {
        contactCooldown = Math.max(0f, contactCooldown - delta);
        fireCooldown = Math.max(0f, fireCooldown - delta);
        blockedMessageCooldown = Math.max(0f, blockedMessageCooldown - delta);

        updateEnemies(delta, player, status, enemies, audio);

        aimDirection.set(aimX - player.getCenterX(), aimY - player.getCenterY());
        if (aimDirection.isZero(0.001f)) aimDirection.set(player.isFacingLeft() ? -1f : 1f, 0f);
        aimDirection.nor();

        if (firing && !mission.hasWeapon() && blockedMessageCooldown <= 0f) {
            mission.notifyAction("Arma indisponivel // encontre A, B e C e fabrique na base");
            blockedMessageCooldown = 1.25f;
        }

        if (firing && mission.hasWeapon() && fireCooldown <= 0f) {
            fire(player, particles, audio);
        }

        updateProjectiles(delta, mission, enemies, particles, audio);
    }

    private void updateEnemies(float delta, Player player, PlayerStatus status,
                               Array<Enemy> enemies, AudioManager audio) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, player);
            if (enemy.overlaps(player) && contactCooldown <= 0f) {
                status.removeHealth(18f * enemyDamageMultiplier);
                contactCooldown = 1.10f;
                audio.playRockImpact();
            }
        }
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
