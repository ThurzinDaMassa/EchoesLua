package com.orion.echoes.lua.systems;

import com.badlogic.gdx.math.MathUtils;
import com.orion.echoes.lua.utils.GameConstants;

public class PlayerStatus {

    private float oxygen;
    private float energy;
    private float health;

    private int ice;
    private int water;
    private int fuel;

    public PlayerStatus() {

        oxygen = 78f;
        energy = 72f;
        health = GameConstants.MAX_HEALTH;

        ice = 0;
        water = 0;
        fuel = 0;
    }

    // =========================================================
    // OXIGÊNIO
    // =========================================================

    public float getOxygen() {
        return oxygen;
    }

    public void setOxygen(float oxygen) {

        this.oxygen = MathUtils.clamp(
            oxygen,
            0f,
            GameConstants.MAX_OXYGEN
        );
    }

    public void addOxygen(float amount) {

        if (amount <= 0f) {
            return;
        }

        setOxygen(
            oxygen + amount
        );
    }

    public void removeOxygen(float amount) {

        if (amount <= 0f) {
            return;
        }

        setOxygen(
            oxygen - amount
        );
    }

    public boolean hasOxygen() {
        return oxygen > 0f;
    }

    public boolean isOxygenFull() {

        return oxygen
            >= GameConstants.MAX_OXYGEN;
    }

    // =========================================================
    // ENERGIA
    // =========================================================

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {

        this.energy = MathUtils.clamp(
            energy,
            0f,
            GameConstants.MAX_ENERGY
        );
    }

    public void addEnergy(float amount) {

        if (amount <= 0f) {
            return;
        }

        setEnergy(
            energy + amount
        );
    }

    public void removeEnergy(float amount) {

        if (amount <= 0f) {
            return;
        }

        setEnergy(
            energy - amount
        );
    }

    public float getHealth() { return health; }

    public void setHealth(float health) {
        this.health = MathUtils.clamp(health, 0f, GameConstants.MAX_HEALTH);
    }

    public void addHealth(float amount) {
        if (amount > 0f) setHealth(health + amount);
    }

    public void removeHealth(float amount) {
        if (amount > 0f) setHealth(health - amount);
    }

    public boolean isAlive() { return health > 0f; }

    // =========================================================
    // GELO
    // =========================================================

    public int getIce() {
        return ice;
    }

    public void addIce(int amount) {

        if (amount > 0) {
            ice += amount;
        }
    }

    public boolean removeIce(int amount) {

        if (
            amount <= 0
                || ice < amount
        ) {

            return false;
        }

        ice -= amount;

        return true;
    }

    // =========================================================
    // ÁGUA
    // =========================================================

    public int getWater() {
        return water;
    }

    public void addWater(int amount) {

        if (amount > 0) {
            water += amount;
        }
    }

    // =========================================================
    // COMBUSTÍVEL
    // =========================================================

    public int getFuel() {
        return fuel;
    }

    public void addFuel(int amount) {

        if (amount > 0) {
            fuel += amount;
        }
    }

    public void restore(float oxygen, float energy, int ice, int water, int fuel) {
        restore(oxygen, energy, GameConstants.MAX_HEALTH, ice, water, fuel);
    }

    public void restore(float oxygen, float energy, float health, int ice, int water, int fuel) {
        setOxygen(oxygen);
        setEnergy(energy);
        setHealth(health);
        this.ice = Math.max(0, ice);
        this.water = Math.max(0, water);
        this.fuel = Math.max(0, fuel);
    }
}
