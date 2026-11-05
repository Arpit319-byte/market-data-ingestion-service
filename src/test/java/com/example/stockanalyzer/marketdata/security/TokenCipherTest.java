package com.example.stockanalyzer.marketdata.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.stockanalyzer.marketdata.exception.AuthenticationException;

class TokenCipherTest {

    @Test
    void roundTripsATokenAndLeavesLegacyPlaintextReadable() {
        TokenCipher cipher = new TokenCipher("test-encryption-key");

        String encrypted = cipher.encrypt("access-token");

        assertTrue(encrypted.startsWith(TokenCipher.PREFIX));
        assertEquals("access-token", cipher.decrypt(encrypted));
        assertEquals("legacy-token", cipher.decrypt("legacy-token"));
    }

    @Test
    void rejectsATamperedToken() {
        TokenCipher cipher = new TokenCipher("test-encryption-key");
        String encrypted = cipher.encrypt("access-token");
        String tampered = encrypted.substring(0, encrypted.length() - 2) + "aa";

        assertThrows(AuthenticationException.class, () -> cipher.decrypt(tampered));
    }
}
