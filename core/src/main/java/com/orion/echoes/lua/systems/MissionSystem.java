package com.orion.echoes.lua.systems;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.config.Difficulty;

public class MissionSystem {

    private final int requiredWater;

    private final int requiredFuel;

    private boolean objectivesComplete;

    private boolean missionComplete;

    private boolean activationEffectPlayed;

    public MissionSystem() {

        this(Difficulty.STANDARD);
    }

    public MissionSystem(Difficulty difficulty) {

        objectivesComplete = false;

        missionComplete = false;

        activationEffectPlayed = false;

        requiredWater = difficulty.getRequiredWater();

        requiredFuel = difficulty.getRequiredFuel();
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
                    >= requiredWater;

            boolean fuelComplete =
                status.getFuel()
                    >= requiredFuel;

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
        return requiredWater;
    }

    public int getRequiredFuel() {
        return requiredFuel;
    }

    public boolean areObjectivesComplete() {
        return objectivesComplete;
    }

    public boolean isMissionComplete() {
        return missionComplete;
    }
}
