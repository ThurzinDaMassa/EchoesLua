package com.orion.echoes.lua.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class Particle {

    private float x;
    private float y;

    private float velocityX;
    private float velocityY;

    private float age;
    private final float lifetime;

    private final float startSize;
    private final float endSize;

    private final Color color;

    public Particle(
        float x,
        float y,
        float velocityX,
        float velocityY,
        float lifetime,
        float startSize,
        float endSize,
        Color color
    ) {

        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.lifetime = lifetime;
        this.startSize = startSize;
        this.endSize = endSize;
        this.color = new Color(color);
        this.age = 0f;
    }

    public void update(float delta) {

        age += delta;

        x += velocityX * delta;
        y += velocityY * delta;

        velocityX *= 0.975f;
        velocityY *= 0.975f;
    }

    public void render(ShapeRenderer renderer) {

        float progress =
            MathUtils.clamp(age / lifetime, 0f, 1f);

        float size =
            MathUtils.lerp(startSize, endSize, progress);

        float alpha =
            1f - progress;

        renderer.setColor(
            color.r,
            color.g,
            color.b,
            alpha
        );

        renderer.circle(
            x,
            y,
            size
        );
    }

    public boolean isAlive() {
        return age < lifetime;
    }
}
