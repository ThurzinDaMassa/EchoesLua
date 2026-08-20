package com.orion.echoes.lua;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.audio.AudioManager;
import com.orion.echoes.lua.config.GameSettings;
import com.orion.echoes.lua.progress.GameProgress;
import com.orion.echoes.lua.screens.MenuScreen;

public class LunarEchoesGame extends Game {

    private SpriteBatch batch;

    private GameAssets assets;

    private AudioManager audioManager;

    private GameSettings settings;

    private GameProgress progress;

    @Override
    public void create() {

        batch =
            new SpriteBatch();

        assets =
            new GameAssets();

        assets.loadAll();

        settings = new GameSettings();

        settings.applyDisplayMode();

        progress = new GameProgress();

        audioManager =
            new AudioManager();

        audioManager.load();

        settings.applyTo(audioManager);

        setScreen(
            new MenuScreen(
                this
            )
        );
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GameAssets getAssets() {
        return assets;
    }

    public AudioManager getAudio() {
        return audioManager;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public GameProgress getProgress() {
        return progress;
    }

    public void changeScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    @Override
    public void dispose() {

        if (
            getScreen() != null
        ) {

            getScreen().dispose();
        }

        if (
            audioManager != null
        ) {

            audioManager.dispose();
        }

        if (
            assets != null
        ) {

            assets.dispose();
        }

        if (
            batch != null
        ) {

            batch.dispose();
        }
    }
}
