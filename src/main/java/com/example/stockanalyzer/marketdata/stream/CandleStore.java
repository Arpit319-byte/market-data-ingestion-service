package com.example.stockanalyzer.marketdata.stream;

import java.util.List;

import com.example.stockanalyzer.marketdata.entities.Candle;

public interface CandleStore {

    int save(List<Candle> candles);
}
