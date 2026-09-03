package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;

/** Predador exclusivo de Tita: pesado, resistente e de perseguicao curta. */
public final class TitanEnemy extends Enemy {
    private static final float MAX_HP = 3000f;
    private final Vector2 position;
    private final Rectangle bounds;
    private final Sprite sprite;
    private final float originX;
    private float hp = MAX_HP;
    private float time;
    private float hitFlash;
    private boolean pendingDrop;
    private boolean pendingDeathAnimation;
    private float velocityX;

    public TitanEnemy(float x, float y, GameAssets assets) {
        super(x, y, assets.getEnemyTitanStalker(), Behavior.HUNTER);
        position = new Vector2(x, y);
        originX = x;
        bounds = new Rectangle(x, y, 205f, 160f);
        sprite = new Sprite(assets.getEnemyTitanStalker());
        sprite.setSize(410f, 320f);
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
        if (distance2 < 520f * 520f && distance2 > 4f) {
            float distance = (float)Math.sqrt(distance2);
            float charge = distance < 210f ? 1.34f : 1f;
            position.add(dx / distance * 66f * charge * delta,
                dy / distance * 66f * charge * delta);
        } else {
            position.x = originX + MathUtils.sin(time * 0.52f) * 96f;
        }
        velocityX = (position.x - oldX) / Math.max(delta, 0.0001f);
        bounds.setPosition(position);
        updateSprite();
    }

    @Override public void damage(float amount) {
        boolean alive = isAlive();
        hp = Math.max(0f, hp - amount);
        hitFlash = 0.18f;
        if (alive && !isAlive()) { pendingDrop = true; pendingDeathAnimation = true; }
    }

    @Override public void defeat() { hp = 0f; }

    private void updateSprite() {
        float stride = MathUtils.sin(time * 5.2f);
        float lift = Math.abs(stride) * 6f;
        float rage = hp < MAX_HP * 0.35f ? 1.08f : 1f;
        sprite.setPosition(position.x - 102f, position.y - 76f + lift * 1.6f);
        sprite.setScale((1f + Math.abs(stride) * 0.055f) * rage,
            (1f - Math.abs(stride) * 0.035f) * rage);
        sprite.setRotation(MathUtils.clamp(-velocityX * 0.045f, -8f, 8f));
        if (Math.abs(velocityX) > 1f) sprite.setFlip(velocityX < 0f, false);
    }

    @Override public void render(SpriteBatch batch) {
        if (!isAlive()) return;
        sprite.setColor(hitFlash > 0f ? new Color(1f, 0.28f, 0.12f, 1f) : Color.WHITE);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
    }

    @Override public void renderStatus(ShapeRenderer shapes) {
        if (!isAlive()) return;
        shapes.setColor(0.10f, 0.025f, 0.01f, 0.92f);
        shapes.rect(getCenterX() - 38f, position.y - 12f, 76f, 6f);
        shapes.setColor(1f, 0.50f, 0.10f, 1f);
        shapes.rect(getCenterX() - 38f, position.y - 12f, 76f * hp / MAX_HP, 6f);
    }

    @Override public boolean overlaps(Player player) {
        return isAlive() && bounds.overlaps(player.getBounds());
    }

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
