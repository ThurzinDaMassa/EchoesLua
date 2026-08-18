package com.orion.echoes.lua.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.utils.GameConstants;

public class IceProcessor {

    private float cooldownTimer;

    private String lastMessage;

    public IceProcessor() {

        cooldownTimer = 0f;

        lastMessage =
            "Colete gelo e volte para a base.";
    }

    public void update(
        float delta,
        Player player,
        LunarBase lunarBase,
        PlayerStatus status,
        ParticleManager particleManager,
        AudioManager audio
    ) {

        updateCooldown(
            delta
        );

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.E
            )
        ) {

            tryProcess(
                player,
                lunarBase,
                status,
                particleManager,
                audio
            );
        }
    }

    private void updateCooldown(
        float delta
    ) {

        if (
            cooldownTimer <= 0f
        ) {

            return;
        }

        cooldownTimer -= delta;

        if (
            cooldownTimer < 0f
        ) {

            cooldownTimer = 0f;
        }
    }

    private void tryProcess(
        Player player,
        LunarBase lunarBase,
        PlayerStatus status,
        ParticleManager particleManager,
        AudioManager audio
    ) {

        if (
            cooldownTimer > 0f
        ) {

            return;
        }

        if (
            !lunarBase
                .isPlayerInside(
                    player
                )
        ) {

            lastMessage =
                "Voce precisa estar na base para processar gelo.";

            return;
        }

        if (
            status.getIce()
                <
                GameConstants
                    .ICE_PROCESS_COST
        ) {

            lastMessage =
                "Sem gelo no inventario.";

            return;
        }

        boolean removed =
            status.removeIce(
                GameConstants
                    .ICE_PROCESS_COST
            );

        if (!removed) {

            lastMessage =
                "Nao foi possivel processar o gelo.";

            return;
        }

        status.addWater(
            GameConstants
                .WATER_PER_ICE
        );

        status.addFuel(
            GameConstants
                .FUEL_PER_ICE
        );

        status.addOxygen(
            GameConstants
                .OXYGEN_PER_ICE
        );

        particleManager
            .emitProcessingBurst(
                lunarBase.getCenterX(),
                lunarBase.getCenterY()
            );

        audio.playIceProcessing();

        cooldownTimer =
            GameConstants
                .ICE_PROCESS_COOLDOWN;

        lastMessage =
            "Gelo processado: +agua, +O2 e +combustivel.";
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
