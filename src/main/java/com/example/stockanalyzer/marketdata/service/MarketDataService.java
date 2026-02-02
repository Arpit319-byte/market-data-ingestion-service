package com.example.stockanalyzer.marketdata.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.repository.StockPriceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final MarketDataProviderService marketDataProviderService;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final DataSourceRepository dataSourceRepository;

    /**
     * Fetches and saves OHLC data for a stock from a data source
     * 
     * @param stockId Stock ID
     * @param dataSourceId Data source ID
     * @param interval Price interval
     * @return List of saved stock prices
     */
    @Transactional
    public Mono<List<StockPrice>> fetchAndSaveOhlcData(Long stockId, Long dataSourceId, PriceInterval interval) {
        log.info("Fetching OHLC data for stockId: {}, dataSourceId: {}, interval: {}", 
                stockId, dataSourceId, interval);

        // Fetch stock and data source
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new MarketDataException("Stock not found with id: " + stockId));
        
        DataSource dataSource = dataSourceRepository.findById(dataSourceId)
                .orElseThrow(() -> new MarketDataException("Data source not found with id: " + dataSourceId));

        if (!dataSource.getIsActive()) {
            throw new MarketDataException("Data source is not active: " + dataSource.getName());
        }

        // Fetch data from API using the appropriate provider
        return marketDataProviderService.fetchOhlcData(dataSource, stock.getSymbol(), interval)
                .map(response -> {
                    // Validate response
                    if (response.getErrorMessage() != null) {
                        throw new MarketDataException("API Error: " + response.getErrorMessage());
                    }
                    if (response.getNote() != null) {
                        throw new MarketDataException("API Note: " + response.getNote());
                    }

                    // Parse and save data
                    List<StockPrice> stockPrices = parseAndSaveOhlcData(response, stock, dataSource, interval);
                    log.info("Successfully saved {} price records", stockPrices.size());
                    return stockPrices;
                });
    }

    /**
     * Parses API response and saves to database
     */
    private List<StockPrice> parseAndSaveOhlcData(
            OhlcApiResponse response, 
            Stock stock, 
            DataSource dataSource, 
            PriceInterval interval) {
        
        List<StockPrice> stockPrices = new ArrayList<>();
        
        // Parse Alpha Vantage format
        Map<String, OhlcApiResponse.TimeSeriesData> timeSeries = getTimeSeriesForInterval(response, interval);
        
        if (timeSeries != null && !timeSeries.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Map.Entry<String, OhlcApiResponse.TimeSeriesData> entry : timeSeries.entrySet()) {
                String dateTimeStr = entry.getKey();
                OhlcApiResponse.TimeSeriesData data = entry.getValue();
                
                try {
                    Instant timestamp = parseTimestamp(dateTimeStr, interval);
                    
                    StockPrice stockPrice = new StockPrice();
                    stockPrice.setStock(stock);
                    stockPrice.setDataSource(dataSource);
                    stockPrice.setTimestamp(timestamp);
                    stockPrice.setInterval(interval);
                    stockPrice.setOpen(parseBigDecimal(data.getOpen() != null ? data.getOpen() : 
                            (data.getOpenPrice() != null ? data.getOpenPrice().toString() : null)));
                    stockPrice.setHigh(parseBigDecimal(data.getHigh() != null ? data.getHigh() : 
                            (data.getHighPrice() != null ? data.getHighPrice().toString() : null)));
                    stockPrice.setLow(parseBigDecimal(data.getLow() != null ? data.getLow() : 
                            (data.getLowPrice() != null ? data.getLowPrice().toString() : null)));
                    stockPrice.setClose(parseBigDecimal(data.getClose() != null ? data.getClose() : 
                            (data.getClosePrice() != null ? data.getClosePrice().toString() : null)));
                    stockPrice.setVolume(parseLong(data.getVolume() != null ? data.getVolume() : 
                            (data.getVolumeValue() != null ? data.getVolumeValue().toString() : null)));
                    
                    // Check if record already exists (avoid duplicates)
                    boolean exists = stockPriceRepository.existsByStockIdAndTimestampAndInterval(
                            stock.getId(), timestamp, interval);
                    
                    if (!exists) {
                        stockPrices.add(stockPrice);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse price data for timestamp {}: {}", dateTimeStr, e.getMessage());
                }
            }
        }
        
        // Save all records
        if (!stockPrices.isEmpty()) {
            stockPriceRepository.saveAll(stockPrices);
        }
        
        return stockPrices;
    }

    private Map<String, OhlcApiResponse.TimeSeriesData> getTimeSeriesForInterval(
            OhlcApiResponse response, PriceInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> response.getTimeSeries1min();
            case FIVE_MINUTE -> response.getTimeSeries5min();
            case FIFTEEN_MINUTE -> response.getTimeSeries15min();
            case ONE_HOUR -> response.getTimeSeries60min();
            case ONE_DAY -> response.getTimeSeriesDaily();
            default -> response.getTimeSeriesDaily();
        };
    }

    private Instant parseTimestamp(String dateTimeStr, PriceInterval interval) {
        try {
            // Try parsing as date-time (for intraday) - format: "2024-01-25 16:00:00"
            if (dateTimeStr.contains(" ")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDate.parse(dateTimeStr.split(" ")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant();
            } else {
                // Parse as date only (for daily) - format: "2024-01-25"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateTimeStr, formatter)
                        .atStartOfDay(ZoneId.of("UTC"))
                        .toInstant();
            }
        } catch (Exception e) {
            log.error("Failed to parse timestamp: {}", dateTimeStr, e);
            return Instant.now();
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse null or empty value to BigDecimal");
        }
        return new BigDecimal(value.trim());
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse null or empty value to Long");
        }
        return Long.parseLong(value.trim());
    }
}
