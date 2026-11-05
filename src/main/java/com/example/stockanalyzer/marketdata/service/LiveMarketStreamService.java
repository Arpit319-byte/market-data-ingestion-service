package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import com.example.stockanalyzer.marketdata.stream.ClosedCandleFlusher;
import com.example.stockanalyzer.marketdata.stream.MarketDataStream;
import com.example.stockanalyzer.marketdata.stream.MarketDataStreamRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LiveMarketStreamService {

    private final DataSourceRepository dataSourceRepository;
    private final InstrumentRepository instrumentRepository;
    private final MarketDataStreamRegistry streamRegistry;
    private final ClosedCandleFlusher closedCandleFlusher;

    public void start() {
        try {
            DataSource dataSource = dataSourceRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                    .filter(streamRegistry::supports)
                    .findFirst()
                    .orElse(null);
            if (dataSource == null) {
                log.warn("No active data source has a live stream; stream was not started");
                return;
            }
            List<Instrument> instruments = instrumentRepository.findByIsActiveTrueAndKiteInstrumentTokenIsNotNull();
            streamRegistry.resolve(dataSource).start(dataSource, instruments);
        } catch (RuntimeException e) {
            log.error("Failed to start live market stream", e);
        }
    }

    public void stop() {
        try {
            for (MarketDataStream stream : streamRegistry.all()) {
                if (stream.isRunning()) {
                    stream.stop();
                }
            }
            closedCandleFlusher.flushAll();
            log.info("Live market stream stopped");
        } catch (RuntimeException e) {
            log.error("Failed to stop live market stream", e);
        }
    }

    public void flushClosedCandles() {
        closedCandleFlusher.flushClosed(Instant.now());
    }
}
