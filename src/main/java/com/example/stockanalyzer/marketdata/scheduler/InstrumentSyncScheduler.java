package com.example.stockanalyzer.marketdata.scheduler;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.service.GrowwInstrumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "groww.instruments.sync-scheduled", havingValue = "true")
public class InstrumentSyncScheduler {

    private final GrowwInstrumentService growwInstrumentService;

    @Value("${groww.instruments.sync-cron:0 30 21 * * SUN}")
    private String cronExpression;

    @Scheduled(cron = "${groww.instruments.sync-cron:0 30 21 * * SUN}")
    public void syncInstruments() {
        log.info("Scheduled instruments sync started");
        try {
            growwInstrumentService.fetchAndSyncInstrument();
        } catch (Exception e) {
            log.error("Instruments sync failed", e);
        }
    }
}
