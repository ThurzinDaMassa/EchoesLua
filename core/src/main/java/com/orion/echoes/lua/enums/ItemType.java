package com.orion.echoes.lua.enums;

public enum ItemType {

    OXYGEN,
    FOOD,
    ICE_ROCK,
    MEDKIT,
    ANTENNA_PART,
    ENERGY_PART,
    EXTRACTION_PART,
    GREENHOUSE_PART,
    WEAPON_PART_A,
    WEAPON_PART_B,
    WEAPON_PART_C,
    AMMO_CELL,
    ALLOY_PLATE,
    QUANTUM_CORE,
    FIBER_MESH,
    MINING_TOOL,
    REPAIR_TOOL,
    ARMOR_HELMET,
    ARMOR_CHEST,
    ARMOR_BOOTS;

    public boolean isArmor() {
        return this == ARMOR_HELMET || this == ARMOR_CHEST || this == ARMOR_BOOTS;
    }

    public boolean isTool() {
        return this == MINING_TOOL || this == REPAIR_TOOL;
    }

    public boolean isEquipment() {
        return isArmor() || isTool();
    }

    public int getMaxStack() {
        return isEquipment() ? 1 : 99;
    }
}
