package com.orion.echoes.lua.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.utils.GameConstants;

/** Interactive fabrication station used only inside the pressurized base. */
public final class CraftingWorkbench {
    private final Sprite sprite;
    private final Rectangle collisionBounds;
    private final Rectangle interactionBounds;
    private float cooldown;

    public CraftingWorkbench(float x, float y, float width, float height, GameAssets assets) {
        sprite = new Sprite(assets.getCraftingWorkbench());
        sprite.setPosition(x, y);
        sprite.setSize(width, height);
        collisionBounds = new Rectangle(x + width * 0.10f, y + height * 0.08f,
            width * 0.80f, height * 0.48f);
        interactionBounds = new Rectangle(x - 58f, y - 50f, width + 116f, height + 96f);
    }

    public void update(float delta) {
        cooldown = Math.max(0f, cooldown - delta);
    }

    public boolean isPlayerNear(Player player) {
        return interactionBounds.overlaps(player.getBounds());
    }

    public boolean blocks(Player player) {
        return collisionBounds.overlaps(player.getBounds());
    }

    public boolean interact(Player player, MissionState mission, PlayerStatus status,
                            ParticleManager particles, AudioManager audio) {
        if (cooldown > 0f) return false;

        if (mission.canCraftWeapon()) return craftWeapon(player, mission, particles, audio);
        if (canProcessIce(status)) return processIce(player, mission, status, particles, audio);

        mission.notifyAction(mission.hasWeapon()
            ? "Bancada pronta // colete gelo para processar"
            : "Bancada // componentes da arma " + mission.getWeaponPartCount() + "/3");
        cooldown = 0.25f;
        return false;
    }

    public boolean craftWeapon(Player player, MissionState mission,
                               ParticleManager particles, AudioManager audio) {
        if (cooldown > 0f || !mission.craftWeapon()) return false;
        finishCraft(player, particles, audio, true);
        return true;
    }

    public boolean canProcessIce(PlayerStatus status) {
        return status.getIce() >= GameConstants.ICE_PROCESS_COST;
    }

    public boolean processIce(Player player, MissionState mission, PlayerStatus status,
                              ParticleManager particles, AudioManager audio) {
        if (cooldown > 0f || !status.removeIce(GameConstants.ICE_PROCESS_COST)) return false;
        mission.consumeItem(com.orion.echoes.lua.enums.ItemType.ICE_ROCK,
            GameConstants.ICE_PROCESS_COST);
        status.addWater(GameConstants.WATER_PER_ICE);
        status.addFuel(GameConstants.FUEL_PER_ICE);
        status.addOxygen(GameConstants.OXYGEN_PER_ICE);
        mission.notifyAction("Gelo processado // +agua, +combustivel e +O2");
        finishCraft(player, particles, audio, false);
        return true;
    }

    private void finishCraft(Player player, ParticleManager particles,
                             AudioManager audio, boolean weapon) {
        player.triggerCraftAnimation();
        particles.emitProcessingBurst(getCenterX(), getCenterY());
        if (weapon) audio.playCraft();
        else audio.playIceProcessing();
        cooldown = 0.55f;
    }

    public void render(SpriteBatch batch, boolean playerNear) {
        sprite.setColor(playerNear ? new Color(0.86f, 1f, 1f, 1f) : Color.WHITE);
        sprite.draw(batch);
        sprite.setColor(Color.WHITE);
    }

    public float getCenterX() {
        return sprite.getX() + sprite.getWidth() / 2f;
    }

    public float getCenterY() {
        return sprite.getY() + sprite.getHeight() * 0.58f;
    }
}
