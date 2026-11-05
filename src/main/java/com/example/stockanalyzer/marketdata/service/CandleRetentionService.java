package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.entities.IntervalType;
import com.example.stockanalyzer.marketdata.repository.CandleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleRetentionService {

    private final CandleRepository candleRepository;

    @Value("${kite.retention.one-minute-days:180}")
    private long oneMinuteRetentionDays;

    @Transactional
    public int pruneOldOneMinuteCandles() {
        Instant cutoff = Instant.now().minus(oneMinuteRetentionDays, ChronoUnit.DAYS);
        int deleted = candleRepository.deleteByIntervalTypeAndCandleStartBefore(IntervalType.ONE_MINUTE, cutoff);
        log.info("Deleted {} one-minute candles older than {}", deleted, cutoff);
        return deleted;
    }
}
