package com.example.stockanalyzer.marketdata.provider.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
 * Implementation for Alpha Vantage API provider.
 * https://www.alphavantage.co/documentation/
 * TIME_SERIES_DAILY, TIME_SERIES_INTRADAY (1min, 5min, 15min, 30min, 60min).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlphaVantageProvider implements MarketDataProvider {

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final WebClient webClient;

    @Override
    public boolean supports(DataSource dataSource) {
        String endpoint = dataSource.getApiEndpoint() != null ? dataSource.getApiEndpoint().toLowerCase() : "";
        String providerType = dataSource.getProviderType() != null ? dataSource.getProviderType().toLowerCase() : "";
        String name = dataSource.getName() != null ? dataSource.getName().toLowerCase() : "";
        return endpoint.contains("alphavantage") ||
               endpoint.contains("alphavantage.co") ||
               providerType.contains("alpha") ||
               providerType.contains("vantage") ||
               name.contains("alpha vantage");
    }

    @Override
    public Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval) {
        String apiKey = dataSource.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return Mono.error(new MarketDataException(
                    "Alpha Vantage requires api_key on the data source. Set data_sources.api_key for this data source."));
        }
        Map<String, String> params = buildParams(symbol, interval);
        params.put("apikey", apiKey);
        String url = buildUrl(dataSource.getApiEndpoint(), params);

        log.info("Fetching OHLC data from Alpha Vantage for symbol {} with interval {}", symbol, interval);

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
                                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying Alpha Vantage API call after error: {}", retrySignal.totalRetries())
                        )
                )
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("Alpha Vantage API call failed with status {}: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Alpha Vantage API call failed: {}", error.getMessage());
                    }
                })
                .flatMap(this::checkErrorResponse)
                .onErrorMap(error -> {
                    if (error instanceof MarketDataException) return error;
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        return new MarketDataException(
                                "Failed to fetch data from Alpha Vantage: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
                    }
                    return new MarketDataException("Failed to fetch data from Alpha Vantage: " + error.getMessage(), error);
                });
    }

    private Mono<OhlcApiResponse> checkErrorResponse(OhlcApiResponse response) {
        if (response.getErrorMessage() != null && !response.getErrorMessage().isBlank()) {
            return Mono.error(new MarketDataException("Alpha Vantage error: " + response.getErrorMessage()));
        }
        if (response.getNote() != null && !response.getNote().isBlank()) {
            return Mono.error(new MarketDataException("Alpha Vantage note (e.g. rate limit): " + response.getNote()));
        }
        if (response.getInformation() != null && !response.getInformation().isBlank()) {
            return Mono.error(new MarketDataException("Alpha Vantage information: " + response.getInformation()));
        }
        return Mono.just(response);
    }

    private Map<String, String> buildParams(String symbol, PriceInterval interval) {
        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol);
        if (interval == PriceInterval.ONE_DAY || interval == PriceInterval.ONE_WEEK || interval == PriceInterval.ONE_MONTH) {
            params.put("function", "TIME_SERIES_DAILY");
            params.put("outputsize", "compact"); // last 100 points; use "full" for full history
        } else {
            params.put("function", "TIME_SERIES_INTRADAY");
            params.put("interval", alphaVantageInterval(interval));
            params.put("outputsize", "compact");
        }
        return params;
    }

    private String alphaVantageInterval(PriceInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> "1min";
            case FIVE_MINUTE -> "5min";
            case FIFTEEN_MINUTE -> "15min";
            case THIRTY_MINUTE -> "30min";
            case ONE_HOUR -> "60min";
            default -> "60min";
        };
    }

    private String buildUrl(String baseEndpoint, Map<String, String> params) {
        String base = (baseEndpoint != null && baseEndpoint.contains("alphavantage")) ? baseEndpoint : BASE_URL;
        StringBuilder sb = new StringBuilder(base);
        sb.append(base.contains("?") ? "&" : "?");
        params.forEach((k, v) -> sb.append(k).append("=").append(v != null ? v : "").append("&"));
        if (sb.charAt(sb.length() - 1) == '&') sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public String getProviderName() {
        return "Alpha Vantage";
    }
}
