package com.example.stockanalyzer.marketdata.fetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetcher responsible for HTTP retrieval of instruments CSV from Groww.
 * Single responsibility: HTTP fetch only.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrowwInstrumentFetcher {

    private final WebClient webClient;

    @Value("${groww.instruments.url:https://growwapi-assets.groww.in/instruments/instrument.csv}")
    private String instrumentUrl;

    /**
     * Fetches the instruments CSV from the configured URL.
     *
     * @return CSV body as string, or null if fetch fails or returns empty
     */
    public String fetch() {
        try {
            String csvBody = webClient.get()
                    .uri(instrumentUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return csvBody;
        } catch (Exception e) {
            log.error("Failed to fetch instruments from {}", instrumentUrl, e);
            return null;
        }
    }
}
