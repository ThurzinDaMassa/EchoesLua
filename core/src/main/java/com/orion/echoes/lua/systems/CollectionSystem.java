package com.orion.echoes.lua.systems;

import com.badlogic.gdx.utils.Array;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.effects.ParticleManager;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.Player;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.progress.MissionState;
import com.orion.echoes.lua.utils.GameConstants;

public class CollectionSystem {

    public void update(
        Player player,
        PlayerStatus status,
        Array<CollectibleItem> items,
        ParticleManager particleManager,
        AudioManager audio,
        MissionState missionState
    ) {

        for (
            CollectibleItem item : items
        ) {

            if (
                item.isCollected()
            ) {

                continue;
            }

            if (
                !item.overlaps(
                    player
                )
            ) {

                continue;
            }

            collectItem(
                item,
                status,
                particleManager,
                audio,
                missionState
            );
        }
    }

    private void collectItem(
        CollectibleItem item,
        PlayerStatus status,
        ParticleManager particleManager,
        AudioManager audio,
        MissionState missionState
    ) {

        ItemType type =
            item.getType();

        switch (type) {

            case OXYGEN:

                status.addOxygen(
                    GameConstants
                        .OXYGEN_PICKUP_AMOUNT
                );

                break;

            case FOOD:

                status.addEnergy(
                    GameConstants
                        .FOOD_PICKUP_AMOUNT
                );

                break;

            case ICE_ROCK:

                status.addIce(
                    1
                );

                break;

            case MEDKIT:
                status.addHealth(GameConstants.MEDKIT_HEAL_AMOUNT);
                break;

            default:
                missionState.collect(type);
                break;
        }

        item.collect();

        particleManager
            .emitPickupBurst(
                type,
                item.getCenterX(),
                item.getCenterY()
            );

        audio.playPickup(
            type
        );
    }
}
