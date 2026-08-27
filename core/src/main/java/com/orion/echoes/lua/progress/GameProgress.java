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
        preferences.putFloat("save.oxygen", status.getOxygen());
        preferences.putFloat("save.energy", status.getEnergy());
        preferences.putFloat("save.health", status.getHealth());
        preferences.putInteger("save.ice", status.getIce());
        preferences.putInteger("save.water", status.getWater());
        preferences.putInteger("save.fuel", status.getFuel());
        preferences.putBoolean("save.weapon", mission.hasWeapon());
        preferences.putInteger("save.kills", mission.getEnemiesDefeated());
        preferences.putLong("save.worldSeed", mission.getWorldSeed());
        for (int i = 0; i < MissionState.MARS_SATELLITE_TARGET; i++) {
            preferences.putBoolean("save.marsSite." + i, mission.isMarsSiteScanned(i));
        }
        for (ItemType type : ItemType.values()) {
            preferences.putInteger("save.item." + type.name(), mission.getCount(type));
        }
        for (RepairType type : RepairType.values()) {
            preferences.putBoolean("save.repair." + type.name(), mission.isRepaired(type));
        }
        if (worldItems != null) {
            preferences.putInteger("save.worldItems", worldItems.size);
            for (int i = 0; i < worldItems.size; i++) {
                preferences.putBoolean("save.worldItem." + i, worldItems.get(i).isCollected());
            }
        }
        if (enemies != null) {
            preferences.putInteger("save.enemies", enemies.size);
            for (int i = 0; i < enemies.size; i++) {
                preferences.putBoolean("save.enemyDefeated." + i, !enemies.get(i).isAlive());
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
        }
        for (RepairType type : RepairType.values()) {
            mission.restoreRepair(type, preferences.getBoolean("save.repair." + type.name(), false));
        }
        mission.restoreWeapon(preferences.getBoolean("save.weapon", false));
        mission.restoreEnemiesDefeated(preferences.getInteger("save.kills", 0));
        mission.restoreWorldSeed(preferences.getLong("save.worldSeed", mission.getWorldSeed()));
        for (int i = 0; i < MissionState.MARS_SATELLITE_TARGET; i++) {
            mission.restoreMarsSite(i, preferences.getBoolean("save.marsSite." + i, false));
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

    public void restoreWorld(Array<CollectibleItem> items, Array<Enemy> enemies) {
        int itemCount = Math.min(items.size, preferences.getInteger("save.worldItems", 0));
        for (int i = 0; i < itemCount; i++) {
            if (preferences.getBoolean("save.worldItem." + i, false)) items.get(i).collect();
        }
        int enemyCount = Math.min(enemies.size, preferences.getInteger("save.enemies", 0));
        for (int i = 0; i < enemyCount; i++) {
            if (preferences.getBoolean("save.enemyDefeated." + i, false)) enemies.get(i).defeat();
        }
    }

    public void clearSavedMission() {
        preferences.putBoolean("save.exists", false);
        preferences.flush();
    }
}
