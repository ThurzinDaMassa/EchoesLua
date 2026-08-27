package com.orion.echoes.lua.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.orion.echoes.lua.assets.GameAssets;
import com.orion.echoes.lua.config.AstronautType;
import com.orion.echoes.lua.config.CharacterAnimationProfile;
import com.orion.echoes.lua.systems.PlayerStatus;
import com.orion.echoes.lua.utils.GameConstants;

public class Player {

    // =========================================================
    // POSICAO
    // =========================================================

    private final Vector2 position;

    private final Vector2 previousPosition;

    private final Vector2 direction;

    // =========================================================
    // COLISAO
    // =========================================================

    private final Rectangle bounds;

    // =========================================================
    // VISUAL
    // =========================================================

    private final Sprite sprite;

    private final Sprite weaponSprite;
    private final TextureRegion[] locomotionFrames;
    private final CharacterAnimationProfile animationProfile;
    private final float frameScale;
    private float animationClock;
    private int animationState = -1;
    private float fireAnimationTimer;
    private float craftAnimationTimer;
    private float damageFlashTimer;

    // =========================================================
    // ESTADO
    // =========================================================

    private boolean moving;

    private boolean facingLeft;

    private boolean sprinting;

    private boolean wantsToSprint;
    private boolean sprintExhausted;

    private float movementTime;

    // =========================================================
    // CONSTRUTOR
    // =========================================================

    public Player(
        float startX,
        float startY,
        GameAssets assets
    ) {
        this(startX, startY, assets, AstronautType.TRIPLE_T);
    }

