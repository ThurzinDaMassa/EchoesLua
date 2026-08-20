package com.orion.echoes.lua.progress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

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
}
