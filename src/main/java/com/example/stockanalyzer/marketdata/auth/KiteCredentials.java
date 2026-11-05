package com.example.stockanalyzer.marketdata.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;

@Component
public class KiteCredentials {

    private final String apiKey;
    private final String apiSecret;

    public KiteCredentials(
            @Value("${kite.api.key:}") String apiKey,
            @Value("${kite.api.secret:}") String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String apiKey(DataSource dataSource) {
        return firstNonBlank(apiKey, dataSource == null ? null : dataSource.getApiKey(), "KITE_API_KEY");
    }

    public String apiSecret(DataSource dataSource) {
        return firstNonBlank(apiSecret, dataSource == null ? null : dataSource.getApiSecret(), "KITE_API_SECRET");
    }

    private static String firstNonBlank(String preferred, String fallback, String name) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        throw new AuthenticationException(name + " is missing");
    }
}
