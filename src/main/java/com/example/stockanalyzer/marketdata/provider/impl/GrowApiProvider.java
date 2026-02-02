package com.example.stockanalyzer.marketdata.provider.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.stockanalyzer.marketdata.dto.GrowApiResponse;
import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.Exchange;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Implementation for Grow API provider
 * API Documentation: https://api.groww.in/v1/live-data/ohlc
 * 
 * Note: This API returns real-time OHLC data (current snapshot).
 * For historical interval-based data, use the Historical Data API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrowApiProvider implements MarketDataProvider {

    private final WebClient webClient;
    private final StockRepository stockRepository;
    private final com.example.stockanalyzer.marketdata.service.GrowwTokenService growwTokenService;
    
    // Pattern to parse OHLC string: "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
    private static final Pattern OHLC_PATTERN = Pattern.compile(
        "\\{open:\\s*([\\d.]+),\\s*high:\\s*([\\d.]+),\\s*low:\\s*([\\d.]+),\\s*close:\\s*([\\d.]+)\\}"
    );

    @Override
    public boolean supports(DataSource dataSource) {
        String endpoint = dataSource.getApiEndpoint().toLowerCase();
        String providerType = dataSource.getProviderType().toLowerCase();
        return endpoint.contains("groww.in") || 
               endpoint.contains("grow") ||
               providerType.contains("grow") ||
               dataSource.getName().toLowerCase().contains("grow");
    }

    @Override
    public Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval) {
        // Note: Grow API OHLC endpoint returns real-time data, not historical intervals
        // For interval-based data, you would need to use the Historical Data API
        log.warn("Grow API OHLC endpoint returns real-time data only. Interval parameter will be ignored.");
        
        // Get stock to determine exchange and segment
        // Note: If multiple stocks exist with same symbol, we take the first one
        // In production, you might want to pass stockId or exchange info explicitly
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new MarketDataException(
                    "Stock not found with symbol: " + symbol + 
                    ". Please ensure the stock exists in the database."));
        
        String exchangeSymbol = buildExchangeSymbol(stock);
        String segment = determineSegment(stock);
        String url = buildUrl(dataSource, segment, exchangeSymbol);
        
        log.info("Fetching OHLC data from Grow API for exchange_symbol: {}, segment: {}", exchangeSymbol, segment);

        // Resolve token: use key+secret from config if set, else use api_key from DataSource (direct token)
        Mono<String> tokenMono = resolveAccessToken(dataSource);

        return tokenMono.flatMap(token -> webClient.get()
                .uri(url)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .header("X-API-VERSION", "1.0")
                .retrieve()
                .bodyToMono(GrowApiResponse.class)
                .timeout(Duration.ofSeconds(
                    dataSource.getTimeoutSeconds() != null ? dataSource.getTimeoutSeconds() : 30
                ))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                    .filter(throwable -> throwable instanceof WebClientResponseException 
                        && ((WebClientResponseException) throwable).getStatusCode() 
                            .is5xxServerError())
                    .doBeforeRetry(retrySignal -> 
                        log.warn("Retrying Grow API call after error: {}", retrySignal.totalRetries())
                    )
                )
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("Grow API call failed with status {}: {}", 
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Grow API call failed: {}", error.getMessage());
                    }
                })
                .map(this::convertToOhlcApiResponse))
                .onErrorMap(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        return new MarketDataException(
                            "Failed to fetch data from Grow API: " + ex.getStatusCode() + 
                            " - " + ex.getResponseBodyAsString(),
                            ex
                        );
                    }
                    return new MarketDataException(
                        "Failed to fetch data from Grow API: " + error.getMessage(),
                        error
                    );
                });
    }

    /**
     * Resolves Bearer token: from GrowwTokenService (key+secret in config) or from DataSource.apiKey (direct token).
     */
    private Mono<String> resolveAccessToken(DataSource dataSource) {
        if (growwTokenService.isKeySecretConfigured()) {
            return growwTokenService.getAccessToken()
                    .switchIfEmpty(Mono.error(new MarketDataException(
                        "Groww API key and secret are set but token exchange failed. Check groww.api.key and groww.api.secret.")));
        }
        String token = dataSource.getApiKey();
        if (token == null || token.isBlank()) {
            return Mono.error(new MarketDataException(
                "Groww API requires either (1) groww.api.key + groww.api.secret in config, or (2) api_key set on the data source (access token)."));
        }
        return Mono.just(token);
    }

    /**
     * Builds the Grow API URL
     * Format: https://api.groww.in/v1/live-data/ohlc?segment=CASH&exchange_symbols=NSE_RELIANCE
     */
    private String buildUrl(DataSource dataSource, String segment, String exchangeSymbol) {
        String baseUrl = dataSource.getApiEndpoint();
        
        // Ensure base URL is correct
        if (!baseUrl.contains("api.groww.in")) {
            baseUrl = "https://api.groww.in/v1/live-data/ohlc";
        }
        
        return String.format("%s?segment=%s&exchange_symbols=%s",
                baseUrl, segment, exchangeSymbol);
    }

    /**
     * Builds exchange_symbol format (e.g., NSE_RELIANCE, BSE_SENSEX)
     */
    private String buildExchangeSymbol(Stock stock) {
        Exchange exchange = stock.getExchange();
        String exchangeCode = exchange.getCode().toUpperCase(); // NSE, BSE, etc.
        String symbol = stock.getSymbol().toUpperCase();
        return exchangeCode + "_" + symbol;
    }

    /**
     * Determines segment based on stock type
     * CASH for stocks, FNO for derivatives, COMMODITY for commodities
     */
    private String determineSegment(Stock stock) {
        // Default to CASH for stocks
        // You can enhance this logic based on stock type or other attributes
        String symbol = stock.getSymbol().toUpperCase();
        
        // Check if it's a derivative (FNO segment)
        if (symbol.contains("FUT") || symbol.contains("CE") || symbol.contains("PE")) {
            return "FNO";
        }
        
        // Default to CASH for regular stocks
        return "CASH";
    }

    /**
     * Converts Grow API response to standard OhlcApiResponse format
     */
    private OhlcApiResponse convertToOhlcApiResponse(GrowApiResponse growResponse) {
        if (!"SUCCESS".equalsIgnoreCase(growResponse.getStatus())) {
            throw new MarketDataException(
                "Grow API returned error: " + growResponse.getError() + 
                " - " + growResponse.getMessage()
            );
        }

        if (growResponse.getPayload() == null || growResponse.getPayload().isEmpty()) {
            throw new MarketDataException("Grow API returned empty payload");
        }

        // Convert Grow API format to standard format
        // Grow returns: {"NSE_RELIANCE": "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"}
        // We need: TimeSeriesData format
        
        OhlcApiResponse response = new OhlcApiResponse();
        Map<String, OhlcApiResponse.TimeSeriesData> timeSeries = new HashMap<>();
        
        // Get the first (and typically only) entry from payload
        Map.Entry<String, String> entry = growResponse.getPayload().entrySet().iterator().next();
        String ohlcString = entry.getValue();
        
        // Parse OHLC string: "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
        OhlcApiResponse.TimeSeriesData data = parseOhlcString(ohlcString);
        
        // Use current timestamp as key (since this is real-time data)
        String timestampKey = Instant.now().toString();
        timeSeries.put(timestampKey, data);
        
        // Set as daily time series (since Grow API returns current snapshot)
        response.setTimeSeriesDaily(timeSeries);
        
        return response;
    }

    /**
     * Parses OHLC string format: "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
     */
    private OhlcApiResponse.TimeSeriesData parseOhlcString(String ohlcString) {
        Matcher matcher = OHLC_PATTERN.matcher(ohlcString.trim());
        
        if (!matcher.matches()) {
            throw new MarketDataException("Failed to parse OHLC string: " + ohlcString);
        }
        
        OhlcApiResponse.TimeSeriesData data = new OhlcApiResponse.TimeSeriesData();
        data.setOpen(matcher.group(1));
        data.setHigh(matcher.group(2));
        data.setLow(matcher.group(3));
        data.setClose(matcher.group(4));
        data.setVolume("0"); // Grow API OHLC doesn't include volume
        
        return data;
    }

    @Override
    public String getProviderName() {
        return "Grow API";
    }
}

