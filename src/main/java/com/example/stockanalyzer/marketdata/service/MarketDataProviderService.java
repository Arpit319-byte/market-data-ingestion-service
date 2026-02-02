package com.example.stockanalyzer.marketdata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service that selects and uses the appropriate market data provider
 * based on the data source configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataProviderService {

    private final List<MarketDataProvider> providers;

    /**
     * Fetches OHLC data using the appropriate provider for the data source
     * 
     * @param dataSource The data source configuration
     * @param symbol Stock symbol
     * @param interval Price interval
     * @return Mono containing the OHLC API response
     */
    public Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval) {
        MarketDataProvider provider = findProvider(dataSource);
        
        if (provider == null) {
            throw new MarketDataException(
                "No provider found for data source: " + dataSource.getName() + 
                ". Please check provider_type or api_endpoint configuration."
            );
        }

        log.info("Using provider: {} for data source: {}", provider.getProviderName(), dataSource.getName());
        return provider.fetchOhlcData(dataSource, symbol, interval);
    }

    /**
     * Finds the appropriate provider for the given data source
     * 
     * @param dataSource The data source configuration
     * @return The matching provider, or null if none found
     */
    private MarketDataProvider findProvider(DataSource dataSource) {
        return providers.stream()
                .filter(provider -> provider.supports(dataSource))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all available providers
     * 
     * @return List of provider names
     */
    public List<String> getAvailableProviders() {
        return providers.stream()
                .map(MarketDataProvider::getProviderName)
                .toList();
    }
}
