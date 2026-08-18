package com.orion.echoes.lua.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class LunarDustParticle {

    private float x;
    private float y;

    private float velocityX;
    private float velocityY;

    private float size;

    private float life;
    private float maxLife;

    private boolean alive;

    public LunarDustParticle(
        float x,
        float y
    ) {

        this.x = x;
        this.y = y;

        /*
         * Pequena dispersao lateral.
         */
        this.velocityX =
            MathUtils.random(
                -25f,
                25f
            );

        /*
         * Poeira sobe levemente.
         */
        this.velocityY =
            MathUtils.random(
                8f,
                25f
            );

        this.size =
            MathUtils.random(
                4f,
                9f
            );

        this.maxLife =
            MathUtils.random(
                0.45f,
                0.85f
            );

        this.life =
            maxLife;

        this.alive =
            true;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta
    ) {

        if (!alive) {
            return;
        }

        x += velocityX * delta;
        y += velocityY * delta;

        /*
         * Reduz um pouco a velocidade
         * com o tempo.
         */
        velocityX *= 0.97f;
        velocityY *= 0.97f;

        /*
         * A particula aumenta levemente.
         */
        size += 4f * delta;

        life -= delta;

        if (life <= 0f) {

            life = 0f;
            alive = false;
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render(
        ShapeRenderer renderer
    ) {

        if (!alive) {
            return;
        }

        float alpha =
            life / maxLife;

        renderer.setColor(
            new Color(
                0.72f,
                0.72f,
                0.75f,
                alpha
            )
        );

        renderer.circle(
            x,
            y,
            size
        );
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public boolean isAlive() {
        return alive;
    }
}
