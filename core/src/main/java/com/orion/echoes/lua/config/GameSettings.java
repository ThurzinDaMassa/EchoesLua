package com.orion.echoes.lua.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

import com.orion.echoes.lua.audio.AudioManager;

public class GameSettings {
    private static final String PREFERENCES_NAME = "lunar-echoes-settings";

    private final Preferences preferences;
    private Difficulty difficulty;
    private float masterVolume;
    private float musicVolume;
    private float soundVolume;
    private boolean muted;
    private boolean fullscreen;

    public GameSettings() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        difficulty = readDifficulty(preferences.getString("difficulty", Difficulty.STANDARD.name()));
        masterVolume = preferences.getFloat("masterVolume", 1f);
        musicVolume = preferences.getFloat("musicVolume", 0.36f);
        soundVolume = preferences.getFloat("soundVolume", 0.72f);
        muted = preferences.getBoolean("muted", false);
        fullscreen = preferences.getBoolean("fullscreen", false);
    }

    private Difficulty readDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return Difficulty.STANDARD;
        }
    }

    public void applyTo(AudioManager audio) {
        audio.setMasterVolume(masterVolume);
        audio.setMusicVolume(musicVolume);
        audio.setSoundVolume(soundVolume);
        audio.setMuted(muted);
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        save();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void adjustMasterVolume(float amount) {
        masterVolume = MathUtils.clamp(masterVolume + amount, 0f, 1f);
        save();
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        applyDisplayMode();
        save();
    }

    public void applyDisplayMode() {
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        save();
    }

    private void save() {
        preferences
            .putString("difficulty", difficulty.name())
            .putFloat("masterVolume", masterVolume)
            .putFloat("musicVolume", musicVolume)
            .putFloat("soundVolume", soundVolume)
            .putBoolean("muted", muted)
            .putBoolean("fullscreen", fullscreen)
            .flush();
    }
}
