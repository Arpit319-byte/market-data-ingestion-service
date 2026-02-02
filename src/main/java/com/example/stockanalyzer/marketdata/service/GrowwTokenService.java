package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.stockanalyzer.marketdata.dto.GrowwTokenResponse;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Exchanges Groww API key + secret for an access token.
 * Token expires daily at 6:00 AM IST; this service caches and refreshes as needed.
 */
@Slf4j
@Service
public class GrowwTokenService {

    private static final String DEFAULT_TOKEN_URL = "https://api.groww.in/v1/token/api/access";

    @Value("${groww.api.key:}")
    private String apiKey;

    @Value("${groww.api.secret:}")
    private String apiSecret;

    @Value("${groww.api.token-url:" + DEFAULT_TOKEN_URL + "}")
    private String tokenUrl;

    private String cachedToken;
    private Instant tokenExpiresAt;

    private final WebClient webClient;

    public GrowwTokenService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Returns a valid Bearer token. Uses key+secret from config if set, otherwise returns null
     * (caller should use DataSource.apiKey as token in that case).
     */
    public Mono<String> getAccessToken() {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            return Mono.empty();
        }

        if (cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt)) {
            return Mono.just(cachedToken);
        }

        return fetchNewToken();
    }

    private Mono<String> fetchNewToken() {
        log.info("Fetching new Groww API access token using key+secret");

        // Groww may expect form-urlencoded: api_key, api_secret (or similar)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("api_key", apiKey);
        formData.add("api_secret", apiSecret);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GrowwTokenResponse.class)
                .flatMap(response -> {
                    if (response.getError() != null) {
                        return Mono.error(new MarketDataException(
                                "Groww token error: " + response.getError() + " - " + response.getErrorDescription()));
                    }
                    if (response.getAccessToken() == null || response.getAccessToken().isBlank()) {
                        return Mono.error(new MarketDataException("Groww token response did not contain access_token"));
                    }
                    cachedToken = response.getAccessToken();
                    long expiresIn = response.getExpiresIn() != null ? response.getExpiresIn() : 86400L; // default 24h
                    tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
                    log.info("Groww access token obtained, expires in {} seconds", expiresIn);
                    return Mono.just(cachedToken);
                })
                .doOnError(e -> log.error("Failed to get Groww access token: {}", e.getMessage()));
    }

    /**
     * Whether key+secret are configured (so token exchange should be used).
     */
    public boolean isKeySecretConfigured() {
        return apiKey != null && !apiKey.isBlank() && apiSecret != null && !apiSecret.isBlank();
    }
}
