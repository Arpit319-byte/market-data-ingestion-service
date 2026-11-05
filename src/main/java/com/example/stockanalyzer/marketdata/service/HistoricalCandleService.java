package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entities.IntervalType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalCandleService {

    static final ZoneId MARKET_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MINUTE_CHUNK_DAYS = 60;

    private final CandleIngestionService candleIngestionService;

    public int backfill(int minuteDays, int dailyDays) {
        Instant now = Instant.now();
        int saved = candleIngestionService.fetchRange(
                IntervalType.ONE_DAY,
                now.minus(dailyDays, ChronoUnit.DAYS),
                now);

        Instant minuteFrom = now.minus(minuteDays, ChronoUnit.DAYS);
        Instant chunkStart = minuteFrom;
        while (chunkStart.isBefore(now)) {
            Instant chunkEnd = chunkStart.plus(MINUTE_CHUNK_DAYS, ChronoUnit.DAYS);
            if (chunkEnd.isAfter(now)) {
                chunkEnd = now;
            }
            saved += candleIngestionService.fetchRange(IntervalType.ONE_MINUTE, chunkStart, chunkEnd);
            chunkStart = chunkEnd;
        }
        log.info("Historical backfill saved {} candles", saved);
        return saved;
    }

    public int reconcileToday() {
        LocalDate today = LocalDate.now(MARKET_ZONE);
        Instant from = today.atTime(9, 15).atZone(MARKET_ZONE).toInstant();
        Instant to = today.atTime(15, 30).atZone(MARKET_ZONE).toInstant();
        int saved = candleIngestionService.fetchRange(IntervalType.ONE_MINUTE, from, to);
        saved += candleIngestionService.fetchRange(IntervalType.ONE_DAY, from, to);
        log.info("Reconciled {} candles for {}", saved, today);
        return saved;
    }
}
