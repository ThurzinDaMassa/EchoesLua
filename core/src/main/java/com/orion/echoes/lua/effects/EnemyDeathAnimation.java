package com.orion.echoes.lua.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.orion.echoes.lua.entities.Enemy;

/** Fragmenta visualmente o sprite do inimigo em placas animadas na morte. */
public final class EnemyDeathAnimation {
    private static final class Fragment {
        TextureRegion region;
        float x, y, vx, vy, width, height, rotation, spin, life, maxLife;
    }
    private final Array<Fragment> fragments = new Array<>();

    public void emit(Enemy enemy) {
        Texture texture = enemy.getDeathTexture();
        if (texture == null) return;
        int halfW = Math.max(1, texture.getWidth() / 2);
        int halfH = Math.max(1, texture.getHeight() / 2);
        float pieceW = enemy.getDeathWidth() * 0.52f;
        float pieceH = enemy.getDeathHeight() * 0.52f;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                Fragment f = new Fragment();
                f.region = new TextureRegion(texture, col * halfW, row * halfH, halfW, halfH);
                f.width = pieceW; f.height = pieceH;
                f.x = enemy.getCenterX() - pieceW / 2f + (col == 0 ? -14f : 14f);
                f.y = enemy.getCenterY() - pieceH / 2f + (row == 0 ? -10f : 16f);
                f.vx = (col == 0 ? -1f : 1f) * MathUtils.random(72f, 155f);
                f.vy = MathUtils.random(90f, 190f) + row * 28f;
                f.spin = MathUtils.random(-260f, 260f);
                f.maxLife = f.life = MathUtils.random(0.65f, 1.05f);
                fragments.add(f);
            }
        }
    }

    public void update(float delta) {
        for (int i = fragments.size - 1; i >= 0; i--) {
            Fragment f = fragments.get(i);
            f.life -= delta;
            if (f.life <= 0f) { fragments.removeIndex(i); continue; }
            f.x += f.vx * delta; f.y += f.vy * delta;
            f.vy -= 310f * delta;
            f.rotation += f.spin * delta;
        }
    }

    public void render(SpriteBatch batch) {
        for (Fragment f : fragments) {
            float alpha = MathUtils.clamp(f.life / f.maxLife, 0f, 1f);
            batch.setColor(1f, 0.74f + alpha * 0.26f, 0.58f + alpha * 0.42f, alpha);
            batch.draw(f.region, f.x, f.y, f.width / 2f, f.height / 2f,
                f.width, f.height, 1f, 1f, f.rotation);
        }
        batch.setColor(Color.WHITE);
    }
}
