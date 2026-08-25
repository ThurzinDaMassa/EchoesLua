package com.orion.echoes.lua.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.enums.ItemType;

public class ParticleManager {

    private final Array<Particle> particles;

    private float dustTimer;
    private float rechargeTimer;
    private float lowOxygenTimer;
    private float portalTimer;

    public ParticleManager() {

        particles = new Array<>();

        dustTimer = 0f;
        rechargeTimer = 0f;
        lowOxygenTimer = 0f;
        portalTimer = 0f;
    }

    public void update(
        float delta,
        Player player,
        boolean insideBase,
        boolean lowOxygen,
        Portal portal
    ) {

        dustTimer -= delta;
        rechargeTimer -= delta;
        lowOxygenTimer -= delta;
        portalTimer -= delta;

        if (player.isMoving() && dustTimer <= 0f) {
            emitDust(player);
            dustTimer = 0.035f;
        }

        if (insideBase && rechargeTimer <= 0f) {
            emitRecharge(player);
            rechargeTimer = 0.06f;
        }

        if (lowOxygen && lowOxygenTimer <= 0f) {
            emitLowOxygen(player);
            lowOxygenTimer = 0.10f;
        }

        if (portal != null && portal.isActive() && portalTimer <= 0f) {
            emitPortalAura(portal);
            portalTimer = 0.04f;
        }

        for (int i = particles.size - 1; i >= 0; i--) {

            Particle p =
                particles.get(i);

            p.update(delta);

            if (!p.isAlive()) {
                particles.removeIndex(i);
            }
        }
    }

    private void emitDust(Player player) {

        float baseX = player.getBottomCenterX();
        float baseY = player.getBottomCenterY() + 4f;

        for (int i = 0; i < 4; i++) {

            float dirX =
                -player.getDirectionX() * 16f;

            particles.add(
                new Particle(
                    baseX + MathUtils.random(-12f, 12f),
                    baseY + MathUtils.random(-2f, 3f),
                    dirX + MathUtils.random(-18f, 18f),
                    MathUtils.random(15f, 35f),
                    MathUtils.random(0.40f, 0.70f),
                    MathUtils.random(3f, 5f),
                    MathUtils.random(8f, 12f),
                    new Color(
                        0.78f,
                        0.78f,
                        0.80f,
                        1f
                    )
                )
            );
        }
    }

    private void emitRecharge(Player player) {

        for (int i = 0; i < 3; i++) {

            particles.add(
                new Particle(
                    player.getCenterX() + MathUtils.random(-18f, 18f),
                    player.getCenterY() + MathUtils.random(-16f, 16f),
                    MathUtils.random(-8f, 8f),
                    MathUtils.random(24f, 42f),
                    0.55f,
                    3f,
                    7f,
                    new Color(
                        0.12f,
                        0.85f,
                        1f,
                        1f
                    )
                )
            );
        }
    }

    private void emitLowOxygen(Player player) {

        for (int i = 0; i < 4; i++) {

            particles.add(
                new Particle(
                    player.getCenterX() + MathUtils.random(-12f, 12f),
                    player.getCenterY() + MathUtils.random(-10f, 14f),
                    MathUtils.random(-6f, 6f),
                    MathUtils.random(20f, 36f),
                    0.45f,
                    3f,
                    7f,
                    new Color(
                        1f,
                        0.22f,
                        0.22f,
                        1f
                    )
                )
            );
        }
    }

    private void emitPortalAura(Portal portal) {

        float cx =
            portal.getCenterX();

        float cy =
            portal.getCenterY();

        for (int i = 0; i < 3; i++) {

            float angle =
                MathUtils.random(0f, 360f);

            float radius =
                MathUtils.random(35f, 80f);

            float px =
                cx + MathUtils.cosDeg(angle) * radius;

            float py =
                cy + MathUtils.sinDeg(angle) * radius;

            particles.add(
                new Particle(
                    px,
                    py,
                    MathUtils.random(-10f, 10f),
                    MathUtils.random(10f, 26f),
                    0.75f,
                    4f,
                    9f,
                    new Color(
                        0.42f,
                        0.30f,
                        1f,
                        1f
                    )
                )
            );
        }
    }

