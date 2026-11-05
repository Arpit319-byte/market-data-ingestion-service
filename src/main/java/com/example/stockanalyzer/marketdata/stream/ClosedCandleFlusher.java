package com.example.stockanalyzer.marketdata.stream;

import java.time.Instant;

public interface ClosedCandleFlusher {

    void flushClosed(Instant now);

    void flushAll();
}
