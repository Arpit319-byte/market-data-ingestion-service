package com.example.stockanalyzer.marketdata.service;

import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entities.BackgroundJob;
import com.example.stockanalyzer.marketdata.logging.IncidentContext;

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
        IncidentContext.putJobId(jobId);
        log.info("Candle backfill job {} started", jobId);
        try {
            BackgroundJob job = candleBackfillJobService.markRunning(jobId);
            int saved = historicalCandleService.backfill(job.getMinuteDays(), job.getDailyDays());
            candleBackfillJobService.markSucceeded(jobId, saved);
            log.info("Candle backfill job {} saved {} candles", jobId, saved);
        } catch (RuntimeException e) {
            log.error("Candle backfill job {} failed", jobId, e);
            candleBackfillJobService.markFailed(jobId, e.getMessage());
        } finally {
            MDC.remove(IncidentContext.JOB_ID);
        }
    }
}
