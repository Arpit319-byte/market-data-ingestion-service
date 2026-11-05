package com.example.stockanalyzer.marketdata.stream;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface MarketSession {

    ZoneId zone();

    boolean isOpen(ZonedDateTime now);
}
