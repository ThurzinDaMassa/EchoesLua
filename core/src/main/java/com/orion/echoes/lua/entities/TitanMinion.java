package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;

/** Cacador leve de Tita com animacao agil e vida reduzida. */
public final class TitanMinion extends Enemy {
    private static final float MAX_HP = 55f;
    private final Vector2 position;
    private final Rectangle bounds;
    private final Sprite sprite;
    private final float originX;
    private float hp = MAX_HP;
    private float time;
    private float hitFlash;
    private float velocityX;
    private boolean pendingDrop;
    private boolean pendingDeathAnimation;

    public TitanMinion(float x, float y, GameAssets assets) {
        super(x, y, assets.getEnemyTitanMinion(), Behavior.HUNTER);
        position = new Vector2(x, y);
        originX = x;
        bounds = new Rectangle(x, y, 44f, 34f);
        sprite = new Sprite(assets.getEnemyTitanMinion());
        sprite.setSize(76f, 51f);
        sprite.setOriginCenter();
        updateSprite();
    }

    @Override public void update(float delta, Player player) {
        if (!isAlive()) return;
        time += delta;
        hitFlash = Math.max(0f, hitFlash - delta);
        float oldX = position.x;
        float dx = player.getCenterX() - getCenterX();
        float dy = player.getCenterY() - getCenterY();
        float distance2 = dx * dx + dy * dy;
        if (distance2 < 620f * 620f && distance2 > 2f) {
            float distance = (float)Math.sqrt(distance2);
            position.add(dx / distance * 148f * delta, dy / distance * 148f * delta);
        } else {
            position.x = originX + MathUtils.sin(time * 1.35f) * 82f;
        }
        velocityX = (position.x - oldX) / Math.max(delta, 0.0001f);
        bounds.setPosition(position);
        updateSprite();
    }

    private void updateSprite() {
        float gait = MathUtils.sin(time * 10.5f);
        float leap = Math.abs(gait) * 7f;
        sprite.setPosition(position.x - 16f, position.y - 10f + leap);
        sprite.setScale(1f + gait * 0.055f, 1f - Math.abs(gait) * 0.08f);
        sprite.setRotation(MathUtils.clamp(-velocityX * 0.035f, -10f, 10f));
        if (Math.abs(velocityX) > 1f) sprite.setFlip(velocityX < 0f, false);
    }

    @Override public void damage(float amount) {
        boolean alive = isAlive();
        hp = Math.max(0f, hp - amount);
        hitFlash = 0.14f;
        if (alive && !isAlive()) { pendingDrop = true; pendingDeathAnimation = true; }
    }

    @Override public void defeat() { hp = 0f; }
    @Override public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        sprite.setColor(hitFlash > 0f ? new Color(1f, 0.24f, 0.12f, 1f) : Color.WHITE);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
    }
    @Override public void renderStatus(ShapeRenderer shapes) {
        if (!isAlive()) return;
        shapes.setColor(0.12f, 0.02f, 0.01f, 0.86f);
        shapes.rect(getCenterX() - 22f, position.y - 8f, 44f, 4f);
        shapes.setColor(1f, 0.60f, 0.12f, 1f);
        shapes.rect(getCenterX() - 22f, position.y - 8f, 44f * hp / MAX_HP, 4f);
    }
    @Override public boolean overlaps(Player player) { return isAlive() && bounds.overlaps(player.getBounds()); }
    @Override public boolean isAlive() { return hp > 0f; }
    @Override public float getHealth() { return hp; }
    @Override public float getMaxHealth() { return MAX_HP; }
    @Override public com.badlogic.gdx.graphics.Texture getDeathTexture() { return sprite.getTexture(); }
    @Override public float getDeathWidth() { return sprite.getWidth(); }
    @Override public float getDeathHeight() { return sprite.getHeight(); }
    @Override public boolean consumeDeathAnimation() {
        if (!pendingDeathAnimation) return false;
        pendingDeathAnimation = false;
        return true;
    }
    @Override public float getCenterX() { return position.x + bounds.width / 2f; }
    @Override public float getCenterY() { return position.y + bounds.height / 2f; }
    @Override public Rectangle getBounds() { return bounds; }
    @Override public boolean consumePendingDrop() {
        if (!pendingDrop) return false;
        pendingDrop = false;
        return true;
    }
}
