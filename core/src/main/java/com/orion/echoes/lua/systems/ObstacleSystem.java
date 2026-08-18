package com.orion.echoes.lua.systems;

import com.badlogic.gdx.utils.Array;

import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.entities.Obstacle;
import com.orion.echoes.lua.entities.Player;

public class ObstacleSystem {

    public boolean handleCollisions(
        Player player,
        Array<Obstacle> obstacles,
        AudioManager audio
    ) {

        for (
            Obstacle obstacle : obstacles
        ) {

            if (
                player
                    .getBounds()
                    .overlaps(
                        obstacle
                            .getBounds()
                    )
            ) {

                player
                    .restorePreviousPosition();

                audio.playRockImpact();

                return true;
            }
        }

        return false;
    }
}
