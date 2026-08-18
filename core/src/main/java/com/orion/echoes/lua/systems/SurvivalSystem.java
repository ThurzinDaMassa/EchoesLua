package com.orion.echoes.lua.systems;

import com.orion.echoes.lua.entities.LunarBase;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.utils.GameConstants;

public class SurvivalSystem {

    private boolean missionFailed;

    public SurvivalSystem() {

        missionFailed = false;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta,
        Player player,
        LunarBase lunarBase,
        PlayerStatus status
    ) {

        if (missionFailed) {
            return;
        }

        /*
         * Dentro da base, o jogador esta protegido.
         *
         * A propria LunarBase ja faz a recarga.
         * Portanto, aqui apenas evitamos o consumo.
         */
        if (
            lunarBase.isPlayerInside(
                player
            )
        ) {

            return;
        }

        consumeOxygen(
            delta,
            status
        );

        checkMissionFailure(
            status
        );
    }

    // =========================================================
    // CONSUMO
    // =========================================================

    private void consumeOxygen(
        float delta,
        PlayerStatus status
    ) {

        float consumption =
            GameConstants.OXYGEN_CONSUMPTION_RATE
                * delta;

        status.removeOxygen(
            consumption
        );
    }

    // =========================================================
    // GAME OVER
    // =========================================================

    private void checkMissionFailure(
        PlayerStatus status
    ) {

        if (
            status.getOxygen() <= 0f
        ) {

            missionFailed = true;

            status.setOxygen(0f);
        }
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public boolean isMissionFailed() {
        return missionFailed;
    }
}
