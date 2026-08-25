package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;

public class Enemy {
    private static final float SIZE = 58f;
    private final Vector2 position;
    private final Rectangle bounds;
    private final float patrolOriginX;
    private final Sprite sprite;
    private float hp = 100f;
    private float time;

    public Enemy(float x, float y, GameAssets assets) {
        position = new Vector2(x, y);
        patrolOriginX = x;
        bounds = new Rectangle(x, y, SIZE, SIZE);
        sprite = new Sprite(assets.getEnemySentinel());
        sprite.setSize(112f, 105f);
        sprite.setOriginCenter();
        updateSprite();
    }

    public void update(float delta, Player player) {
        if (!isAlive()) return;
        time += delta;
        float dx = player.getCenterX() - getCenterX();
        float dy = player.getCenterY() - getCenterY();
        float distance2 = dx * dx + dy * dy;
        if (distance2 < 620f * 620f && distance2 > 1f) {
            float distance = (float) Math.sqrt(distance2);
            position.add(dx / distance * 105f * delta, dy / distance * 105f * delta);
        } else {
            position.x = patrolOriginX + MathUtils.sin(time * 0.72f) * 90f;
        }
        bounds.setPosition(position);
        updateSprite();
    }

    public void damage(float amount) {
        hp = Math.max(0f, hp - amount);
    }

    public void defeat() {
        hp = 0f;
    }

    private void updateSprite() {
        sprite.setPosition(position.x - 27f, position.y - 21f + MathUtils.sin(time * 4f) * 5f);
        sprite.setRotation(MathUtils.sin(time * 1.8f) * 3f);
    }

    public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        sprite.draw(batch);
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
}
