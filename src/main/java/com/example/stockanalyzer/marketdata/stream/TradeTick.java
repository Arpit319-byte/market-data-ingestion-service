package com.example.stockanalyzer.marketdata.stream;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.stockanalyzer.marketdata.entities.Instrument;

public record TradeTick(
        Instrument instrument,
        Instant timestamp,
        BigDecimal lastPrice,
        long volumeTradedToday) {
}
