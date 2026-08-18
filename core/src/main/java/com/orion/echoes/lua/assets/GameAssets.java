package com.orion.echoes.lua.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class GameAssets {

    private final AssetManager manager;

    public GameAssets() {

        manager = new AssetManager();
    }

    public void loadAll() {

        loadTexture(
            AssetPaths.ASTRONAUT
        );

        loadTexture(
            AssetPaths.LUNAR_BASE
        );

        loadTexture(
            AssetPaths.OXYGEN
        );

        loadTexture(
            AssetPaths.FOOD
        );

        loadTexture(
            AssetPaths.ICE_ROCK
        );

        loadTexture(
            AssetPaths.FUEL
        );

        loadTexture(
            AssetPaths.OBSTACLE
        );

        loadTexture(
            AssetPaths.PORTAL
        );

        manager.finishLoading();

        configureTextures();
    }

    private void loadTexture(
        String path
    ) {

        manager.load(
            path,
            Texture.class
        );
    }

    private void configureTextures() {

        setLinear(
            AssetPaths.ASTRONAUT
        );

        setLinear(
            AssetPaths.LUNAR_BASE
        );

        setLinear(
            AssetPaths.OXYGEN
        );

        setLinear(
            AssetPaths.FOOD
        );

        setLinear(
            AssetPaths.ICE_ROCK
        );

        setLinear(
            AssetPaths.FUEL
        );

        setLinear(
            AssetPaths.OBSTACLE
        );

        setLinear(
            AssetPaths.PORTAL
        );
    }

    private void setLinear(
        String path
    ) {

        Texture texture =
            getTexture(path);

        texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );
    }

    public Texture getTexture(
        String path
    ) {

        return manager.get(
            path,
            Texture.class
        );
    }


    public Texture getAstronaut() {
        return getTexture(
            AssetPaths.ASTRONAUT
        );
    }

    public Texture getLunarBase() {
        return getTexture(
            AssetPaths.LUNAR_BASE
        );
    }

    public Texture getOxygen() {
        return getTexture(
            AssetPaths.OXYGEN
        );
    }

    public Texture getFood() {
        return getTexture(
            AssetPaths.FOOD
        );
    }

    public Texture getIceRock() {
        return getTexture(
            AssetPaths.ICE_ROCK
        );
    }

    public Texture getFuel() {
        return getTexture(
            AssetPaths.FUEL
        );
    }

    public Texture getObstacle() {
        return getTexture(
            AssetPaths.OBSTACLE
        );
    }

    public Texture getPortal() {
        return getTexture(
            AssetPaths.PORTAL
        );
    }

    public void dispose() {

        manager.dispose();
    }
}
