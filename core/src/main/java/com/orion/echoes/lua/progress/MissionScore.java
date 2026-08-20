package com.orion.echoes.lua.progress;

import com.orion.echoes.lua.config.Difficulty;

public final class MissionScore {
    private MissionScore() {
    }

    public static int calculate(
        float missionTime,
        int collectedItems,
        float remainingOxygen,
        Difficulty difficulty
    ) {
        int completion = 1000;
        int itemBonus = Math.max(0, collectedItems) * 100;
        int oxygenBonus = Math.max(0, Math.round(remainingOxygen)) * 8;
        int timeBonus = Math.max(0, 900 - Math.round(missionTime) * 3);
        return Math.round((completion + itemBonus + oxygenBonus + timeBonus)
            * difficulty.getScoreMultiplier());
    }
}
