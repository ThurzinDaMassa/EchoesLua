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

        if (!collides(player, obstacles)) {
            return false;
        }

        float currentX = player.getX();
        float currentY = player.getY();
        float previousX = player.getPreviousPosition().x;
        float previousY = player.getPreviousPosition().y;

        // Tenta preservar um eixo para o jogador deslizar ao longo da rocha.
        player.setPosition(previousX, currentY);
        if (collides(player, obstacles)) {
            player.setPosition(currentX, previousY);
            if (collides(player, obstacles)) {
                player.setPosition(previousX, previousY);
            }
        }

        audio.playRockImpact();
        return true;
    }

    private boolean collides(Player player, Array<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (player.getBounds().overlaps(obstacle.getBounds())) {
                return true;
            }
        }
        return false;
    }
}
