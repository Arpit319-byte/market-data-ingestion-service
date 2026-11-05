package com.example.stockanalyzer.marketdata.dto;

public record MarketHealthResponse(
        boolean leader,
        String kiteSession,
        String liveStream) {
}
