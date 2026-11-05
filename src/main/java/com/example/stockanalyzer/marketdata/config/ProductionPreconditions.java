package com.example.stockanalyzer.marketdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionPreconditions implements ApplicationRunner {

    private final String apiKey;
    private final String encryptionKey;
    private final String kiteApiKey;
    private final String kiteApiSecret;

    public ProductionPreconditions(
            @Value("${app.security.api-key:}") String apiKey,
            @Value("${app.encryption.key:}") String encryptionKey,
            @Value("${kite.api.key:}") String kiteApiKey,
            @Value("${kite.api.secret:}") String kiteApiSecret) {
        this.apiKey = apiKey;
        this.encryptionKey = encryptionKey;
        this.kiteApiKey = kiteApiKey;
        this.kiteApiSecret = kiteApiSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        ProductionSettings.require("APP_API_KEY", apiKey);
        ProductionSettings.require("APP_ENCRYPTION_KEY", encryptionKey);
        ProductionSettings.require("KITE_API_KEY", kiteApiKey);
        ProductionSettings.require("KITE_API_SECRET", kiteApiSecret);
    }
}
