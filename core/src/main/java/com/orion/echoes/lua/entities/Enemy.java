package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;

public class Enemy {
    public enum Behavior { PATROL, HUNTER }
    private static final float SIZE = 58f;
    private final Vector2 position;
    private final Rectangle bounds;
    private final float patrolOriginX;
    private final Sprite sprite;
    private final Behavior behavior;
    private float hp = 100f;
    private float time;
    private boolean pendingDrop;
    private float velocityX;
    private float velocityY;
    private float hitFlashTimer;

    public Enemy(float x, float y, GameAssets assets) {
        this(x, y, assets.getEnemySentinel(), Behavior.HUNTER);
    }

    public Enemy(float x, float y, GameAssets assets, Behavior behavior) {
        this(x, y, assets.getEnemySentinel(), behavior);
    }

    public Enemy(float x, float y, Texture texture) {
        this(x, y, texture, Behavior.HUNTER);
    }

    public Enemy(float x, float y, Texture texture, Behavior behavior) {
        position = new Vector2(x, y);
        patrolOriginX = x;
        this.behavior = behavior;
        bounds = new Rectangle(x, y, SIZE, SIZE);
        sprite = new Sprite(texture);
        sprite.setSize(112f, 105f);
        sprite.setOriginCenter();
        updateSprite();
    }

    public void update(float delta, Player player) {
        if (!isAlive()) return;
        time += delta;
        hitFlashTimer = Math.max(0f, hitFlashTimer - delta);
        float previousX = position.x;
        float previousY = position.y;
        float dx = player.getCenterX() - getCenterX();
        float dy = player.getCenterY() - getCenterY();
        float distance2 = dx * dx + dy * dy;
        if (behavior == Behavior.HUNTER && distance2 < 760f * 760f && distance2 > 1f) {
            float distance = (float) Math.sqrt(distance2);
            position.add(dx / distance * 122f * delta, dy / distance * 122f * delta);
        } else {
            position.x = patrolOriginX + MathUtils.sin(time * 0.86f) * 132f;
        }
        velocityX = (position.x - previousX) / Math.max(delta, 0.0001f);
        velocityY = (position.y - previousY) / Math.max(delta, 0.0001f);
        bounds.setPosition(position);
        updateSprite();
    }

    public void damage(float amount) {
        boolean wasAlive = isAlive();
        hp = Math.max(0f, hp - amount);
        hitFlashTimer = 0.16f;
        if (wasAlive && !isAlive()) pendingDrop = true;
    }

    public void defeat() {
        hp = 0f;
    }

    private void updateSprite() {
        float cadence = behavior == Behavior.HUNTER ? 7.4f : 4.6f;
        float stride = MathUtils.sin(time * cadence);
        float lift = Math.abs(stride) * (behavior == Behavior.HUNTER ? 7f : 4f);
        float squash = 1f + MathUtils.sin(time * cadence * 2f) * 0.035f;
        sprite.setPosition(position.x - 27f, position.y - 21f + lift);
        sprite.setScale(1f / squash, squash);
        sprite.setRotation(MathUtils.clamp(-velocityX * 0.035f, -7f, 7f)
            + MathUtils.sin(time * 1.8f) * 1.5f);
        if (Math.abs(velocityX) > 1f) sprite.setFlip(velocityX < 0f, false);
    }

    public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        if (hitFlashTimer > 0f) sprite.setColor(1f, 0.30f, 0.24f, 1f);
        else sprite.setColor(Color.WHITE);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
    }

    public void renderStatus(ShapeRenderer shapes) {
        if (!isAlive()) return;
        float cx = getCenterX();
        shapes.setColor(0.08f, 0.015f, 0.12f, 0.88f);
        shapes.rect(cx - 30f, position.y - 10f, 60f, 5f);
        shapes.setColor(0.26f, 1f, 0.92f, 1f);
        shapes.rect(cx - 30f, position.y - 10f, 60f * hp / 100f, 5f);
    }

    public boolean overlaps(Player player) {
        return isAlive() && bounds.overlaps(player.getBounds());
    }

    public boolean isAlive() {
        return hp > 0f;
    }

    public float getCenterX() { return position.x + SIZE / 2f; }
    public float getCenterY() { return position.y + SIZE / 2f; }
    public Rectangle getBounds() { return bounds; }
    public Behavior getBehavior() { return behavior; }
    public boolean consumePendingDrop() {
        if (!pendingDrop) return false;
        pendingDrop = false;
        return true;
    }
}
