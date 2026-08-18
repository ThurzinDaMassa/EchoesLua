package com.orion.echoes.lua.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import com.orion.echoes.lua.enums.ItemType;

public class AudioManager {

    // =========================================================
    // MUSIC
    // =========================================================

    private Music ambientMusic;

    private Music portalLoopMusic;

    // =========================================================
    // SOUNDS
    // =========================================================

    private Sound pickupOxygenSound;

    private Sound pickupFoodSound;

    private Sound pickupIceSound;

    private Sound processIceSound;

    private Sound baseRechargeSound;

    private Sound oxygenWarningSound;

    private Sound portalActivateSound;

    private Sound rockImpactSound;

    private Sound victorySound;

    private Sound defeatSound;

    private Sound menuClickSound;

    private Sound menuHoverSound;

    private Sound footstepSound;

    // =========================================================
    // VOLUME
    // =========================================================

    private float masterVolume;

    private float musicVolume;

    private float soundVolume;

    // =========================================================
    // TIMERS
    // =========================================================

    private float footstepTimer;

    private float oxygenWarningTimer;

    private float rechargeTimer;

    private float rockImpactTimer;

    // =========================================================
    // ESTADO
    // =========================================================

    private boolean portalLoopActive;

    private boolean muted;

    // =========================================================
    // CONSTRUTOR
    // =========================================================

    public AudioManager() {

        masterVolume = 1f;

        musicVolume = 0.36f;

        soundVolume = 0.72f;

        footstepTimer = 0f;

        oxygenWarningTimer = 0f;

        rechargeTimer = 0f;

        rockImpactTimer = 0f;

        portalLoopActive = false;

        muted = false;
    }

    // =========================================================
    // LOAD
    // =========================================================

    public void load() {

        ambientMusic =
            Gdx.audio.newMusic(
                Gdx.files.internal(
                    SoundPaths.AMBIENT_LUNAR
                )
            );

        portalLoopMusic =
            Gdx.audio.newMusic(
                Gdx.files.internal(
                    SoundPaths.PORTAL_LOOP
                )
            );

        pickupOxygenSound =
            loadSound(
                SoundPaths.PICKUP_OXYGEN
            );

        pickupFoodSound =
            loadSound(
                SoundPaths.PICKUP_FOOD
            );

        pickupIceSound =
            loadSound(
                SoundPaths.PICKUP_ICE
            );

        processIceSound =
            loadSound(
                SoundPaths.PROCESS_ICE
            );

        baseRechargeSound =
            loadSound(
                SoundPaths.BASE_RECHARGE
            );

        oxygenWarningSound =
            loadSound(
                SoundPaths.OXYGEN_WARNING
            );

        portalActivateSound =
            loadSound(
                SoundPaths.PORTAL_ACTIVATE
            );

        rockImpactSound =
            loadSound(
                SoundPaths.ROCK_IMPACT
            );

        victorySound =
            loadSound(
                SoundPaths.VICTORY
            );

        defeatSound =
            loadSound(
                SoundPaths.DEFEAT
            );

        menuClickSound =
            loadSound(
                SoundPaths.MENU_CLICK
            );

        menuHoverSound =
            loadSound(
                SoundPaths.MENU_HOVER
            );

        footstepSound =
            loadSound(
                SoundPaths.FOOTSTEP_LUNAR
            );

        configureMusic();
    }

    private Sound loadSound(
        String path
    ) {

        return Gdx.audio.newSound(
            Gdx.files.internal(
                path
            )
        );
    }

