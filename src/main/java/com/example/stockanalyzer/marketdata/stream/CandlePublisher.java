package com.example.stockanalyzer.marketdata.stream;

import java.util.List;

import com.example.stockanalyzer.marketdata.entities.Candle;

public interface CandlePublisher {

    void publish(List<Candle> candles);
}
