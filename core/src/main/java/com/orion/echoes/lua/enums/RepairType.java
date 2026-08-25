package com.orion.echoes.lua.enums;

public enum RepairType {
    COMMUNICATION("COMUNICACAO", ItemType.ANTENNA_PART),
    ENERGY("ENERGIA", ItemType.ENERGY_PART),
    EXTRACTION("EXTRACAO", ItemType.EXTRACTION_PART),
    GREENHOUSE("ESTUFA", ItemType.GREENHOUSE_PART);

    private final String label;
    private final ItemType requiredPart;

    RepairType(String label, ItemType requiredPart) {
        this.label = label;
        this.requiredPart = requiredPart;
    }

    public String getLabel() {
        return label;
    }

    public ItemType getRequiredPart() {
        return requiredPart;
    }
}
