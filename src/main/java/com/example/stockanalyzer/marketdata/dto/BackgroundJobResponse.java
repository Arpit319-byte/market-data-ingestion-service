package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;

public record BackgroundJobResponse(
        Long id,
        String jobType,
        String status,
        String message,
        Integer savedCount,
        Instant startedAt,
        Instant finishedAt) {
}
