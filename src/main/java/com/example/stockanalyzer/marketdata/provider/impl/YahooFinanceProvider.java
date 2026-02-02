package com.example.stockanalyzer.marketdata.provider.impl;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Implementation for Yahoo Finance API provider
 * https://query1.finance.yahoo.com/v8/finance/chart/{symbol}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YahooFinanceProvider implements MarketDataProvider {

    private final WebClient webClient;

    @Override
    public boolean supports(DataSource dataSource) {
        String endpoint = dataSource.getApiEndpoint().toLowerCase();
        String providerType = dataSource.getProviderType().toLowerCase();
        return endpoint.contains("yahoo") || 
               endpoint.contains("finance.yahoo.com") ||
               providerType.contains("yahoo") ||
               dataSource.getName().toLowerCase().contains("yahoo");
    }

    @Override
    public Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval) {
        String url = buildUrl(dataSource, symbol, interval);
        
        log.info("Fetching OHLC data from Yahoo Finance for symbol {} with interval {}", symbol, interval);

        return webClient.get()
                .uri(url)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(OhlcApiResponse.class)
                .timeout(Duration.ofSeconds(
                    dataSource.getTimeoutSeconds() != null ? dataSource.getTimeoutSeconds() : 30
                ))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                    .filter(throwable -> throwable instanceof WebClientResponseException 
                        && ((WebClientResponseException) throwable).getStatusCode() 
                            .is5xxServerError())
                    .doBeforeRetry(retrySignal -> 
                        log.warn("Retrying Yahoo Finance API call after error: {}", retrySignal.totalRetries())
                    )
                )
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("Yahoo Finance API call failed with status {}: {}", 
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Yahoo Finance API call failed: {}", error.getMessage());
                    }
                })
                .onErrorMap(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        return new MarketDataException(
                            "Failed to fetch data from Yahoo Finance: " + ex.getStatusCode() + 
                            " - " + ex.getResponseBodyAsString(),
                            ex
                        );
                    }
                    return new MarketDataException(
                        "Failed to fetch data from Yahoo Finance: " + error.getMessage(),
                        error
                    );
                });
    }

    /**
     * Builds the Yahoo Finance API URL
     * Format: https://query1.finance.yahoo.com/v8/finance/chart/{symbol}?interval=1d&range=1mo
     */
    private String buildUrl(DataSource dataSource, String symbol, PriceInterval interval) {
        String baseUrl = dataSource.getApiEndpoint();
        String intervalParam = mapIntervalToYahoo(interval);
        
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        return String.format("%s/%s?interval=%s&range=1mo", baseUrl, symbol, intervalParam);
    }

    /**
     * Maps PriceInterval to Yahoo Finance interval format
     */
    private String mapIntervalToYahoo(PriceInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> "1m";
            case FIVE_MINUTE -> "5m";
            case FIFTEEN_MINUTE -> "15m";
            case THIRTY_MINUTE -> "30m";
            case ONE_HOUR -> "1h";
            case FOUR_HOUR -> "4h";
            case ONE_DAY -> "1d";
            case ONE_WEEK -> "1wk";
            case ONE_MONTH -> "1mo";
        };
    }

    @Override
    public String getProviderName() {
        return "Yahoo Finance";
    }
}
