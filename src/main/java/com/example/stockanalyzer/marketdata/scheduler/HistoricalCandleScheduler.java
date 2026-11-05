package com.example.stockanalyzer.marketdata.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.service.CandleRetentionService;
import com.example.stockanalyzer.marketdata.service.HistoricalCandleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoricalCandleScheduler {

    private final HistoricalCandleService historicalCandleService;
    private final CandleRetentionService candleRetentionService;

    @Scheduled(cron = "${kite.scheduler.reconciliation-cron:0 0 16 * * MON-FRI}", zone = "Asia/Kolkata")
    public void reconcileAfterClose() {
        try {
            historicalCandleService.reconcileToday();
        } catch (RuntimeException e) {
            log.error("Daily candle reconciliation failed", e);
        }
    }

    @Scheduled(cron = "${kite.scheduler.retention-cron:0 0 2 * * *}", zone = "Asia/Kolkata")
    public void pruneOldCandles() {
        try {
            candleRetentionService.pruneOldOneMinuteCandles();
        } catch (RuntimeException e) {
            log.error("One-minute candle retention failed", e);
        }
    }
}
