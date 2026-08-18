package com.orion.echoes.lua.systems;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;

public class MissionSystem {

    private static final int REQUIRED_WATER =
        2;

    private static final int REQUIRED_FUEL =
        2;

    private boolean objectivesComplete;

    private boolean missionComplete;

    private boolean activationEffectPlayed;

    public MissionSystem() {

        objectivesComplete = false;

        missionComplete = false;

        activationEffectPlayed = false;
    }

    public void update(
        Player player,
        PlayerStatus status,
        Portal portal,
        ParticleManager particleManager,
        AudioManager audio
    ) {

        if (
            missionComplete
        ) {

            return;
        }

        if (
            !objectivesComplete
        ) {

            boolean waterComplete =
                status.getWater()
                    >= REQUIRED_WATER;

            boolean fuelComplete =
                status.getFuel()
                    >= REQUIRED_FUEL;

            if (
                waterComplete
                    &&
                    fuelComplete
            ) {

                objectivesComplete = true;

                portal.activate();

                if (
                    !activationEffectPlayed
                ) {

                    particleManager
                        .emitPortalActivationBurst(
                            portal.getCenterX(),
                            portal.getCenterY()
                        );

                    audio.playPortalActivation();

                    activationEffectPlayed = true;
                }
            }
        }

        if (
            objectivesComplete
                &&
                portal.overlaps(
                    player
                )
        ) {

            missionComplete = true;
        }
    }

    public int getRequiredWater() {
        return REQUIRED_WATER;
    }

    public int getRequiredFuel() {
        return REQUIRED_FUEL;
    }

    public boolean areObjectivesComplete() {
        return objectivesComplete;
    }

    public boolean isMissionComplete() {
        return missionComplete;
    }
}
