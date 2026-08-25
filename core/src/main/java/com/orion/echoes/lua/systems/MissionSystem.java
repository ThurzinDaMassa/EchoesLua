package com.orion.echoes.lua.systems;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.entities.Portal;
import com.orion.echoes.lua.config.Difficulty;
import com.orion.echoes.lua.progress.MissionState;

public class MissionSystem {

    private final int requiredWater;

    private final int requiredFuel;

    private boolean objectivesComplete;

    private boolean missionComplete;

    private boolean activationEffectPlayed;

    private final MissionState state;

    public MissionSystem() {

        this(Difficulty.STANDARD, new MissionState());
    }

    public MissionSystem(Difficulty difficulty) {

        this(difficulty, new MissionState());
    }

    public MissionSystem(Difficulty difficulty, MissionState state) {

        objectivesComplete = false;

        missionComplete = false;

        activationEffectPlayed = false;

        this.state = state;

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

            if (state.isPortalReady(status.getOxygen())) {

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

    public MissionState getState() {
        return state;
    }
}
