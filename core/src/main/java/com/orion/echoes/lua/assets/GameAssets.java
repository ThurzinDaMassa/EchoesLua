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

        loadTexture(AssetPaths.ASTRONAUT_TRIPLE_T_LOCOMOTION);
        loadTexture(AssetPaths.ASTRONAUT_WINSTON_LOCOMOTION);
        loadTexture(AssetPaths.ASTRONAUT_SHREK_LOCOMOTION);
        loadTexture(AssetPaths.ASTRONAUT_NEON_LOCOMOTION);

        loadTexture(
            AssetPaths.LUNAR_BASE
        );
        loadTexture(AssetPaths.LUNAR_SURFACE);
        loadTexture(AssetPaths.BASE_INTERIOR);
        loadTexture(AssetPaths.CRAFTING_WORKBENCH);

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
        loadTexture(AssetPaths.MARS_OXYGEN);
        loadTexture(AssetPaths.MARS_FOOD);
        loadTexture(AssetPaths.MARS_MEDKIT);
        loadTexture(AssetPaths.MARS_ICE_SAMPLE);

        loadTexture(
            AssetPaths.OBSTACLE
        );

        loadTexture(
            AssetPaths.PORTAL
        );

        loadTexture(
            AssetPaths.MARS_SURFACE
        );
        loadTexture(AssetPaths.MARS_ROCK_SEDIMENTARY);
        loadTexture(AssetPaths.MARS_ROCK_BASALT);
        loadTexture(AssetPaths.MARS_ROCK_IRONSTONE);

        loadTexture(AssetPaths.ENEMY_SENTINEL);
        loadTexture(AssetPaths.ENEMY_MARS_SKIMMER);
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
        loadTexture(AssetPaths.LOOT_CHEST_CLOSED);
        loadTexture(AssetPaths.LOOT_CHEST_OPEN);
        loadTexture(AssetPaths.ALLOY_PLATE);
        loadTexture(AssetPaths.QUANTUM_CORE);
        loadTexture(AssetPaths.FIBER_MESH);
        loadTexture(AssetPaths.MINING_TOOL);
        loadTexture(AssetPaths.REPAIR_TOOL);
        loadTexture(AssetPaths.ARMOR_HELMET);
        loadTexture(AssetPaths.ARMOR_CHEST);
        loadTexture(AssetPaths.ARMOR_BOOTS);
        loadTexture(AssetPaths.ARMOR_OVERLAY_HELMET);
        loadTexture(AssetPaths.ARMOR_OVERLAY_CHEST);
        loadTexture(AssetPaths.ARMOR_OVERLAY_BOOTS);
        loadTexture(AssetPaths.REPAIR_STATION);
        loadTexture(AssetPaths.REPAIR_STATION_BROKEN);
        loadTexture(AssetPaths.REPAIR_HAMMER);
        loadTexture(AssetPaths.MARS_SATELLITE_BROKEN);
        loadTexture(AssetPaths.MARS_SATELLITE_REPAIRED);
        loadTexture(AssetPaths.MISSION_INTRO_DISTRESS);
        loadTexture(AssetPaths.MISSION_INTRO_FAILURE);
        loadTexture(AssetPaths.MISSION_INTRO_DEPLOYMENT);
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

        setLinear(AssetPaths.ASTRONAUT_TRIPLE_T_LOCOMOTION);
        setLinear(AssetPaths.ASTRONAUT_WINSTON_LOCOMOTION);
        setLinear(AssetPaths.ASTRONAUT_SHREK_LOCOMOTION);
        setLinear(AssetPaths.ASTRONAUT_NEON_LOCOMOTION);

        setLinear(
            AssetPaths.LUNAR_BASE
        );
        setLinear(AssetPaths.LUNAR_SURFACE);
        setLinear(AssetPaths.BASE_INTERIOR);
        setLinear(AssetPaths.CRAFTING_WORKBENCH);

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
        setLinear(AssetPaths.MARS_OXYGEN);
        setLinear(AssetPaths.MARS_FOOD);
        setLinear(AssetPaths.MARS_MEDKIT);
        setLinear(AssetPaths.MARS_ICE_SAMPLE);

        setLinear(
            AssetPaths.OBSTACLE
        );

        setLinear(
            AssetPaths.PORTAL
        );

        setLinear(
            AssetPaths.MARS_SURFACE
        );
        setLinear(AssetPaths.MARS_ROCK_SEDIMENTARY);
        setLinear(AssetPaths.MARS_ROCK_BASALT);
        setLinear(AssetPaths.MARS_ROCK_IRONSTONE);

        setLinear(AssetPaths.ENEMY_SENTINEL);
        setLinear(AssetPaths.ENEMY_MARS_SKIMMER);
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
        setLinear(AssetPaths.LOOT_CHEST_CLOSED);
        setLinear(AssetPaths.LOOT_CHEST_OPEN);
        setLinear(AssetPaths.ALLOY_PLATE);
        setLinear(AssetPaths.QUANTUM_CORE);
        setLinear(AssetPaths.FIBER_MESH);
        setLinear(AssetPaths.MINING_TOOL);
        setLinear(AssetPaths.REPAIR_TOOL);
        setLinear(AssetPaths.ARMOR_HELMET);
        setLinear(AssetPaths.ARMOR_CHEST);
        setLinear(AssetPaths.ARMOR_BOOTS);
        setLinear(AssetPaths.ARMOR_OVERLAY_HELMET);
        setLinear(AssetPaths.ARMOR_OVERLAY_CHEST);
        setLinear(AssetPaths.ARMOR_OVERLAY_BOOTS);
        setLinear(AssetPaths.REPAIR_STATION);
        setLinear(AssetPaths.REPAIR_STATION_BROKEN);
        setLinear(AssetPaths.REPAIR_HAMMER);
        setLinear(AssetPaths.MARS_SATELLITE_BROKEN);
        setLinear(AssetPaths.MARS_SATELLITE_REPAIRED);
        setLinear(AssetPaths.MISSION_INTRO_DISTRESS);
        setLinear(AssetPaths.MISSION_INTRO_FAILURE);
        setLinear(AssetPaths.MISSION_INTRO_DEPLOYMENT);
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


    public Texture getAstronautLocomotion(com.orion.echoes.lua.config.AstronautType type) {
        return switch (type) {
            case TRIPLE_T -> getTexture(AssetPaths.ASTRONAUT_TRIPLE_T_LOCOMOTION);
            case WINSTON -> getTexture(AssetPaths.ASTRONAUT_WINSTON_LOCOMOTION);
            case SHREK -> getTexture(AssetPaths.ASTRONAUT_SHREK_LOCOMOTION);
            case NEON -> getTexture(AssetPaths.ASTRONAUT_NEON_LOCOMOTION);
        };
    }

    public Texture getLunarBase() {
        return getTexture(
            AssetPaths.LUNAR_BASE
        );
    }

    public Texture getLunarSurface() { return getTexture(AssetPaths.LUNAR_SURFACE); }
    public Texture getBaseInterior() { return getTexture(AssetPaths.BASE_INTERIOR); }
    public Texture getCraftingWorkbench() { return getTexture(AssetPaths.CRAFTING_WORKBENCH); }

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
    public Texture getMarsOxygen() { return getTexture(AssetPaths.MARS_OXYGEN); }
    public Texture getMarsFood() { return getTexture(AssetPaths.MARS_FOOD); }
    public Texture getMarsMedkit() { return getTexture(AssetPaths.MARS_MEDKIT); }
    public Texture getMarsIceSample() { return getTexture(AssetPaths.MARS_ICE_SAMPLE); }

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

    public Texture getMarsRockSedimentary() { return getTexture(AssetPaths.MARS_ROCK_SEDIMENTARY); }
    public Texture getMarsRockBasalt() { return getTexture(AssetPaths.MARS_ROCK_BASALT); }
    public Texture getMarsRockIronstone() { return getTexture(AssetPaths.MARS_ROCK_IRONSTONE); }

    public Texture getEnemySentinel() { return getTexture(AssetPaths.ENEMY_SENTINEL); }
    public Texture getEnemyMarsSkimmer() { return getTexture(AssetPaths.ENEMY_MARS_SKIMMER); }
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
    public Texture getLootChestClosed() { return getTexture(AssetPaths.LOOT_CHEST_CLOSED); }
    public Texture getLootChestOpen() { return getTexture(AssetPaths.LOOT_CHEST_OPEN); }
    public Texture getAlloyPlate() { return getTexture(AssetPaths.ALLOY_PLATE); }
    public Texture getQuantumCore() { return getTexture(AssetPaths.QUANTUM_CORE); }
    public Texture getFiberMesh() { return getTexture(AssetPaths.FIBER_MESH); }
    public Texture getMiningTool() { return getTexture(AssetPaths.MINING_TOOL); }
    public Texture getRepairTool() { return getTexture(AssetPaths.REPAIR_TOOL); }
    public Texture getArmorHelmet() { return getTexture(AssetPaths.ARMOR_HELMET); }
    public Texture getArmorChest() { return getTexture(AssetPaths.ARMOR_CHEST); }
    public Texture getArmorBoots() { return getTexture(AssetPaths.ARMOR_BOOTS); }
    public Texture getArmorOverlayHelmet() { return getTexture(AssetPaths.ARMOR_OVERLAY_HELMET); }
    public Texture getArmorOverlayChest() { return getTexture(AssetPaths.ARMOR_OVERLAY_CHEST); }
    public Texture getArmorOverlayBoots() { return getTexture(AssetPaths.ARMOR_OVERLAY_BOOTS); }
    public Texture getRepairStation() { return getTexture(AssetPaths.REPAIR_STATION); }
    public Texture getRepairStationBroken() { return getTexture(AssetPaths.REPAIR_STATION_BROKEN); }
    public Texture getRepairHammer() { return getTexture(AssetPaths.REPAIR_HAMMER); }
    public Texture getMarsSatelliteBroken() { return getTexture(AssetPaths.MARS_SATELLITE_BROKEN); }
    public Texture getMarsSatelliteRepaired() { return getTexture(AssetPaths.MARS_SATELLITE_REPAIRED); }
    public Texture getMissionIntroDistress() { return getTexture(AssetPaths.MISSION_INTRO_DISTRESS); }
    public Texture getMissionIntroFailure() { return getTexture(AssetPaths.MISSION_INTRO_FAILURE); }
    public Texture getMissionIntroDeployment() { return getTexture(AssetPaths.MISSION_INTRO_DEPLOYMENT); }
    public Texture getMarsBase() { return getTexture(AssetPaths.MARS_BASE); }
    public Texture getMarsResearchSite() { return getTexture(AssetPaths.MARS_RESEARCH_SITE); }

    public void dispose() {

        manager.dispose();
    }
}
