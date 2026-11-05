package com.example.stockanalyzer.marketdata.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductionSettingsTest {

    @Test
    void rejectsABlankProductionSetting() {
        assertThrows(IllegalStateException.class, () -> ProductionSettings.require("APP_API_KEY", " "));
    }
}
