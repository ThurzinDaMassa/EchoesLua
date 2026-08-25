package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.utils.GameConstants;

/** Disparo visual da arma EVA, com alcance e colisao reais. */
public class Projectile {
    private static final float SPEED = 920f;
    private static final float MAX_LIFETIME = 1.65f;
    private static final float WIDTH = 48f;
    private static final float HEIGHT = 22f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Sprite sprite;
    private float lifetime = MAX_LIFETIME;
    private boolean alive = true;

    public Projectile(float x, float y, float directionX, float directionY, GameAssets assets) {
        Vector2 direction = new Vector2(directionX, directionY);
        if (direction.isZero(0.001f)) direction.set(1f, 0f);
        direction.nor();

        position.set(x - WIDTH / 2f, y - HEIGHT / 2f);
        velocity.set(direction).scl(SPEED);
        bounds.set(position.x + 6f, position.y + 6f, WIDTH - 12f, HEIGHT - 12f);

        sprite = new Sprite(assets.getEnergyProjectile());
        sprite.setSize(WIDTH, HEIGHT);
        sprite.setOriginCenter();
        sprite.setRotation(direction.angleDeg());
        syncSprite();
    }

    public void update(float delta) {
        if (!alive) return;
        position.mulAdd(velocity, delta);
        lifetime -= delta;
        bounds.setPosition(position.x + 6f, position.y + 6f);
        syncSprite();

        if (lifetime <= 0f
            || position.x < -WIDTH || position.y < -HEIGHT
            || position.x > GameConstants.WORLD_WIDTH + WIDTH
            || position.y > GameConstants.WORLD_HEIGHT + HEIGHT) {
            alive = false;
        }
    }

    private void syncSprite() {
        sprite.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        if (alive) sprite.draw(batch);
    }

    public void destroy() { alive = false; }
    public boolean isAlive() { return alive; }
    public Rectangle getBounds() { return bounds; }
    public float getCenterX() { return position.x + WIDTH / 2f; }
    public float getCenterY() { return position.y + HEIGHT / 2f; }
    public float getDirectionX() { return velocity.x / SPEED; }
    public float getDirectionY() { return velocity.y / SPEED; }
}
