package com.example.stockanalyzer.marketdata.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.stockanalyzer.marketdata.entities.ProviderType;
import com.example.stockanalyzer.marketdata.provider.impl.UnavailableMarketDataProvider;

@Configuration
public class AdditionalProviderConfig {

    @Bean
    MarketDataProvider upstoxMarketDataProvider() {
        return new UnavailableMarketDataProvider(ProviderType.UPSTOX, "Upstox");
    }

    @Bean
    MarketDataProvider fyersMarketDataProvider() {
        return new UnavailableMarketDataProvider(ProviderType.FYERS, "Fyers");
    }

    @Bean
    MarketDataProvider growwMarketDataProvider() {
        return new UnavailableMarketDataProvider(ProviderType.GROWW, "Groww");
    }
}