    public void emitPickupBurst(
        ItemType type,
        float x,
        float y
    ) {

        Color color =
            getPickupColor(type);

        for (int i = 0; i < 16; i++) {

            float angle =
                MathUtils.random(0f, 360f);

            float speed =
                MathUtils.random(45f, 110f);

            particles.add(
                new Particle(
                    x,
                    y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    0.50f,
                    4f,
                    1.5f,
                    color
                )
            );
        }
    }

    public void emitProcessingBurst(
        float x,
        float y
    ) {

        for (int i = 0; i < 22; i++) {

            float angle =
                MathUtils.random(0f, 360f);

            float speed =
                MathUtils.random(45f, 120f);

            particles.add(
                new Particle(
                    x,
                    y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    0.60f,
                    5f,
                    1.5f,
                    new Color(
                        0.10f,
                        0.88f,
                        1f,
                        1f
                    )
                )
            );
        }
    }

    public void emitPortalActivationBurst(
        float x,
        float y
    ) {

        for (int i = 0; i < 34; i++) {

            float angle =
                MathUtils.random(0f, 360f);

            float speed =
                MathUtils.random(70f, 180f);

            particles.add(
                new Particle(
                    x,
                    y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    0.85f,
                    6f,
                    2f,
                    new Color(
                        0.50f,
                        0.32f,
                        1f,
                        1f
                    )
                )
            );
        }
    }

    public void emitMuzzleBurst(float x, float y, float directionX, float directionY) {
        for (int i = 0; i < 12; i++) {
            float spread = MathUtils.random(-0.24f, 0.24f);
            float speed = MathUtils.random(90f, 230f);
            particles.add(new Particle(
                x, y,
                (directionX - directionY * spread) * speed,
                (directionY + directionX * spread) * speed,
                MathUtils.random(0.12f, 0.24f),
                MathUtils.random(3f, 6f), 0.7f,
                i % 3 == 0 ? new Color(1f, 0.24f, 0.78f, 1f)
                    : new Color(0.18f, 0.94f, 1f, 1f)
            ));
        }
    }

    public void emitProjectileTrail(float x, float y, float directionX, float directionY) {
        particles.add(new Particle(
            x - directionX * 14f + MathUtils.random(-2f, 2f),
            y - directionY * 14f + MathUtils.random(-2f, 2f),
            -directionX * MathUtils.random(18f, 38f),
            -directionY * MathUtils.random(18f, 38f),
            0.18f, 3f, 0.3f,
            MathUtils.randomBoolean(0.22f)
                ? new Color(1f, 0.22f, 0.72f, 0.9f)
                : new Color(0.15f, 0.88f, 1f, 0.9f)
        ));
    }

    public void emitProjectileImpact(float x, float y) {
        for (int i = 0; i < 24; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(55f, 190f);
            particles.add(new Particle(
                x, y,
                MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed,
                MathUtils.random(0.25f, 0.48f),
                MathUtils.random(3f, 7f), 1.1f,
                i % 4 == 0 ? new Color(1f, 0.28f, 0.74f, 1f)
                    : new Color(0.12f, 0.96f, 1f, 1f)
            ));
        }
    }

    private Color getPickupColor(ItemType type) {

        switch (type) {
            case OXYGEN:
                return new Color(0.15f, 0.82f, 1f, 1f);
            case FOOD:
                return new Color(1f, 0.62f, 0.12f, 1f);
            case ICE_ROCK:
                return new Color(0.78f, 0.92f, 1f, 1f);
            case ANTENNA_PART:
                return new Color(0.18f, 0.92f, 1f, 1f);
            case ENERGY_PART:
                return new Color(1f, 0.78f, 0.18f, 1f);
            case EXTRACTION_PART:
                return new Color(0.24f, 0.68f, 1f, 1f);
            case GREENHOUSE_PART:
                return new Color(0.26f, 1f, 0.54f, 1f);
            case WEAPON_PART_A:
            case WEAPON_PART_B:
            case WEAPON_PART_C:
                return new Color(0.78f, 0.34f, 1f, 1f);
            default:
                return Color.WHITE;
        }
    }

    public void render(ShapeRenderer renderer) {
        for (Particle particle : particles) {
            particle.render(renderer);
        }
    }

    public void clear() {
        particles.clear();
    }
}
