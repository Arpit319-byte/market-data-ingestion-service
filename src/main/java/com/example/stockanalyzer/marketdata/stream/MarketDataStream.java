package com.example.stockanalyzer.marketdata.stream;

import java.util.List;

import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Instrument;

public interface MarketDataStream {

    boolean supports(DataSource dataSource);

    void start(DataSource dataSource, List<Instrument> instruments);

    void stop();

    boolean isRunning();
}
