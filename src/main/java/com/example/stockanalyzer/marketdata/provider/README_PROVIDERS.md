# Market Data Provider Guide

## Overview
The market data system uses a provider pattern that allows easy integration with multiple third-party APIs. Each provider implements the `MarketDataProvider` interface.

## Available Providers

### 1. Grow API Provider
- **Class**: `GrowApiProvider`
- **Supports**: Data sources with "grow" in name, endpoint, or provider_type
- **Format**: `/v1/api/stock_data/{symbol}?interval={interval}&apikey={key}`
- **Authentication**: Bearer token in Authorization header

### 2. Alpha Vantage Provider
- **Class**: `AlphaVantageProvider`
- **Supports**: Data sources with "alphavantage" or "alpha" in name, endpoint, or provider_type
- **Format**: `?function=TIME_SERIES_DAILY&symbol={symbol}&apikey={key}&datatype=json`
- **Authentication**: API key as query parameter

### 3. Yahoo Finance Provider
- **Class**: `YahooFinanceProvider`
- **Supports**: Data sources with "yahoo" in name, endpoint, or provider_type
- **Format**: `/v8/finance/chart/{symbol}?interval={interval}&range=1mo`
- **Authentication**: None required

## How to Add a New Provider

### Step 1: Create Provider Implementation

Create a new class implementing `MarketDataProvider`:

```java
package com.example.stockanalyzer.marketdata.provider.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;
// ... other imports

@Component
@RequiredArgsConstructor
public class YourApiProvider implements MarketDataProvider {

    private final WebClient webClient;

    @Override
    public boolean supports(DataSource dataSource) {
        // Return true if this provider can handle the data source
        String endpoint = dataSource.getApiEndpoint().toLowerCase();
        return endpoint.contains("yourapi.com");
    }

    @Override
    public Mono<OhlcApiResponse> fetchOhlcData(
            DataSource dataSource, 
            String symbol, 
            PriceInterval interval) {
        // Implement API call logic here
        String url = buildUrl(dataSource, symbol, interval);
        
        return webClient.get()
                .uri(url)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(OhlcApiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)));
    }

    @Override
    public String getProviderName() {
        return "Your API Name";
    }

    private String buildUrl(DataSource dataSource, String symbol, PriceInterval interval) {
        // Build your API URL here
        return String.format("%s/%s?interval=%s", 
                dataSource.getApiEndpoint(), symbol, interval.getValue());
    }
}
```

### Step 2: Register the Provider

The provider will be automatically discovered by Spring if:
- It's annotated with `@Component`
- It's in a package scanned by Spring
- It implements `MarketDataProvider`

### Step 3: Configure Data Source

Add a data source in the database:

```sql
INSERT INTO data_sources (name, provider_type, api_endpoint, api_key, is_active, created_at, updated_at)
VALUES (
    'Your API',
    'REST_API',
    'https://api.yourapi.com/v1/stock',
    'YOUR_API_KEY',
    true,
    NOW(),
    NOW()
);
```

The `supports()` method will automatically match this data source to your provider.

## Provider Selection Logic

The `MarketDataProviderService` automatically selects the correct provider by:
1. Iterating through all registered providers
2. Calling `supports()` on each provider
3. Using the first provider that returns `true`

## Best Practices

1. **Error Handling**: Always handle API errors gracefully
2. **Retry Logic**: Implement retry for transient failures
3. **Timeout**: Respect the timeout configured in DataSource
4. **Rate Limiting**: Consider implementing rate limiting per provider
5. **Logging**: Log important events (API calls, errors, etc.)
6. **Testing**: Write unit tests for your provider

## Example: Adding IEX Cloud Provider

```java
@Component
@RequiredArgsConstructor
public class IexCloudProvider implements MarketDataProvider {
    
    private final WebClient webClient;

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource.getApiEndpoint().contains("iexcloud.io") ||
               dataSource.getName().toLowerCase().contains("iex");
    }

    @Override
    public Mono<OhlcApiResponse> fetchOhlcData(DataSource dataSource, String symbol, PriceInterval interval) {
        String url = String.format("%s/stock/%s/chart/%s?token=%s",
                dataSource.getApiEndpoint(), symbol, interval.getValue(), dataSource.getApiKey());
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(OhlcApiResponse.class);
    }

    @Override
    public String getProviderName() {
        return "IEX Cloud";
    }
}
```

That's it! The provider will be automatically available once you restart the application.
