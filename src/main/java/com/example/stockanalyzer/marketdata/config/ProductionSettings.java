package com.example.stockanalyzer.marketdata.config;

public final class ProductionSettings {

    private ProductionSettings() {
    }

    public static void require(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set");
        }
    }
}
