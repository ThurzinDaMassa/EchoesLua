package com.orion.echoes.lua.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.orion.echoes.lua.enums.ItemType;
import com.orion.echoes.lua.enums.RepairType;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.entities.CollectibleItem;
import com.orion.echoes.lua.entities.Enemy;
import com.orion.echoes.lua.entities.Player;
import com.badlogic.gdx.utils.Array;

public class GameProgress {
    private static final String PREFERENCES_NAME = "lunar-echoes-progress";

    private final Preferences preferences;

    public GameProgress() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }

    public int getBestScore() {
        return preferences.getInteger("bestScore", 0);
    }

    public float getBestTime() {
        return preferences.getFloat("bestTime", 0f);
    }

    public boolean recordVictory(int score, float missionTime) {
        boolean newRecord = score > getBestScore();
        if (newRecord) {
            preferences.putInteger("bestScore", score);
        }

        float bestTime = getBestTime();
        if (bestTime <= 0f || missionTime < bestTime) {
            preferences.putFloat("bestTime", missionTime);
        }

        preferences.putInteger("missionsCompleted",
            preferences.getInteger("missionsCompleted", 0) + 1);
        preferences.flush();
        return newRecord;
    }

    public void saveMission(MissionState mission, PlayerStatus status,
                            String scene, float missionTime, int collectedItems,
                            Player player, Array<CollectibleItem> worldItems,
                            Array<Enemy> enemies) {
        preferences.putBoolean("save.exists", true);
        preferences.putString("save.scene", scene);
        preferences.putFloat("save.time", missionTime);
        preferences.putInteger("save.collected", collectedItems);
        preferences.putFloat("save.playerX", player.getX());
        preferences.putFloat("save.playerY", player.getY());
        preferences.putFloat("save.position." + scene + ".x", player.getX());
        preferences.putFloat("save.position." + scene + ".y", player.getY());
        preferences.putFloat("save.oxygen", status.getOxygen());
        preferences.putFloat("save.energy", status.getEnergy());
        preferences.putFloat("save.health", status.getHealth());
        preferences.putInteger("save.ice", status.getIce());
        preferences.putInteger("save.water", status.getWater());
        preferences.putInteger("save.fuel", status.getFuel());
        preferences.putBoolean("save.weapon", mission.hasWeapon());
        preferences.putInteger("save.magazine", mission.getMagazineAmmo());
        preferences.putBoolean("save.marsStorage", mission.hasMarsStorage());
        preferences.putInteger("save.kills", mission.getEnemiesDefeated());
        preferences.putLong("save.worldSeed", mission.getWorldSeed());
        preferences.putBoolean("save.titan.dialogue", mission.isTitanDialogueComplete());
        preferences.putBoolean("save.titan.combatProof", mission.hasTitanCombatProof());
        preferences.putBoolean("save.titan.sampleDelivered", mission.isMethaneSampleDelivered());
        preferences.putBoolean("save.titan.entered", mission.hasEnteredTitan());
        preferences.putBoolean("save.titan.enemyDefeated", mission.isTitanEnemyDefeated());
        preferences.putBoolean("save.titan.coreInstalled", mission.isTitanCoreInstalled());
        preferences.putBoolean("save.lunar.iceProcessed", mission.isIceProcessed());
        for (int i = 0; i < MissionState.MARS_SATELLITE_TARGET; i++) {
            preferences.putBoolean("save.marsSite." + i, mission.isMarsSiteScanned(i));
        }
        for (int i = 0; i < MissionState.LUNAR_CHEST_COUNT; i++) {
            preferences.putBoolean("save.lunarChest." + i, mission.isChestOpened(false, i));
        }
        for (int i = 0; i < MissionState.MARS_CHEST_COUNT; i++) {
            preferences.putBoolean("save.marsChest." + i, mission.isChestOpened(true, i));
        }
        for (int i = 0; i < MissionState.TITAN_CHEST_COUNT; i++) {
            preferences.putBoolean("save.titanChest." + i, mission.isTitanChestOpened(i));
        }
        putItemType("save.equipped.helmet", mission.getEquippedHelmet());
        putItemType("save.equipped.chest", mission.getEquippedChest());
        putItemType("save.equipped.boots", mission.getEquippedBoots());
        putItemType("save.equipped.mining", mission.getEquippedMiningTool());
        putItemType("save.equipped.repair", mission.getEquippedRepairTool());
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++) {
            putItemType("save.inventorySlot." + i, mission.getInventorySlot(i));
        }
        for (int i = 0; i < MissionState.MARS_STORAGE_SIZE; i++) {
            putItemType("save.storageSlot." + i, mission.getStorageSlot(i));
        }
        for (ItemType type : ItemType.values()) {
            preferences.putInteger("save.item." + type.name(), mission.getCount(type));
            preferences.putInteger("save.stored." + type.name(), mission.getStoredCount(type));
        }
        for (RepairType type : RepairType.values()) {
            preferences.putBoolean("save.repair." + type.name(), mission.isRepaired(type));
        }
        if (worldItems != null) {
            String worldKey = "save.world." + scene;
            preferences.putInteger(worldKey + ".items", worldItems.size);
            for (int i = 0; i < worldItems.size; i++) {
                preferences.putBoolean(worldKey + ".item." + i, worldItems.get(i).isCollected());
            }
        }
        if (enemies != null) {
            String worldKey = "save.world." + scene;
            preferences.putInteger(worldKey + ".enemies", enemies.size);
            for (int i = 0; i < enemies.size; i++) {
                preferences.putBoolean(worldKey + ".enemyDefeated." + i, !enemies.get(i).isAlive());
            }
        }
        preferences.flush();
    }

    public boolean hasSavedMission() {
        return preferences.getBoolean("save.exists", false);
    }

    public MissionState loadMissionState() {
        MissionState mission = new MissionState();
        for (ItemType type : ItemType.values()) {
            mission.restoreCount(type, preferences.getInteger("save.item." + type.name(), 0));
            mission.restoreStoredCount(type, preferences.getInteger("save.stored." + type.name(), 0));
        }
        for (RepairType type : RepairType.values()) {
            mission.restoreRepair(type, preferences.getBoolean("save.repair." + type.name(), false));
        }
        mission.restoreWeapon(preferences.getBoolean("save.weapon", false));
        mission.restoreMagazine(preferences.getInteger("save.magazine",
            mission.hasWeapon() ? MissionState.MAGAZINE_SIZE : 0));
        mission.restoreMarsStorage(preferences.getBoolean("save.marsStorage", false));
        mission.restoreEnemiesDefeated(preferences.getInteger("save.kills", 0));
        mission.restoreWorldSeed(preferences.getLong("save.worldSeed", mission.getWorldSeed()));
        mission.restoreTitanProgress(
            preferences.getBoolean("save.titan.dialogue", false),
            preferences.getBoolean("save.titan.combatProof", false),
            preferences.getBoolean("save.titan.sampleDelivered", false),
            preferences.getBoolean("save.titan.entered", false),
            preferences.getBoolean("save.titan.enemyDefeated", false));
        mission.restoreExtendedProgress(
            preferences.getBoolean("save.lunar.iceProcessed", false),
            preferences.getBoolean("save.titan.coreInstalled", false));
        for (int i = 0; i < MissionState.MARS_SATELLITE_TARGET; i++) {
            mission.restoreMarsSite(i, preferences.getBoolean("save.marsSite." + i, false));
        }
        for (int i = 0; i < MissionState.LUNAR_CHEST_COUNT; i++) {
            mission.restoreChest(false, i, preferences.getBoolean("save.lunarChest." + i, false));
        }
        for (int i = 0; i < MissionState.MARS_CHEST_COUNT; i++) {
            mission.restoreChest(true, i, preferences.getBoolean("save.marsChest." + i, false));
        }
        for (int i = 0; i < MissionState.TITAN_CHEST_COUNT; i++) {
            mission.restoreTitanChest(i, preferences.getBoolean("save.titanChest." + i, false));
        }
        mission.restoreEquipment(
            readItemType("save.equipped.helmet"), readItemType("save.equipped.chest"),
            readItemType("save.equipped.boots"), readItemType("save.equipped.mining"),
            readItemType("save.equipped.repair"));
        for (int i = 0; i < MissionState.INVENTORY_SIZE; i++) {
            String key = "save.inventorySlot." + i;
            if (preferences.contains(key)) mission.restoreInventorySlot(i, readItemType(key));
        }
        for (int i = 0; i < MissionState.MARS_STORAGE_SIZE; i++) {
            String key = "save.storageSlot." + i;
            if (preferences.contains(key)) mission.restoreStorageSlot(i, readItemType(key));
        }
        return mission;
    }

    public PlayerStatus loadPlayerStatus() {
        PlayerStatus status = new PlayerStatus();
        status.restore(
            preferences.getFloat("save.oxygen", 78f),
            preferences.getFloat("save.energy", 72f),
            preferences.getFloat("save.health", 100f),
            preferences.getInteger("save.ice", 0),
            preferences.getInteger("save.water", 0),
            preferences.getInteger("save.fuel", 0)
        );
        return status;
    }

    public String getSavedScene() {
        return preferences.getString("save.scene", "LUA");
    }

    public float getSavedMissionTime() {
        return preferences.getFloat("save.time", 0f);
    }

    public int getSavedCollectedItems() {
        return preferences.getInteger("save.collected", 0);
    }

    public float getSavedPlayerX(float fallback) {
        return preferences.getFloat("save.playerX", fallback);
    }

    public float getSavedPlayerY(float fallback) {
        return preferences.getFloat("save.playerY", fallback);
    }

    public void restoreWorld(String scene, Array<CollectibleItem> items, Array<Enemy> enemies) {
        String worldKey = "save.world." + scene;
        String itemCountKey = worldKey + ".items";
        int itemCount = Math.min(items.size, preferences.getInteger(itemCountKey,
            preferences.getInteger("save.worldItems", 0)));
        for (int i = 0; i < itemCount; i++) {
            String key = worldKey + ".item." + i;
            if (preferences.getBoolean(key,
                preferences.getBoolean("save.worldItem." + i, false))) items.get(i).collect();
        }
        int enemyCount = Math.min(enemies.size, preferences.getInteger(worldKey + ".enemies",
            preferences.getInteger("save.enemies", 0)));
        for (int i = 0; i < enemyCount; i++) {
            String key = worldKey + ".enemyDefeated." + i;
            if (preferences.getBoolean(key,
                preferences.getBoolean("save.enemyDefeated." + i, false))) enemies.get(i).defeat();
        }
    }

    public void restoreWorld(Array<CollectibleItem> items, Array<Enemy> enemies) {
        restoreWorld(getSavedScene(), items, enemies);
    }

    public void clearSavedMission() {
        preferences.putBoolean("save.exists", false);
        preferences.flush();
    }

    private void putItemType(String key, ItemType type) {
        preferences.putString(key, type == null ? "" : type.name());
    }

    private ItemType readItemType(String key) {
        String value = preferences.getString(key, "");
        if (value.isBlank()) return null;
        try {
            return ItemType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
