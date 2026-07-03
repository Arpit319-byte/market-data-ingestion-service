package com.example.stockanalyzer.marketdata.provider;

import java.util.List;

import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.Instrument;

public interface MarketDataProvider {

    boolean supports(DataSource dataSource);

    String getProviderName();

    List<Candle> fetchCandles(
            DataSource dataSource,
            Instrument instrument,
            FetchCandlesRequest request);
}
