package com.orion.echoes.lua.systems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.orion.echoes.lua.utils.GameConstants;

class PlayerStatusTest {
    @Test
    void clampsOxygenAndEnergy() {
        PlayerStatus status = new PlayerStatus();
        status.addOxygen(999f);
        status.removeEnergy(999f);
        assertEquals(GameConstants.MAX_OXYGEN, status.getOxygen());
        assertEquals(0f, status.getEnergy());
    }

    @Test
    void neverConsumesIceThatDoesNotExist() {
        PlayerStatus status = new PlayerStatus();
        assertFalse(status.removeIce(1));
        status.addIce(2);
        assertTrue(status.removeIce(1));
        assertEquals(1, status.getIce());
    }
}
