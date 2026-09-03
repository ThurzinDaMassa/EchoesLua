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
        state.markIceProcessed();
        for (int i = 0; i < MissionState.LUNAR_ENEMY_TARGET - 1; i++) {
            state.recordEnemyDefeated();
        }
        assertFalse(state.isPortalReady(50f));
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
    void tracksIndependentMarsSatellites() {
        MissionState state = new MissionState();
        assertTrue(state.scanMarsSite(2));
        assertFalse(state.scanMarsSite(2));
        assertTrue(state.scanMarsSite(0));
        assertEquals(2, state.getMarsSitesScanned());
        assertTrue(state.isMarsSiteScanned(0));
        assertFalse(state.isMarsSiteScanned(1));
    }

    @Test
    void requiresAllFourMarsSatellites() {
        MissionState state = new MissionState();
        for (int i = 0; i < MissionState.MARS_SATELLITE_TARGET; i++) {
            assertTrue(state.repairMarsSatellite(i));
        }
        assertEquals(4, state.getMarsSatellitesRepaired());
        assertTrue(state.isMarsSiteScanned(3));
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

    @Test
    void inventoryTracksEveryCollectedItem() {
        MissionState state = new MissionState();
        state.collect(ItemType.OXYGEN);
        state.collect(ItemType.OXYGEN);
        state.collect(ItemType.MEDKIT);
        state.collect(ItemType.ICE_ROCK);
        assertEquals(2, state.getCount(ItemType.OXYGEN));
        assertEquals(1, state.getCount(ItemType.MEDKIT));
        assertEquals(1, state.getCount(ItemType.ICE_ROCK));
        assertTrue(state.consumeItem(ItemType.ICE_ROCK, 1));
        assertEquals(0, state.getCount(ItemType.ICE_ROCK));
    }

    @Test
    void titanPortalRequiresDialogueAndExactlyOneProofPath() {
        MissionState state = new MissionState();
        assertFalse(state.isTitanPortalUnlocked());
        state.completeTitanDialogue();
        assertFalse(state.isTitanPortalUnlocked());
        state.registerTitanCombatProof();
        assertTrue(state.isTitanPortalUnlocked());
    }

    @Test
    void methaneSampleIsConsumedWhenItUnlocksTitan() {
        MissionState state = new MissionState();
        state.collect(ItemType.METHANE_SAMPLE);
        assertFalse(state.deliverMethaneSample());
        state.completeTitanDialogue();
        assertTrue(state.deliverMethaneSample());
        assertEquals(0, state.getCount(ItemType.METHANE_SAMPLE));
        assertTrue(state.isTitanPortalUnlocked());
    }

    @Test
    void lunarIceProcessingIsARequiredMissionStage() {
        MissionState state = new MissionState();
        for (RepairType type : RepairType.values()) {
            state.collect(type.getRequiredPart());
            assertTrue(state.repair(type));
        }
        state.collect(ItemType.WEAPON_PART_A);
        state.collect(ItemType.WEAPON_PART_B);
        state.collect(ItemType.WEAPON_PART_C);
        assertTrue(state.craftWeapon());
        for (int i = 0; i < MissionState.LUNAR_ENEMY_TARGET; i++) state.recordEnemyDefeated();
        assertEquals(2, state.getLunarStage());
        assertEquals(ItemType.ICE_ROCK, state.getRequestedItem());
        assertFalse(state.isPortalReady(80f));
        state.markIceProcessed();
        assertTrue(state.isPortalReady(80f));
    }

    @Test
    void titanCoreMustBeCarriedBackAndInstalled() {
        MissionState state = new MissionState();
        state.recordTitanEnemyDefeated();
        assertFalse(state.isTitanCoreInstalled());
        assertFalse(state.installTitanCore());
        state.collect(ItemType.TITAN_CORE);
        assertTrue(state.installTitanCore());
        assertTrue(state.isTitanCoreInstalled());
        assertEquals(0, state.getCount(ItemType.TITAN_CORE));
    }
}
