package com.orion.echoes.lua.progress;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.orion.echoes.lua.config.Difficulty;

class MissionScoreTest {
    @Test
    void rewardsSpeedResourcesAndRemainingOxygen() {
        int carefulRun = MissionScore.calculate(120f, 8, 70f, Difficulty.STANDARD);
        int slowRun = MissionScore.calculate(260f, 4, 20f, Difficulty.STANDARD);
        assertTrue(carefulRun > slowRun);
    }

    @Test
    void difficultyAppliesRiskMultiplier() {
        int explorer = MissionScore.calculate(150f, 6, 50f, Difficulty.EXPLORER);
        int survivor = MissionScore.calculate(150f, 6, 50f, Difficulty.SURVIVOR);
        assertTrue(survivor > explorer);
    }
}
