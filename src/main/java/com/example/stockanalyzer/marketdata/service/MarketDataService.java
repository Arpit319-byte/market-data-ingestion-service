package com.example.stockanalyzer.marketdata.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataIngestionJob;
import com.example.stockanalyzer.marketdata.entites.DataIngestionJob.JobStatus;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.event.StockPriceUpdateEvent;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.repository.DataIngestionJobRepository;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.StockPriceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service that orchestrates fetching OHLC data from providers, persisting to DB,
 * tracking ingestion jobs, and publishing price update events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private static final String JOB_TYPE_FETCH_OHLC = "FETCH_OHLC";
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StockRepository stockRepository;
    private final DataSourceRepository dataSourceRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DataIngestionJobRepository dataIngestionJobRepository;
    private final MarketDataProviderService marketDataProviderService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Fetches OHLC data from the configured provider and saves new records.
     * Does not run inside a transaction; use fetchAndSaveOhlcDataBlocking for transactional behaviour.
     */
    public Mono<List<StockPrice>> fetchAndSaveOhlcData(Long stockId, Long dataSourceId, PriceInterval interval) {
        return Mono.fromCallable(() -> loadStockAndDataSource(stockId, dataSourceId))
                .flatMap(pair -> {
                    Stock stock = pair.left();
                    DataSource dataSource = pair.right();
                    DataIngestionJob job = createJob(dataSource, stock, interval);
                    return marketDataProviderService.fetchOhlcData(dataSource, stock.getSymbol(), interval)
                            .map(response -> convertAndSave(response, stock, dataSource, interval, job))
                            .doOnSuccess(result -> {
                                completeJob(job, result.fetched(), result.saved().size(), null);
                                if (!result.saved().isEmpty()) {
                                    eventPublisher.publishEvent(new StockPriceUpdateEvent(this, result.saved()));
                                }
                            })
                            .map(result -> result.saved())
                            .onErrorResume(e -> {
                                failJob(job, e.getMessage());
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

    private DataIngestionJob createJob(DataSource dataSource, Stock stock, PriceInterval interval) {
        DataIngestionJob job = new DataIngestionJob();
        job.setDataSource(dataSource);
        job.setStock(stock);
        job.setJobType(JOB_TYPE_FETCH_OHLC);
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        job.setIntervalType(interval);
        job.setRetryCount(0);
        job = dataIngestionJobRepository.save(job);
        return job;
    }

    private void completeJob(DataIngestionJob job, int recordsFetched, int recordsSaved, String errorMessage) {
        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        job.setRecordsFetched(recordsFetched);
        job.setRecordsSaved(recordsSaved);
        job.setErrorMessage(errorMessage);
        dataIngestionJobRepository.save(job);
    }

    private void failJob(DataIngestionJob job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setCompletedAt(Instant.now());
        job.setErrorMessage(errorMessage != null && errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage);
        dataIngestionJobRepository.save(job);
    }

    private record ConvertResult(int fetched, List<StockPrice> saved) {}

    private ConvertResult convertAndSave(OhlcApiResponse response, Stock stock, DataSource dataSource,
                                        PriceInterval interval, DataIngestionJob job) {
        Map<String, OhlcApiResponse.TimeSeriesData> series = selectTimeSeries(response, interval);
        if (series == null || series.isEmpty()) {
            log.warn("No time series data for interval {} in response", interval);
            return new ConvertResult(0, List.of());
        }
        int fetched = series.size();
        List<StockPrice> saved = new ArrayList<>();
        for (Map.Entry<String, OhlcApiResponse.TimeSeriesData> entry : series.entrySet()) {
            Instant timestamp = parseTimestamp(entry.getKey());
            OhlcApiResponse.TimeSeriesData data = entry.getValue();
            if (data == null) continue;
            if (stockPriceRepository.existsByStockIdAndTimestampAndInterval(stock.getId(), timestamp, interval)) {
                continue;
            }
            StockPrice sp = toStockPrice(stock, dataSource, timestamp, interval, data);
            sp = stockPriceRepository.save(sp);
            saved.add(sp);
        }
        return new ConvertResult(fetched, saved);
    }

    private Map<String, OhlcApiResponse.TimeSeriesData> selectTimeSeries(OhlcApiResponse response, PriceInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> response.getTimeSeries1min();
            case FIVE_MINUTE -> response.getTimeSeries5min();
            case FIFTEEN_MINUTE -> response.getTimeSeries15min();
            case ONE_HOUR -> response.getTimeSeries60min();
            case ONE_DAY, ONE_WEEK, ONE_MONTH, THIRTY_MINUTE, FOUR_HOUR -> response.getTimeSeriesDaily();
        };
    }

    private Instant parseTimestamp(String key) {
        if (key == null || key.isBlank()) return Instant.now();
        try {
            return Instant.parse(key);
        } catch (Exception ignored) {}
        try {
            return LocalDate.parse(key.trim(), DATE_ONLY).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception ignored) {}
        try {
            return java.time.LocalDateTime.parse(key.trim(), DATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {}
        return Instant.now();
    }

    private StockPrice toStockPrice(Stock stock, DataSource dataSource, Instant timestamp, PriceInterval interval,
                                    OhlcApiResponse.TimeSeriesData data) {
        StockPrice sp = new StockPrice();
        sp.setStock(stock);
        sp.setDataSource(dataSource);
        sp.setTimestamp(timestamp);
        sp.setInterval(interval);
        sp.setOpen(toBigDecimal(data.getOpen(), data.getOpenPrice()));
        sp.setHigh(toBigDecimal(data.getHigh(), data.getHighPrice()));
        sp.setLow(toBigDecimal(data.getLow(), data.getLowPrice()));
        sp.setClose(toBigDecimal(data.getClose(), data.getClosePrice()));
        sp.setVolume(toVolume(data.getVolume(), data.getVolumeValue()));
        return sp;
    }

    private static BigDecimal toBigDecimal(String s, BigDecimal fallback) {
        if (fallback != null) return fallback;
        if (s != null && !s.isBlank()) {
            try { return new BigDecimal(s.trim()); } catch (Exception ignored) {}
        }
        return BigDecimal.ZERO;
    }

    private static long toVolume(String s, Long fallback) {
        if (fallback != null) return fallback;
        if (s != null && !s.isBlank()) {
            try { return Long.parseLong(s.trim()); } catch (Exception ignored) {}
        }
        return 0L;
    }
}
