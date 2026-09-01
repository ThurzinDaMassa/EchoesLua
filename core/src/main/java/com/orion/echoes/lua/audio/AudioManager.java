package com.orion.echoes.lua.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import com.orion.echoes.lua.enums.ItemType;

public class AudioManager {

    // =========================================================
    // MUSIC
    // =========================================================

    private Music ambientMusic;

    private Music portalLoopMusic;
    private Music marsAmbientMusic;

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
    private Sound weaponFireSound;
    private Sound enemyHitSound;
    private Sound medkitSound;
    private Sound playerDamageSound;
    private Sound weaponReloadStartSound;
    private Sound weaponReloadCompleteSound;
    private Sound storageChestOpenSound;
    private Sound inventoryMoveSound;
    private Sound enemyAlertSound;

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

    private float ambientMix;

    private float portalMix;

    private float portalPan;

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

        ambientMix = 1f;

        portalMix = 0f;

        portalPan = 0f;
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
        marsAmbientMusic = Gdx.audio.newMusic(Gdx.files.internal(SoundPaths.AMBIENT_MARS));

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
        weaponFireSound = loadSound(SoundPaths.WEAPON_FIRE);
        enemyHitSound = loadSound(SoundPaths.ENEMY_HIT);
        medkitSound = loadSound(SoundPaths.MEDKIT_PICKUP);
        playerDamageSound = loadSound(SoundPaths.PLAYER_DAMAGE);
        weaponReloadStartSound = loadSound(SoundPaths.WEAPON_RELOAD_START);
        weaponReloadCompleteSound = loadSound(SoundPaths.WEAPON_RELOAD_COMPLETE);
        storageChestOpenSound = loadSound(SoundPaths.STORAGE_CHEST_OPEN);
        inventoryMoveSound = loadSound(SoundPaths.INVENTORY_MOVE);
        enemyAlertSound = loadSound(SoundPaths.ENEMY_ALERT);

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
            0f
        );
        marsAmbientMusic.setLooping(true);
        marsAmbientMusic.setVolume(getMusicVolume() * 0.72f);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta,
        boolean playerMoving,
        boolean insideBase,
        boolean oxygenCritical,
        boolean portalActive,
        float portalProximity,
        float portalPan
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

        updateMusicMix(
            delta,
            oxygenCritical,
            portalActive,
            portalProximity,
            portalPan
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

        playVaried(footstepSound, 0.24f, 0.91f, 1.08f);

        footstepTimer =
            0.30f;
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

        playVaried(baseRechargeSound, 0.22f, 0.96f, 1.04f);

        rechargeTimer =
            1.80f;
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
            0.38f
        );

        oxygenWarningTimer =
            2.65f;
    }

    // =========================================================
    // PORTAL LOOP
    // =========================================================

    private void updateMusicMix(
        float delta,
        boolean oxygenCritical,
        boolean portalActive,
        float portalProximity,
        float requestedPortalPan
    ) {
        if (portalActive && !portalLoopActive) {
            portalLoopActive = true;
            portalMix = 0f;
            portalLoopMusic.setVolume(0f);
            portalLoopMusic.play();
        }

        float proximity = MathUtils.clamp(portalProximity, 0f, 1f);
        float ambientTarget = portalActive
            ? MathUtils.lerp(0.82f, 0.52f, proximity)
            : oxygenCritical ? 0.76f : 1f;
        float portalTarget = portalActive ? MathUtils.lerp(0.18f, 1f, proximity) : 0f;
        float fade = MathUtils.clamp(delta * 1.8f, 0f, 1f);
        ambientMix = MathUtils.lerp(ambientMix, ambientTarget, fade);
        portalMix = MathUtils.lerp(portalMix, portalTarget, fade);
        portalPan = MathUtils.lerp(portalPan,
            MathUtils.clamp(requestedPortalPan, -0.72f, 0.72f), fade);
        updateMusicVolumes();

        if (!portalActive && portalLoopActive && portalMix < 0.015f) {
            portalLoopActive = false;
            portalMix = 0f;
            portalLoopMusic.stop();
        }
    }

    public void startPortalLoop() {

        if (portalLoopActive) {
            return;
        }

        portalLoopActive = true;

        portalMix = 0f;

        portalLoopMusic.setVolume(
            0f
        );

        portalLoopMusic.play();
    }

    public void stopPortalLoop() {

        if (!portalLoopActive) {
            return;
        }

        portalLoopActive = false;

        portalMix = 0f;

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

        ambientMix = 1f;

        ambientMusic.setVolume(
            getMusicVolume() * ambientMix
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

                playVaried(pickupOxygenSound, 0.78f, 0.97f, 1.05f);

                break;

            case FOOD:

                playVaried(pickupFoodSound, 0.74f, 0.94f, 1.04f);

                break;

            case ICE_ROCK:

                playVaried(pickupIceSound, 0.82f, 0.90f, 1.02f);

                break;

            case MEDKIT:
                playVaried(medkitSound, 0.82f, 0.98f, 1.04f);
                break;

            default:
                playVaried(pickupIceSound, 0.70f, 1.02f, 1.15f);
                break;
        }
    }

    // =========================================================
    // PROCESSAMENTO
    // =========================================================

    public void playIceProcessing() {

        playVaried(processIceSound, 0.78f, 0.97f, 1.03f);
    }

    public void playRepair() {
        playVaried(processIceSound, 0.70f, 0.86f, 0.94f);
        playVaried(baseRechargeSound, 0.46f, 1.06f, 1.14f);
    }

    public void playCraft() {
        playVaried(processIceSound, 0.78f, 1.10f, 1.18f);
        playVaried(portalActivateSound, 0.58f, 1.12f, 1.20f);
    }

    public void playWeaponFire() {
        playVaried(weaponFireSound, 0.76f, 0.96f, 1.06f);
    }

    public void playEnemyHit() {
        playVaried(enemyHitSound, 0.62f, 0.92f, 1.08f);
    }

    public void playPlayerDamage() {
        playVaried(playerDamageSound, 0.86f, 0.94f, 1.03f);
    }

    public void playWeaponReloadStart() {
        playVaried(weaponReloadStartSound, 0.78f, 0.97f, 1.03f);
    }

    public void playWeaponReloadComplete() {
        playVaried(weaponReloadCompleteSound, 0.82f, 0.98f, 1.04f);
    }

    public void playChestOpen() {
        playVaried(storageChestOpenSound, 0.72f, 0.98f, 1.03f);
    }

    public void playItemMove() {
        playVaried(inventoryMoveSound, 0.42f, 0.96f, 1.08f);
    }

    public void playEnemyAlert() {
        playVaried(enemyAlertSound, 0.58f, 0.96f, 1.04f);
    }

    public void playMarsAmbient() {
        stopAmbientMusic();
        if (!marsAmbientMusic.isPlaying()) marsAmbientMusic.play();
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

        playVaried(rockImpactSound, 0.28f, 0.82f, 1.02f);

        rockImpactTimer =
            0.32f;
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

        stopAmbientMusic();

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

        playVaried(menuHoverSound, 0.24f, 0.99f, 1.04f);
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

    private void playVaried(
        Sound sound,
        float volume,
        float minimumPitch,
        float maximumPitch
    ) {
        if (muted || sound == null) {
            return;
        }

        sound.play(
            volume * soundVolume * masterVolume,
            MathUtils.random(minimumPitch, maximumPitch),
            MathUtils.random(-0.06f, 0.06f)
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
                getMusicVolume() * ambientMix
            );
        }

        if (portalLoopMusic != null) {
            portalLoopMusic.setPan(portalPan, getPortalVolume() * portalMix);
        }
        if (marsAmbientMusic != null) marsAmbientMusic.setVolume(getMusicVolume() * 0.72f);
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

    public void setMuted(boolean muted) {
        this.muted = muted;
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
        if (marsAmbientMusic != null) marsAmbientMusic.stop();
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
        if (marsAmbientMusic != null) marsAmbientMusic.dispose();

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
        disposeSound(weaponFireSound);
        disposeSound(enemyHitSound);
        disposeSound(medkitSound);
        disposeSound(playerDamageSound);
        disposeSound(weaponReloadStartSound);
        disposeSound(weaponReloadCompleteSound);
        disposeSound(storageChestOpenSound);
        disposeSound(inventoryMoveSound);
        disposeSound(enemyAlertSound);
    }

    private void disposeSound(
        Sound sound
    ) {

        if (sound != null) {
            sound.dispose();
        }
    }
}
