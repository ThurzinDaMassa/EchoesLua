package com.orion.echoes.lua.config;

public enum AstronautType {
    TRIPLE_T("TRIPLE T"),
    WINSTON("WINSTON"),
    SHREK("SHREK"),
    NEON("NEON");

    private final String label;
    AstronautType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
