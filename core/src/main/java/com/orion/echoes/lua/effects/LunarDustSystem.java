package com.orion.echoes.lua.effects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.utils.GameConstants;

public class LunarDustSystem {

    private final Array<LunarDustParticle> particles;

    private float emissionTimer;

    private static final float EMISSION_INTERVAL =
        0.055f;

    public LunarDustSystem() {

        particles =
            new Array<>();

        emissionTimer = 0f;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta,
        Player player
    ) {

        emissionTimer -= delta;

        /*
         * So gera poeira enquanto
         * o jogador estiver andando.
         */
        if (
            player.isMoving()
                && emissionTimer <= 0f
        ) {

            emitParticles(
                player
            );

            emissionTimer =
                EMISSION_INTERVAL;
        }

        updateParticles(
            delta
        );

        removeDeadParticles();
    }

    // =========================================================
    // EMISSAO
    // =========================================================

    private void emitParticles(
        Player player
    ) {

        float baseX =
            player.getCenterX();

        float baseY =
            player.getY() + 4f;

        /*
         * Gera entre 1 e 3 particulas
         * por emissao.
         */
        int amount =
            MathUtils.random(
                1,
                3
            );

        for (
            int i = 0;
            i < amount;
            i++
        ) {

            float offsetX =
                MathUtils.random(
                    -GameConstants.PLAYER_WIDTH
                        * 0.35f,
                    GameConstants.PLAYER_WIDTH
                        * 0.35f
                );

            float offsetY =
                MathUtils.random(
                    -3f,
                    5f
                );

            particles.add(
                new LunarDustParticle(
                    baseX + offsetX,
                    baseY + offsetY
                )
            );
        }
    }

    // =========================================================
    // UPDATE DAS PARTICULAS
    // =========================================================

    private void updateParticles(
        float delta
    ) {

        for (
            LunarDustParticle particle :
            particles
        ) {

            particle.update(
                delta
            );
        }
    }

    // =========================================================
    // LIMPEZA
    // =========================================================

    private void removeDeadParticles() {

        for (
            int i =
            particles.size - 1;
            i >= 0;
            i--
        ) {

            if (
                !particles
                    .get(i)
                    .isAlive()
            ) {

                particles.removeIndex(
                    i
                );
            }
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render(
        ShapeRenderer renderer
    ) {

        for (
            LunarDustParticle particle :
            particles
        ) {

            particle.render(
                renderer
            );
        }
    }

    // =========================================================
    // GETTER
    // =========================================================

    public int getParticleCount() {
        return particles.size;
    }

    public void clear() {
        particles.clear();
    }
}