    private void configureMusic() {

        ambientMusic.setLooping(
            true
        );

        ambientMusic.setVolume(
            getMusicVolume()
        );

        portalLoopMusic.setLooping(
            true
        );

        portalLoopMusic.setVolume(
            getPortalVolume()
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta,
        boolean playerMoving,
        boolean insideBase,
        boolean oxygenCritical,
        boolean portalActive
    ) {

        updateTimers(
            delta
        );

        updateFootsteps(
            playerMoving
        );

        updateRecharge(
            insideBase
        );

        updateOxygenWarning(
            oxygenCritical
        );

        updatePortalLoop(
            portalActive
        );
    }

    private void updateTimers(
        float delta
    ) {

        if (footstepTimer > 0f) {
            footstepTimer -= delta;
        }

        if (oxygenWarningTimer > 0f) {
            oxygenWarningTimer -= delta;
        }

        if (rechargeTimer > 0f) {
            rechargeTimer -= delta;
        }

        if (rockImpactTimer > 0f) {
            rockImpactTimer -= delta;
        }
    }

    // =========================================================
    // FOOTSTEPS
    // =========================================================

    private void updateFootsteps(
        boolean moving
    ) {

        if (!moving) {
            return;
        }

        if (footstepTimer > 0f) {
            return;
        }

        play(
            footstepSound,
            0.26f
        );

        footstepTimer =
            0.32f;
    }

    // =========================================================
    // BASE
    // =========================================================

    private void updateRecharge(
        boolean insideBase
    ) {

        if (!insideBase) {
            return;
        }

        if (rechargeTimer > 0f) {
            return;
        }

        play(
            baseRechargeSound,
            0.20f
        );

        rechargeTimer =
            1.35f;
    }

    // =========================================================
    // OXYGEN WARNING
    // =========================================================

    private void updateOxygenWarning(
        boolean critical
    ) {

        if (!critical) {
            return;
        }

        if (oxygenWarningTimer > 0f) {
            return;
        }

        play(
            oxygenWarningSound,
            0.48f
        );

        oxygenWarningTimer =
            2.1f;
    }

    // =========================================================
    // PORTAL LOOP
    // =========================================================

    private void updatePortalLoop(
        boolean active
    ) {

        if (active) {

            startPortalLoop();

        } else {

            stopPortalLoop();
        }
    }

    public void startPortalLoop() {

        if (portalLoopActive) {
            return;
        }

        portalLoopActive = true;

        portalLoopMusic.setVolume(
            getPortalVolume()
        );

        portalLoopMusic.play();
    }

    public void stopPortalLoop() {

        if (!portalLoopActive) {
            return;
        }

        portalLoopActive = false;

        portalLoopMusic.stop();
    }

    // =========================================================
    // BACKGROUND MUSIC
    // =========================================================

    public void playAmbientMusic() {

        if (
            ambientMusic.isPlaying()
        ) {

            return;
        }

        ambientMusic.setVolume(
            getMusicVolume()
        );

        ambientMusic.play();
    }

    public void stopAmbientMusic() {

        if (
            ambientMusic.isPlaying()
        ) {

            ambientMusic.stop();
        }
    }

    public void pauseAmbientMusic() {

        if (
            ambientMusic.isPlaying()
        ) {

            ambientMusic.pause();
        }
    }

    public void resumeAmbientMusic() {

        if (
            !ambientMusic.isPlaying()
        ) {

            ambientMusic.play();
        }
    }

    // =========================================================
    // PICKUPS
    // =========================================================

    public void playPickup(
        ItemType type
    ) {

        switch (type) {

            case OXYGEN:

                play(
                    pickupOxygenSound,
                    0.85f
                );

                break;

            case FOOD:

                play(
                    pickupFoodSound,
                    0.80f
                );

                break;

            case ICE_ROCK:

                play(
                    pickupIceSound,
                    0.90f
                );

                break;
        }
    }

    // =========================================================
    // PROCESSAMENTO
    // =========================================================

    public void playIceProcessing() {

        play(
            processIceSound,
            0.85f
        );
    }

    // =========================================================
    // PORTAL
    // =========================================================

    public void playPortalActivation() {

        play(
            portalActivateSound,
            1f
        );
    }

    // =========================================================
    // IMPACTO
    // =========================================================

    public void playRockImpact() {

        if (
            rockImpactTimer > 0f
        ) {

            return;
        }

        play(
            rockImpactSound,
            0.45f
        );

        rockImpactTimer =
            0.18f;
    }

    // =========================================================
    // RESULTADO
    // =========================================================

    public void playVictory() {

        stopPortalLoop();

        stopAmbientMusic();

        play(
            victorySound,
            1f
        );
    }

    public void playDefeat() {

        stopPortalLoop();

        play(
            defeatSound,
            0.90f
        );
    }

    // =========================================================
    // MENU
    // =========================================================

    public void playMenuClick() {

        play(
            menuClickSound,
            0.75f
        );
    }

    public void playMenuHover() {

        play(
            menuHoverSound,
            0.32f
        );
    }

    // =========================================================
    // PLAY
    // =========================================================

    private void play(
        Sound sound,
        float volume
    ) {

        if (
            muted
                ||
                sound == null
        ) {

            return;
        }

        sound.play(
            volume
                *
                soundVolume
                *
                masterVolume
        );
    }

    // =========================================================
    // VOLUME
    // =========================================================

    private float getMusicVolume() {

        if (muted) {
            return 0f;
        }

        return musicVolume
            * masterVolume;
    }

    private float getPortalVolume() {

        if (muted) {
            return 0f;
        }

        return 0.22f
            * masterVolume;
    }

    public void setMasterVolume(
        float volume
    ) {

        masterVolume =
            clamp01(
                volume
            );

        updateMusicVolumes();
    }

    public void setMusicVolume(
        float volume
    ) {

        musicVolume =
            clamp01(
                volume
            );

        updateMusicVolumes();
    }

    public void setSoundVolume(
        float volume
    ) {

        soundVolume =
            clamp01(
                volume
            );
    }

    private void updateMusicVolumes() {

        if (ambientMusic != null) {

            ambientMusic.setVolume(
                getMusicVolume()
            );
        }

        if (portalLoopMusic != null) {

            portalLoopMusic.setVolume(
                getPortalVolume()
            );
        }
    }

    private float clamp01(
        float value
    ) {

        return Math.max(
            0f,
            Math.min(
                1f,
                value
            )
        );
    }

    // =========================================================
    // MUTE
    // =========================================================

    public void toggleMute() {

        muted =
            !muted;

        updateMusicVolumes();
    }

    public boolean isMuted() {
        return muted;
    }

    // =========================================================
    // STOP ALL
    // =========================================================

    public void stopGameplayAudio() {

        stopPortalLoop();
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    public void dispose() {

        if (ambientMusic != null) {
            ambientMusic.dispose();
        }

        if (portalLoopMusic != null) {
            portalLoopMusic.dispose();
        }

        disposeSound(
            pickupOxygenSound
        );

        disposeSound(
            pickupFoodSound
        );

        disposeSound(
            pickupIceSound
        );

        disposeSound(
            processIceSound
        );

        disposeSound(
            baseRechargeSound
        );

        disposeSound(
            oxygenWarningSound
        );

        disposeSound(
            portalActivateSound
        );

        disposeSound(
            rockImpactSound
        );

        disposeSound(
            victorySound
        );

        disposeSound(
            defeatSound
        );

        disposeSound(
            menuClickSound
        );

        disposeSound(
            menuHoverSound
        );

        disposeSound(
            footstepSound
        );
    }

    private void disposeSound(
        Sound sound
    ) {

        if (sound != null) {
            sound.dispose();
        }
    }
}
