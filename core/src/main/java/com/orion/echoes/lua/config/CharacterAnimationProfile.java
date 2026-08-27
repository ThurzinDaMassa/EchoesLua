package com.orion.echoes.lua.config;

/**
 * Layout shared by the normalized locomotion atlases.
 * Every pose is packed around the same centre and feet baseline, so changing
 * frames cannot move the character or expose pixels from a neighbouring cell.
 */
public final class CharacterAnimationProfile {

    public static final int FRAME_WIDTH = 384;
    public static final int FRAME_HEIGHT = 341;
    private static final float TARGET_IDLE_HEIGHT = 112f;
    private static final float SOURCE_IDLE_HEIGHT = 286f;
    private static final float FRAME_CENTER_X = 192f;
    private static final float FEET_BASELINE_Y = 326f;
    private static final CharacterAnimationProfile STANDARD = new CharacterAnimationProfile();

    private final float scale;
    private CharacterAnimationProfile() {
        this.scale = TARGET_IDLE_HEIGHT / SOURCE_IDLE_HEIGHT;
    }

    public static CharacterAnimationProfile forType(AstronautType type) {
        return STANDARD;
    }

    public float getScale() {
        return scale;
    }

    public float getCenterX(int frame) {
        return FRAME_CENTER_X;
    }

    public float getBottomY(int frame) {
        return FEET_BASELINE_Y;
    }
}
