package com.example.stockanalyzer.marketdata.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MarketDataProviderRegistry {

    private final List<MarketDataProvider> providers;

    public MarketDataProvider resolve(DataSource dataSource) {
        return providers.stream()
                .filter(provider -> provider.supports(dataSource))
                .findFirst()
                .orElseThrow(() -> new MarketDataException(
                        "No market data provider found for data source: " + dataSource.getName()));
    }
}
