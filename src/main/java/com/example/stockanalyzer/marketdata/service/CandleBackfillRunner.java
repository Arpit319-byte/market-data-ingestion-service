package com.example.stockanalyzer.marketdata.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entities.BackgroundJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleBackfillRunner {

    private final CandleBackfillJobService candleBackfillJobService;
    private final HistoricalCandleService historicalCandleService;

    @Async
    public void run(Long jobId) {
        try {
            BackgroundJob job = candleBackfillJobService.markRunning(jobId);
            int saved = historicalCandleService.backfill(job.getMinuteDays(), job.getDailyDays());
            candleBackfillJobService.markSucceeded(jobId, saved);
        } catch (RuntimeException e) {
            log.error("Candle backfill job {} failed", jobId, e);
            candleBackfillJobService.markFailed(jobId, e.getMessage());
        }
    }
}
