package com.example.stockanalyzer.marketdata.scheduler;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;
import com.example.stockanalyzer.marketdata.service.MarketDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job that fetches Groww live OHLC data every 30 seconds (configurable).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrowwLiveDataScheduler {

    private final MarketDataService marketDataService;
    private final StockRepository stockRepository;
    private final DataSourceRepository dataSourceRepository;

    @Value("${groww.scheduler.enabled:true}")
    private boolean enabled;

    @Value("${groww.scheduler.interval-ms:30000}")
    private long intervalMs;

    /**
     * Fetches live OHLC data from Groww API every 30 seconds (default).
     * Runs only when groww.scheduler.enabled=true.
     */
    @Scheduled(fixedRateString = "${groww.scheduler.interval-ms:30000}", initialDelayString = "${groww.scheduler.initial-delay-ms:5000}")
    public void fetchGrowwLiveData() {
        if (!enabled) {
            return;
        }

        log.debug("Groww live data scheduler running (interval: {} ms)", intervalMs);

        DataSource growwDataSource = dataSourceRepository.findByNameIgnoreCase("Grow API")
                .or(() -> dataSourceRepository.findByNameIgnoreCase("Groww API"))
                .orElseGet(() -> {
                    log.warn("No Groww data source found. Create a data source with name 'Grow API' or 'Groww API'.");
                    return null;
                });

        if (growwDataSource == null || !growwDataSource.getIsActive()) {
            return;
        }

        List<Stock> stocks = stockRepository.findByIsActiveTrue();
        if (stocks.isEmpty()) {
            log.debug("No active stocks to fetch. Add stocks and set isactive=true.");
            return;
        }

        Long dataSourceId = growwDataSource.getId();
        int successCount = 0;
        int failCount = 0;

        for (Stock stock : stocks) {
            try {
                marketDataService.fetchAndSaveOhlcData(stock.getId(), dataSourceId, PriceInterval.ONE_DAY)
                        .block(Duration.ofSeconds(30));
                successCount++;
            } catch (MarketDataException e) {
                log.warn("Failed to fetch Groww data for stock {} ({}): {}", 
                        stock.getSymbol(), stock.getId(), e.getMessage());
                failCount++;
            } catch (Exception e) {
                log.warn("Failed to fetch Groww data for stock {} ({}): {}", 
                        stock.getSymbol(), stock.getId(), e.getMessage());
                failCount++;
            }
        }

        if (successCount > 0 || failCount > 0) {
            log.info("Groww live data fetch completed: {} succeeded, {} failed (total: {})", 
                    successCount, failCount, stocks.size());
        }
    }
}
