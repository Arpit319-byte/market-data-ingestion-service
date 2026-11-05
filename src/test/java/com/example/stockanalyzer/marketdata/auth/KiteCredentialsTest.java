package com.example.stockanalyzer.marketdata.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.exception.AuthenticationException;

class KiteCredentialsTest {

    @Test
    void environmentValueWinsOverTheDatabaseColumn() {
        DataSource dataSource = new DataSource();
        dataSource.setApiKey("database-key");
        dataSource.setApiSecret("database-secret");
        KiteCredentials credentials = new KiteCredentials("env-key", "env-secret");

        assertEquals("env-key", credentials.apiKey(dataSource));
        assertEquals("env-secret", credentials.apiSecret(dataSource));
    }

    @Test
    void fallsBackToTheDatabaseWhenTheEnvironmentIsEmpty() {
        DataSource dataSource = new DataSource();
        dataSource.setApiKey("database-key");
        KiteCredentials credentials = new KiteCredentials(" ", "");

        assertEquals("database-key", credentials.apiKey(dataSource));
        assertThrows(AuthenticationException.class, () -> credentials.apiSecret(dataSource));
    }
}
