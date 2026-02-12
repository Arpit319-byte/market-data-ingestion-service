package com.example.stockanalyzer.marketdata.provider;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;

import reactor.core.publisher.Mono;

/**
 * Interface for market data providers
 * Implement this interface to add support for different API providers
 */
public interface MarketDataProvider {

    /**
     * Checks if this provider supports the given data source
     * 
     * @param dataSource The data source configuration
     * @return true if this provider can handle the data source
     */
    boolean supports(DataSource dataSource);

    /**
     * Fetches OHLC data from the provider's API
     * 
     * @param dataSource The data source configuration
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param interval Price interval
     * @return Mono containing the OHLC API response
     */
    Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval);

    /**
     * Gets the provider name
     * 
     * @return Provider name (e.g., "Grow API")
     */
    String getProviderName();
}
