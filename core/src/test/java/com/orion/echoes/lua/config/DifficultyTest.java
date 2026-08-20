package com.orion.echoes.lua.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DifficultyTest {
    @Test
    void cyclesInBothDirections() {
        assertEquals(Difficulty.STANDARD, Difficulty.EXPLORER.next());
        assertEquals(Difficulty.EXPLORER, Difficulty.STANDARD.previous());
        assertEquals(Difficulty.EXPLORER, Difficulty.SURVIVOR.next());
    }

    @Test
    void harderModesDemandMoreAndScoreMore() {
        assertTrue(Difficulty.SURVIVOR.getRequiredWater()
            > Difficulty.STANDARD.getRequiredWater());
        assertTrue(Difficulty.SURVIVOR.getOxygenConsumptionMultiplier()
            > Difficulty.STANDARD.getOxygenConsumptionMultiplier());
        assertTrue(Difficulty.SURVIVOR.getScoreMultiplier()
            > Difficulty.STANDARD.getScoreMultiplier());
    }
}
