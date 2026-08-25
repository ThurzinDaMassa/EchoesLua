package com.orion.echoes.lua.progress;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.enums.RepairType;

class MissionStateTest {
    @Test
    void completesTheDocumentedPortalRequirements() {
        MissionState state = new MissionState();
        state.collect(ItemType.ANTENNA_PART);
        state.collect(ItemType.ENERGY_PART);
        state.collect(ItemType.EXTRACTION_PART);
        state.collect(ItemType.GREENHOUSE_PART);
        assertTrue(state.repair(RepairType.COMMUNICATION));
        assertTrue(state.repair(RepairType.ENERGY));
        assertTrue(state.repair(RepairType.EXTRACTION));
        assertTrue(state.repair(RepairType.GREENHOUSE));

        state.collect(ItemType.WEAPON_PART_A);
        state.collect(ItemType.WEAPON_PART_B);
        state.collect(ItemType.WEAPON_PART_C);
        assertTrue(state.craftWeapon());
        state.recordEnemyDefeated();

        assertTrue(state.isPortalReady(50f));
        assertFalse(state.isPortalReady(20f));
    }

    @Test
    void repairAndCraftConsumeTheirParts() {
        MissionState state = new MissionState();
        state.collect(ItemType.GREENHOUSE_PART);
        assertTrue(state.repair(RepairType.GREENHOUSE));
        assertEquals(0, state.getCount(ItemType.GREENHOUSE_PART));
        assertFalse(state.repair(RepairType.GREENHOUSE));

        state.collect(ItemType.WEAPON_PART_A);
        state.collect(ItemType.WEAPON_PART_B);
        assertFalse(state.craftWeapon());
        assertFalse(state.hasWeapon());
    }

    @Test
    void tracksIndependentMarsResearchSites() {
        MissionState state = new MissionState();
        assertTrue(state.scanMarsSite(2));
        assertFalse(state.scanMarsSite(2));
        assertTrue(state.scanMarsSite(0));
        assertEquals(2, state.getMarsSitesScanned());
        assertTrue(state.isMarsSiteScanned(0));
        assertFalse(state.isMarsSiteScanned(1));
    }

    @Test
    void explainsBlockedActionsWithoutChangingProgress() {
        MissionState state = new MissionState();
        assertFalse(state.repair(RepairType.COMMUNICATION));
        assertTrue(state.getLastMessage().contains("antena"));
        assertFalse(state.craftWeapon());
        assertTrue(state.getLastMessage().contains("0/3"));
        assertEquals(0, state.getRepairCount());
        assertFalse(state.hasWeapon());
    }

    @Test
    void requestsTheExactMissingMissionItem() {
        MissionState state = new MissionState();
        assertEquals(ItemType.ANTENNA_PART, state.getRequestedItem());
        state.collect(ItemType.ANTENNA_PART);
        assertTrue(state.repair(RepairType.COMMUNICATION));
        assertEquals(ItemType.ENERGY_PART, state.getRequestedItem());
    }
}
