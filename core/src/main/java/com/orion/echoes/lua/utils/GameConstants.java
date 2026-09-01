package com.orion.echoes.lua.utils;

public final class GameConstants {

    private GameConstants() {
    }

    // =========================================================
    // RESOLUCAO
    // =========================================================

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 720f;

    // =========================================================
    // MUNDO
    // =========================================================

    public static final float WORLD_WIDTH = 3800f;
    public static final float WORLD_HEIGHT = 2200f;

    // =========================================================
    // JOGADOR
    // =========================================================

    public static final float PLAYER_WIDTH = 84f;
    public static final float PLAYER_HEIGHT = 112f;

    public static final float PLAYER_SPEED = 320f;

    public static final float PLAYER_SPRINT_MULTIPLIER = 1.55f;
    public static final float SPRINT_ENERGY_COST = 28f;
    public static final float ENERGY_RECOVERY_RATE = 18f;

    public static final float PLAYER_START_X =
        WORLD_WIDTH / 2f;

    public static final float PLAYER_START_Y =
        WORLD_HEIGHT / 2f;

    // =========================================================
    // CAMERA
    // =========================================================

    public static final float CAMERA_FOLLOW_SPEED = 5f;

    // =========================================================
    // BASE LUNAR
    // =========================================================

    public static final float BASE_WIDTH = 420f;
    public static final float BASE_HEIGHT = 300f;

    public static final float BASE_X = 450f;
    public static final float BASE_Y = 500f;

    // =========================================================
    // OXIGENIO
    // =========================================================

    public static final float MAX_OXYGEN = 100f;

    public static final float BASE_OXYGEN_RECHARGE_RATE =
        25f;

    /*
     * Oxigenio consumido por segundo
     * quando estiver fora da base.
     *
     * 2 pontos por segundo:
     * 100 O2 dura aproximadamente 50 segundos.
     */
    public static final float OXYGEN_CONSUMPTION_RATE =
        2f;

    public static final float CRITICAL_OXYGEN_THRESHOLD = 25f;

    // =========================================================
    // ENERGIA
    // =========================================================

    public static final float MAX_ENERGY = 100f;
    public static final float MAX_HEALTH = 100f;
    public static final float MEDKIT_HEAL_AMOUNT = 45f;

    // =========================================================
    // ITENS
    // =========================================================

    public static final float ITEM_SIZE = 54f;

    public static final float OXYGEN_PICKUP_AMOUNT = 25f;

    public static final float FOOD_PICKUP_AMOUNT = 30f;

    // =========================================================
    // PROCESSAMENTO DE GELO
    // =========================================================

    public static final int ICE_PROCESS_COST = 1;

    public static final int WATER_PER_ICE = 1;

    public static final int FUEL_PER_ICE = 1;

    public static final float OXYGEN_PER_ICE = 25f;

    public static final float ICE_PROCESS_COOLDOWN =
        0.25f;

    // =========================================================
    // HUD
    // =========================================================

    public static final float HUD_WIDTH = 1280f;
    public static final float HUD_HEIGHT = 720f;

    public static final float HUD_BAR_WIDTH = 250f;
    public static final float HUD_BAR_HEIGHT = 22f;
}
