package com.example.stockanalyzer.marketdata.provider.impl;

import java.util.List;

import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.ProviderType;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;

public class UnavailableMarketDataProvider implements MarketDataProvider {

    private final ProviderType providerType;
    private final String providerName;

    public UnavailableMarketDataProvider(ProviderType providerType, String providerName) {
        this.providerType = providerType;
        this.providerName = providerName;
    }

    @Override
    public boolean supports(DataSource dataSource) {
        return dataSource != null && providerType.equals(dataSource.getProviderType());
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public List<Candle> fetchCandles(DataSource dataSource, Instrument instrument, FetchCandlesRequest request) {
        throw new MarketDataException(providerName + " market data is not connected yet");
    }
}
