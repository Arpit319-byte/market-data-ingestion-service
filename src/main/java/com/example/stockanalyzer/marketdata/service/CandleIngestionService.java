package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.IntervalType;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.exception.NotFoundException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;
import com.example.stockanalyzer.marketdata.provider.MarketDataProviderRegistry;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import com.example.stockanalyzer.marketdata.stream.CandleStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleIngestionService {

    private final DataSourceRepository dataSourceRepository;
    private final InstrumentRepository instrumentRepository;
    private final MarketDataProviderRegistry providerRegistry;
    private final CandleStore candleStore;

    @Value("${kite.backfill.delay-ms:350}")
    private long providerDelayMs;

    public int fetchAndSave(FetchCandlesRequest request) {
        DataSource dataSource = dataSourceRepository.findById(request.getDataSourceId())
                .orElseThrow(() -> new NotFoundException("Data source not found: " + request.getDataSourceId()));
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException("Instrument not found: " + request.getInstrumentId()));
        return fetchOne(dataSource, instrument, request);
    }

    public int fetchRange(IntervalType intervalType, Instant from, Instant to) {
        DataSource dataSource = activeDataSource();
        int saved = 0;
        for (Instrument instrument : instrumentRepository.findByIsActiveTrueAndKiteInstrumentTokenIsNotNull()) {
            saved += fetchOne(dataSource, instrument, new FetchCandlesRequest(
                    instrument.getId(),
                    dataSource.getId(),
                    intervalType,
                    from,
                    to));
            pause();
        }
        log.info("Saved {} candles for interval {} between {} and {}", saved, intervalType, from, to);
        return saved;
    }

    private int fetchOne(DataSource dataSource, Instrument instrument, FetchCandlesRequest request) {
        MarketDataProvider provider = providerRegistry.resolve(dataSource);
        return candleStore.save(provider.fetchCandles(dataSource, instrument, request));
    }

    private DataSource activeDataSource() {
        return dataSourceRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                .findFirst()
                .orElseThrow(() -> new MarketDataException("No active data source configured"));
    }

    private void pause() {
        if (providerDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(providerDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MarketDataException("Candle fetch was interrupted", e);
        }
    }
}
