package com.orion.echoes.lua.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
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
        loadTexture(AssetPaths.ASTRONAUT_TRIPLE_T);
        loadTexture(AssetPaths.ASTRONAUT_WINSTON);
        loadTexture(AssetPaths.ASTRONAUT_SHREK);
        loadTexture(AssetPaths.ASTRONAUT_NEON);

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
        loadTexture(AssetPaths.MEDKIT);

        loadTexture(
            AssetPaths.OBSTACLE
        );

        loadTexture(
            AssetPaths.PORTAL
        );

        loadTexture(
            AssetPaths.MARS_SURFACE
        );

        loadTexture(AssetPaths.ENEMY_SENTINEL);
        loadTexture(AssetPaths.MISSION_COMPONENT);
        loadTexture(AssetPaths.ANTENNA_PART);
        loadTexture(AssetPaths.ENERGY_PART);
        loadTexture(AssetPaths.EXTRACTION_PART);
        loadTexture(AssetPaths.GREENHOUSE_PART);
        loadTexture(AssetPaths.WEAPON_PART_A);
        loadTexture(AssetPaths.WEAPON_PART_B);
        loadTexture(AssetPaths.WEAPON_PART_C);
        loadTexture(AssetPaths.EVA_WEAPON);
        loadTexture(AssetPaths.ENERGY_PROJECTILE);
        loadTexture(AssetPaths.REPAIR_STATION);
        loadTexture(AssetPaths.MARS_BASE);
        loadTexture(AssetPaths.MARS_RESEARCH_SITE);

        manager.finishLoading();

        configureTextures();
    }

    private void loadTexture(
        String path
    ) {

        TextureLoader.TextureParameter parameters = new TextureLoader.TextureParameter();
        parameters.genMipMaps = true;
        parameters.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        parameters.magFilter = Texture.TextureFilter.Linear;
        manager.load(path, Texture.class, parameters);
    }

    private void configureTextures() {

        setLinear(
            AssetPaths.ASTRONAUT
        );
        setLinear(AssetPaths.ASTRONAUT_TRIPLE_T);
        setLinear(AssetPaths.ASTRONAUT_WINSTON);
        setLinear(AssetPaths.ASTRONAUT_SHREK);
        setLinear(AssetPaths.ASTRONAUT_NEON);

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
        setLinear(AssetPaths.MEDKIT);

        setLinear(
            AssetPaths.OBSTACLE
        );

        setLinear(
            AssetPaths.PORTAL
        );

        setLinear(
            AssetPaths.MARS_SURFACE
        );

        setLinear(AssetPaths.ENEMY_SENTINEL);
        setLinear(AssetPaths.MISSION_COMPONENT);
        setLinear(AssetPaths.ANTENNA_PART);
        setLinear(AssetPaths.ENERGY_PART);
        setLinear(AssetPaths.EXTRACTION_PART);
        setLinear(AssetPaths.GREENHOUSE_PART);
        setLinear(AssetPaths.WEAPON_PART_A);
        setLinear(AssetPaths.WEAPON_PART_B);
        setLinear(AssetPaths.WEAPON_PART_C);
        setLinear(AssetPaths.EVA_WEAPON);
        setLinear(AssetPaths.ENERGY_PROJECTILE);
        setLinear(AssetPaths.REPAIR_STATION);
        setLinear(AssetPaths.MARS_BASE);
        setLinear(AssetPaths.MARS_RESEARCH_SITE);
    }

    private void setLinear(
        String path
    ) {

        Texture texture =
            getTexture(path);

        texture.setFilter(
            Texture.TextureFilter.MipMapLinearLinear,
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

    public Texture getAstronaut(com.orion.echoes.lua.config.AstronautType type) {
        return switch (type) {
            case TRIPLE_T -> getTexture(AssetPaths.ASTRONAUT_TRIPLE_T);
            case WINSTON -> getTexture(AssetPaths.ASTRONAUT_WINSTON);
            case SHREK -> getTexture(AssetPaths.ASTRONAUT_SHREK);
            case NEON -> getTexture(AssetPaths.ASTRONAUT_NEON);
        };
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

    public Texture getMedkit() { return getTexture(AssetPaths.MEDKIT); }

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

    public Texture getMarsSurface() {
        return getTexture(AssetPaths.MARS_SURFACE);
    }

    public Texture getEnemySentinel() { return getTexture(AssetPaths.ENEMY_SENTINEL); }
    public Texture getMissionComponent() { return getTexture(AssetPaths.MISSION_COMPONENT); }
    public Texture getAntennaPart() { return getTexture(AssetPaths.ANTENNA_PART); }
    public Texture getEnergyPart() { return getTexture(AssetPaths.ENERGY_PART); }
    public Texture getExtractionPart() { return getTexture(AssetPaths.EXTRACTION_PART); }
    public Texture getGreenhousePart() { return getTexture(AssetPaths.GREENHOUSE_PART); }
    public Texture getWeaponPartA() { return getTexture(AssetPaths.WEAPON_PART_A); }
    public Texture getWeaponPartB() { return getTexture(AssetPaths.WEAPON_PART_B); }
    public Texture getWeaponPartC() { return getTexture(AssetPaths.WEAPON_PART_C); }
    public Texture getEvaWeapon() { return getTexture(AssetPaths.EVA_WEAPON); }
    public Texture getEnergyProjectile() { return getTexture(AssetPaths.ENERGY_PROJECTILE); }
    public Texture getRepairStation() { return getTexture(AssetPaths.REPAIR_STATION); }
    public Texture getMarsBase() { return getTexture(AssetPaths.MARS_BASE); }
    public Texture getMarsResearchSite() { return getTexture(AssetPaths.MARS_RESEARCH_SITE); }

    public void dispose() {

        manager.dispose();
    }
}