    public Player(float startX, float startY, GameAssets assets, AstronautType astronautType) {

        position =
            new Vector2(
                startX,
                startY
            );

        previousPosition =
            new Vector2(
                startX,
                startY
            );

        direction =
            new Vector2();

        bounds =
            new Rectangle();

        Texture locomotionTexture = assets.getAstronautLocomotion(astronautType);
        int frameWidth = locomotionTexture.getWidth() / 4;
        int frameHeight = locomotionTexture.getHeight() / 3;
        animationProfile = CharacterAnimationProfile.forType(astronautType);
        frameScale = animationProfile.getScale();
        locomotionFrames = new TextureRegion[12];
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                locomotionFrames[row * 4 + column] = new TextureRegion(
                    locomotionTexture,
                    column * frameWidth,
                    row * frameHeight,
                    frameWidth,
                    frameHeight);
            }
        }
        sprite = new Sprite(locomotionFrames[0]);
        sprite.setSize(
            frameWidth * frameScale,
            frameHeight * frameScale
        );

        sprite.setOriginCenter();

        sprite.setPosition(
            startX,
            startY
        );

        weaponSprite = new Sprite(assets.getEvaWeapon());
        weaponSprite.setSize(94f, 47f);
        weaponSprite.setOrigin(18f, 23.5f);

        moving = false;

        // As folhas de animacao novas foram desenhadas olhando para a direita.
        facingLeft = false;

        sprinting = false;

        movementTime = 0f;
        sprintExhausted = false;

        updateBounds();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public void update(
        float delta,
        PlayerStatus status
    ) {

        /*
         * Salva a posicao antes de mover.
         *
         * Isso e usado pelo sistema de colisao
         * com obstaculos.
         */
        fireAnimationTimer = Math.max(0f, fireAnimationTimer - delta);
        craftAnimationTimer = Math.max(0f, craftAnimationTimer - delta);
        damageFlashTimer = Math.max(0f, damageFlashTimer - delta);

        previousPosition.set(
            position
        );

        readInput();

        updateMovement(
            delta,
            status
        );

        updateAnimationClock(delta);

        limitToWorld();

        updateSprite();

        updateBounds();
    }

    // =========================================================
    // INPUT
    // =========================================================

    private void readInput() {

        direction.setZero();

        boolean moveLeft =
            Gdx.input.isKeyPressed(
                Input.Keys.A
            )
                ||
                Gdx.input.isKeyPressed(
                    Input.Keys.LEFT
                );

        boolean moveRight =
            Gdx.input.isKeyPressed(
                Input.Keys.D
            )
                ||
                Gdx.input.isKeyPressed(
                    Input.Keys.RIGHT
                );

        boolean moveUp =
            Gdx.input.isKeyPressed(
                Input.Keys.W
            )
                ||
                Gdx.input.isKeyPressed(
                    Input.Keys.UP
                );

        boolean moveDown =
            Gdx.input.isKeyPressed(
                Input.Keys.S
            )
                ||
                Gdx.input.isKeyPressed(
                    Input.Keys.DOWN
                );

        wantsToSprint =
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        if (moveLeft) {
            direction.x -= 1f;
        }

        if (moveRight) {
            direction.x += 1f;
        }

        if (moveUp) {
            direction.y += 1f;
        }

        if (moveDown) {
            direction.y -= 1f;
        }
    }

    // =========================================================
    // MOVIMENTO
    // =========================================================

    private void updateMovement(
        float delta,
        PlayerStatus status
    ) {

        moving =
            !direction.isZero();

        if (!wantsToSprint) sprintExhausted = false;
        if (sprintExhausted && status.getEnergy() >= 25f) sprintExhausted = false;

        sprinting = moving && wantsToSprint && !sprintExhausted
            && status.getEnergy() > 1f;

        if (!moving) {
            status.addEnergy(GameConstants.ENERGY_RECOVERY_RATE * delta);
            return;
        }

        /*
         * Evita andar mais rapido
         * na diagonal.
         */
        direction.nor();

        updateFacingDirection();

        float speed = GameConstants.PLAYER_SPEED;
        if (sprinting) {
            speed *= GameConstants.PLAYER_SPRINT_MULTIPLIER;
            status.removeEnergy(GameConstants.SPRINT_ENERGY_COST * delta);
            if (status.getEnergy() <= 1f) sprintExhausted = true;
        } else {
            status.addEnergy(GameConstants.ENERGY_RECOVERY_RATE * 0.55f * delta);
        }

        movementTime += delta * (sprinting ? 13f : 8f);

        position.mulAdd(
            direction,
            speed * delta
        );
    }

    // =========================================================
    // DIRECAO DO SPRITE
    // =========================================================

    private void updateFacingDirection() {

        if (direction.x < 0f) {

            facingLeft = true;

        } else if (
            direction.x > 0f
        ) {

            facingLeft = false;
        }
    }

    // =========================================================
    // LIMITES DO MUNDO
    // =========================================================

    private void limitToWorld() {

        position.x =
            MathUtils.clamp(
                position.x,
                0f,
                GameConstants.WORLD_WIDTH
                    - GameConstants.PLAYER_WIDTH
            );

        position.y =
            MathUtils.clamp(
                position.y,
                0f,
                GameConstants.WORLD_HEIGHT
                    - GameConstants.PLAYER_HEIGHT
            );
    }

    // =========================================================
    // SPRITE
    // =========================================================

    private void updateSprite() {

        int row = !moving ? 0 : sprinting ? 2 : 1;
        int frame = ((int) animationClock) & 3;
        int frameIndex = row * 4 + frame;
        sprite.setRegion(locomotionFrames[frameIndex]);

        // Os proprios quadros ja possuem movimento. Manter o idle preso ao chao
        // evita o efeito de deslizar/flutuar quando nenhuma tecla esta pressionada.
        float bob = !moving ? 0f
            : MathUtils.sin(movementTime) * (sprinting ? 0.7f : 0.4f);
        if (craftAnimationTimer > 0f) bob += MathUtils.sin(craftAnimationTimer * 28f) * 1.5f;

        float visibleCenterX = animationProfile.getCenterX(frameIndex);
        if (facingLeft) {
            visibleCenterX = CharacterAnimationProfile.FRAME_WIDTH - visibleCenterX;
        }
        float feetFromCellBottom = CharacterAnimationProfile.FRAME_HEIGHT
            - animationProfile.getBottomY(frameIndex);
        float originX = visibleCenterX * frameScale;
        float originY = feetFromCellBottom * frameScale;

        sprite.setOrigin(originX, originY);

        sprite.setPosition(
            getCenterX() - originX,
            position.y - originY + bob
        );

        float recoil = fireAnimationTimer > 0f
            ? MathUtils.sin(fireAnimationTimer / 0.14f * MathUtils.PI) * 2.4f : 0f;
        float craftTilt = craftAnimationTimer > 0f
            ? MathUtils.sin(craftAnimationTimer * 18f) * 1.8f : 0f;
        sprite.setRotation((moving ? -direction.x * bob * 0.35f : 0f)
            + (facingLeft ? recoil : -recoil) + craftTilt);

        /*
         * O flip e feito apenas no eixo X.
         */
        sprite.setFlip(
            facingLeft,
            false
        );
    }

    private void updateAnimationClock(float delta) {
        int state = !moving ? 0 : sprinting ? 2 : 1;
        if (state != animationState) {
            animationState = state;
            animationClock = 0f;
        }
        float framesPerSecond = state == 0 ? 2.2f : state == 1 ? 7.5f : 11.5f;
        animationClock += delta * framesPerSecond;
        if (animationClock >= 4f) animationClock -= 4f;
    }

    // =========================================================
    // BOUNDS
    // =========================================================

    private void updateBounds() {

        /*
         * O sprite possui partes transparentes
         * nas laterais.
         *
         * Por isso a hitbox fica menor
         * que a imagem inteira.
         */

        float paddingX =
            GameConstants.PLAYER_WIDTH
                * 0.20f;

        float paddingBottom =
            GameConstants.PLAYER_HEIGHT
                * 0.08f;

        float paddingTop =
            GameConstants.PLAYER_HEIGHT
                * 0.10f;

        bounds.set(
            position.x
                + paddingX,

            position.y
                + paddingBottom,

            GameConstants.PLAYER_WIDTH
                - paddingX * 2f,

            GameConstants.PLAYER_HEIGHT
                - paddingBottom
                - paddingTop
        );
    }

    // =========================================================
    // COLISAO COM OBSTACULOS
    // =========================================================

    public void restorePreviousPosition() {

        position.set(
            previousPosition
        );

        direction.setZero();

        moving = false;

        updateSprite();

        updateBounds();
    }

    // =========================================================
    // TELEPORTE / RESPAWN
    // =========================================================

    public void setPosition(
        float x,
        float y
    ) {

        position.set(
            x,
            y
        );

        previousPosition.set(
            x,
            y
        );

        limitToWorld();

        updateSprite();

        updateBounds();
    }

    // =========================================================
    // MOVIMENTO MANUAL FUTURO
    // =========================================================

    public void moveBy(
        float x,
        float y
    ) {

        previousPosition.set(
            position
        );

        position.add(
            x,
            y
        );

        limitToWorld();

        updateSprite();

        updateBounds();
    }

    // =========================================================
    // RENDER
    // =========================================================

    public void render(
        SpriteBatch batch
    ) {
        if (damageFlashTimer > 0f) {
            float pulse = 0.35f + MathUtils.sin(damageFlashTimer * 38f) * 0.15f;
            sprite.setColor(1f, pulse, pulse, 1f);
        } else {
            sprite.setColor(Color.WHITE);
        }
        sprite.draw(
            batch
        );
        sprite.setColor(Color.WHITE);
    }

    public void renderWeapon(SpriteBatch batch, float aimX, float aimY) {
        float originX = getCenterX();
        float originY = getCenterY() - 4f;
        float angle = MathUtils.atan2(aimY - originY, aimX - originX) * MathUtils.radiansToDegrees;
        boolean aimLeft = aimX < originX;

        float recoil = fireAnimationTimer > 0f
            ? MathUtils.sin(fireAnimationTimer / 0.14f * MathUtils.PI) * 7f : 0f;
        float radians = angle * MathUtils.degreesToRadians;
        weaponSprite.setPosition(originX - 18f - MathUtils.cos(radians) * recoil,
            originY - 23.5f - MathUtils.sin(radians) * recoil);
        weaponSprite.setRotation(angle);
        weaponSprite.setFlip(false, aimLeft);
        weaponSprite.draw(batch);
    }

    public void setFacingTowards(float worldX) {
        if (Math.abs(worldX - getCenterX()) > 2f) {
            facingLeft = worldX < getCenterX();
            sprite.setFlip(facingLeft, false);
        }
    }

    public void triggerFireAnimation() {
        fireAnimationTimer = 0.14f;
    }

    public void triggerCraftAnimation() {
        craftAnimationTimer = 0.70f;
    }

    public void triggerDamageFlash() {
        damageFlashTimer = 0.46f;
    }

    public boolean isFiringAnimation() { return fireAnimationTimer > 0f; }

    // =========================================================
    // GETTERS DE POSICAO
    // =========================================================

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getCenterX() {

        return position.x
            + GameConstants.PLAYER_WIDTH
            / 2f;
    }

    public float getCenterY() {

        return position.y
            + GameConstants.PLAYER_HEIGHT
            / 2f;
    }

    public float getBottomCenterX() {

        return position.x
            + GameConstants.PLAYER_WIDTH
            / 2f;
    }

    public float getBottomCenterY() {

        return position.y;
    }

    // =========================================================
    // GETTERS DE MOVIMENTO
    // =========================================================

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getPreviousPosition() {
        return previousPosition;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public float getDirectionX() {
        return direction.x;
    }

    public float getDirectionY() {
        return direction.y;
    }

    // =========================================================
    // GETTERS DE ESTADO
    // =========================================================

    public boolean isMoving() {
        return moving;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    // =========================================================
    // COLISAO
    // =========================================================

    public Rectangle getBounds() {
        return bounds;
    }

    // =========================================================
    // TAMANHO
    // =========================================================

    public float getWidth() {
        return GameConstants.PLAYER_WIDTH;
    }

    public float getHeight() {
        return GameConstants.PLAYER_HEIGHT;
    }

    // =========================================================
    // SPRITE
    // =========================================================

    public Sprite getSprite() {
        return sprite;
    }

    // =========================================================
    // DISPOSE
    // =========================================================

    public void dispose() {

        /*
         * Nao damos dispose na textura aqui.
         *
         * A textura pertence ao GameAssets
         * e e compartilhada pelo jogo.
         */
    }
}
