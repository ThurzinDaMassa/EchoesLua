package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.orion.echoes.lua.assets.GameAssets;

/** Projetil animado exclusivo dos tres padroes de ataque do chefe de Tita. */
public final class BossProjectile {
    public enum Type { METHANE_ORB, ICE_SHARD, SHOCKWAVE }
    private final Type type;
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Sprite sprite;
    private final float baseWidth;
    private final float baseHeight;
    private final float damage;
    private float life;
    private float time;
    private boolean alive = true;

    public BossProjectile(Type type, float x, float y, float dx, float dy, GameAssets assets) {
        this.type = type;
        Vector2 direction = new Vector2(dx, dy);
        if (direction.isZero(0.001f)) direction.set(1f, 0f);
        direction.nor();
        Texture texture;
        float speed;
        if (type == Type.ICE_SHARD) {
            texture = assets.getBossIceShard(); baseWidth = 92f; baseHeight = 38f;
            speed = 470f; damage = 14f; life = 4.2f;
        } else if (type == Type.SHOCKWAVE) {
            texture = assets.getBossShockwave(); baseWidth = 104f; baseHeight = 104f;
            speed = 245f; damage = 21f; life = 5.2f;
        } else {
            texture = assets.getBossMethaneOrb(); baseWidth = 68f; baseHeight = 68f;
            speed = 320f; damage = 18f; life = 4.8f;
        }
        position.set(x - baseWidth / 2f, y - baseHeight / 2f);
        velocity.set(direction).scl(speed);
        sprite = new Sprite(texture);
        sprite.setSize(baseWidth, baseHeight);
        sprite.setOriginCenter();
        if (type == Type.ICE_SHARD) sprite.setRotation(direction.angleDeg());
        sync();
    }

    public void update(float delta) {
        if (!alive) return;
        time += delta;
        life -= delta;
        position.mulAdd(velocity, delta);
        if (type == Type.METHANE_ORB) sprite.rotate(150f * delta);
        if (type == Type.SHOCKWAVE) sprite.rotate(-74f * delta);
        float pulse = 1f + MathUtils.sin(time * (type == Type.SHOCKWAVE ? 8f : 13f)) * 0.09f;
        sprite.setScale(pulse);
        sync();
        if (life <= 0f) alive = false;
    }

    private void sync() {
        sprite.setPosition(position.x, position.y);
        bounds.set(position.x + baseWidth * 0.22f, position.y + baseHeight * 0.22f,
            baseWidth * 0.56f, baseHeight * 0.56f);
    }

    public boolean hits(Player player) { return alive && bounds.overlaps(player.getBounds()); }
    public void destroy() { alive = false; }
    public boolean isAlive() { return alive; }
    public float getDamage() { return damage; }
    public float getCenterX() { return position.x + baseWidth / 2f; }
    public float getCenterY() { return position.y + baseHeight / 2f; }
    public Rectangle getBounds() { return bounds; }
    public Type getType() { return type; }
    public void render(SpriteBatch batch) { if (alive) sprite.draw(batch); }
}
