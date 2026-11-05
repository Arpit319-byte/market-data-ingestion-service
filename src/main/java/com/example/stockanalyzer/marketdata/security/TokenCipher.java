package com.example.stockanalyzer.marketdata.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.exception.AuthenticationException;

@Component
public class TokenCipher {

    static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public TokenCipher(@Value("${app.encryption.key:}") String encryptionKey) {
        this.key = (encryptionKey == null || encryptionKey.isBlank()) ? null : deriveKey(encryptionKey);
    }

    public String encrypt(String value) {
        if (value == null || value.isBlank() || value.startsWith(PREFIX) || key == null) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException e) {
            throw new AuthenticationException("Failed to encrypt broker token", e);
        }
    }

    public String decrypt(String value) {
        if (value == null || !value.startsWith(PREFIX)) {
            return value;
        }
        if (key == null) {
            throw new AuthenticationException("APP_ENCRYPTION_KEY is required to read stored broker tokens");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new AuthenticationException("Stored broker token could not be decrypted", e);
        }
    }

    private static byte[] deriveKey(String encryptionKey) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
