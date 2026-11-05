package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.Instrument;
import com.example.stockanalyzer.marketdata.entites.IntervalType;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.provider.MarketDataProvider;
import com.example.stockanalyzer.marketdata.provider.MarketDataProviderRegistry;
import com.example.stockanalyzer.marketdata.repository.CandleRepository;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleIngestionService {

    private final DataSourceRepository dataSourceRepository;
    private final InstrumentRepository instrumentRepository;
    private final CandleRepository candleRepository;
    private final MarketDataProviderRegistry providerRegistry;

    @Transactional
    public int fetchAndSave(FetchCandlesRequest request) {
        DataSource dataSource = dataSourceRepository.findById(request.getDataSourceId())
                .orElseThrow(() -> new MarketDataException("Data source not found: " + request.getDataSourceId()));
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new MarketDataException("Instrument not found: " + request.getInstrumentId()));

        MarketDataProvider provider = providerRegistry.resolve(dataSource);
        List<Candle> candles = provider.fetchCandles(dataSource, instrument, request);
        return saveNewCandles(candles);
    }

    @Transactional
    public int fetchAndSaveForActiveInstruments(IntervalType intervalType) {
        DataSource dataSource = dataSourceRepository.findByIsActiveTrueOrderByPriorityAsc().stream()
                .findFirst()
                .orElseThrow(() -> new MarketDataException("No active data source configured"));
        MarketDataProvider provider = providerRegistry.resolve(dataSource);

        Instant to = Instant.now();
        Instant from = to.minusSeconds(intervalType.getMinutes() * 60L * 3L);
        int saved = 0;

        for (Instrument instrument : instrumentRepository.findByIsActiveTrueAndKiteInstrumentTokenIsNotNull()) {
            FetchCandlesRequest request = new FetchCandlesRequest(
                    instrument.getId(),
                    dataSource.getId(),
                    intervalType,
                    from,
                    to);
            saved += saveNewCandles(provider.fetchCandles(dataSource, instrument, request));
        }

        log.info("Saved {} new candles for interval {}", saved, intervalType);
        return saved;
    }

    private int saveNewCandles(List<Candle> candles) {
        int saved = 0;
        for (Candle candle : candles) {
            boolean exists = candleRepository.findByInstrumentIdAndIntervalTypeAndCandleStart(
                    candle.getInstrument().getId(),
                    candle.getIntervalType(),
                    candle.getCandleStart()).isPresent();

            if (!exists) {
                candleRepository.save(candle);
                saved++;
            }
        }
        return saved;
    }
}
