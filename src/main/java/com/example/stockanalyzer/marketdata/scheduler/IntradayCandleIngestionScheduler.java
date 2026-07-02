package com.example.stockanalyzer.marketdata.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.stockanalyzer.marketdata.entites.IntervalType;
import com.example.stockanalyzer.marketdata.service.CandleIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntradayCandleIngestionScheduler {

    private final CandleIngestionService candleIngestionService;

    @Scheduled(cron = "${kite.scheduler.intraday-cron:0 */5 9-15 * * MON-FRI}", zone = "Asia/Kolkata")
    public void fetchIntradayCandles() {
        try {
            candleIngestionService.fetchAndSaveForActiveInstruments(IntervalType.FIVE_MINUTE);
        } catch (Exception e) {
            log.error("Intraday candle ingestion failed", e);
        }
    }
}
