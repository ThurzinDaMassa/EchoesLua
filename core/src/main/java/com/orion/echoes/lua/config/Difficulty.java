package com.orion.echoes.lua.config;

public enum Difficulty {
    EXPLORER("EXPLORADOR", 0.72f, 2, 2, 0.85f),
    STANDARD("PADRAO", 1f, 3, 3, 1f),
    SURVIVOR("SOBREVIVENTE", 1.35f, 4, 4, 1.35f);

    private final String label;
    private final float oxygenConsumptionMultiplier;
    private final int requiredWater;
    private final int requiredFuel;
    private final float scoreMultiplier;

    Difficulty(
        String label,
        float oxygenConsumptionMultiplier,
        int requiredWater,
        int requiredFuel,
        float scoreMultiplier
    ) {
        this.label = label;
        this.oxygenConsumptionMultiplier = oxygenConsumptionMultiplier;
        this.requiredWater = requiredWater;
        this.requiredFuel = requiredFuel;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public float getOxygenConsumptionMultiplier() {
        return oxygenConsumptionMultiplier;
    }

    public int getRequiredWater() {
        return requiredWater;
    }

    public int getRequiredFuel() {
        return requiredFuel;
    }

    public float getScoreMultiplier() {
        return scoreMultiplier;
    }

    public Difficulty next() {
        Difficulty[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public Difficulty previous() {
        Difficulty[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }
}
