package com.example.stockanalyzer.marketdata.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataIngestionJob;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.event.StockPriceUpdateEvent;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.mapper.OhlcToStockPriceMapper;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.StockPriceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Orchestrates fetching OHLC data from providers, persisting to DB,
 * tracking ingestion jobs, and publishing price update events.
 * Delegates job lifecycle to DataIngestionJobService and conversion to OhlcToStockPriceMapper.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final StockRepository stockRepository;
    private final DataSourceRepository dataSourceRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DataIngestionJobService dataIngestionJobService;
    private final MarketDataProviderService marketDataProviderService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Fetches OHLC data from the configured provider and saves new records.
     */
    public Mono<List<StockPrice>> fetchAndSaveOhlcData(Long stockId, Long dataSourceId, PriceInterval interval) {
        return Mono.fromCallable(() -> loadStockAndDataSource(stockId, dataSourceId))
                .flatMap(pair -> {
                    Stock stock = pair.left();
                    DataSource dataSource = pair.right();
                    DataIngestionJob job = dataIngestionJobService.createOhlcFetchJob(dataSource, stock, interval);
                    return marketDataProviderService.fetchOhlcData(dataSource, stock.getSymbol(), interval)
                            .map(response -> convertAndSave(response, stock, dataSource, interval, job))
                            .doOnSuccess(result -> {
                                dataIngestionJobService.completeJob(job, result.fetched(), result.saved().size(), null);
                                if (!result.saved().isEmpty()) {
                                    eventPublisher.publishEvent(new StockPriceUpdateEvent(this, result.saved()));
                                }
                            })
                            .map(result -> result.saved())
                            .onErrorResume(e -> {
                                dataIngestionJobService.failJob(job, e.getMessage());
                                return Mono.error(e instanceof MarketDataException ? e : new MarketDataException(e.getMessage(), e));
                            });
                });
    }

    /**
     * Blocking, transactional variant for use from schedulers or synchronous callers.
     */
    @Transactional
    public List<StockPrice> fetchAndSaveOhlcDataBlocking(Long stockId, Long dataSourceId, PriceInterval interval) {
        return fetchAndSaveOhlcData(stockId, dataSourceId, interval)
                .block();
    }

    private record Pair<L, R>(L left, R right) {}
    private static <L, R> Pair<L, R> pair(L left, R right) { return new Pair<>(left, right); }

    private Pair<Stock, DataSource> loadStockAndDataSource(Long stockId, Long dataSourceId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new MarketDataException("Stock not found: " + stockId));
        DataSource dataSource = dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new MarketDataException("Data source not found: " + dataSourceId));
        return pair(stock, dataSource);
    }

    private record ConvertResult(int fetched, List<StockPrice> saved) {}

    private ConvertResult convertAndSave(OhlcApiResponse response, Stock stock, DataSource dataSource,
                                        PriceInterval interval, DataIngestionJob job) {
        Map<String, OhlcApiResponse.TimeSeriesData> series = OhlcToStockPriceMapper.selectTimeSeries(response, interval);
        if (series == null || series.isEmpty()) {
            log.warn("No time series data for interval {} in response", interval);
            return new ConvertResult(0, List.of());
        }
        int fetched = series.size();
        List<StockPrice> saved = new ArrayList<>();
        for (Map.Entry<String, OhlcApiResponse.TimeSeriesData> entry : series.entrySet()) {
            var timestamp = OhlcToStockPriceMapper.parseTimestamp(entry.getKey());
            OhlcApiResponse.TimeSeriesData data = entry.getValue();
            if (data == null) continue;
            if (stockPriceRepository.existsByStockIdAndTimestampAndInterval(stock.getId(), timestamp, interval)) {
                continue;
            }
            StockPrice sp = OhlcToStockPriceMapper.toStockPrice(stock, dataSource, timestamp, interval, data);
            sp = stockPriceRepository.save(sp);
            saved.add(sp);
        }
        return new ConvertResult(fetched, saved);
    }
}
