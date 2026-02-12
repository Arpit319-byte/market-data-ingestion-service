package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.repository.StockPriceRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for reading stocks and stock prices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    /**
     * List all stocks, optionally filtered to active only.
     */
    public List<Stock> listStocks(boolean activeOnly) {
        return activeOnly ? stockRepository.findByIsActiveTrue() : stockRepository.findAll();
    }

    /**
     * Get a stock by ID.
     */
    public Optional<Stock> getStock(Long id) {
        return stockRepository.findById(id);
    }

    /**
     * Get stock prices for a stock within a date range.
     * Returns empty optional if the stock does not exist.
     */
    public Optional<List<StockPrice>> getStockPrices(
            Long stockId,
            Instant from,
            Instant to,
            PriceInterval interval) {
        if (!stockRepository.existsById(stockId)) {
            return Optional.empty();
        }
        List<StockPrice> prices = interval != null
                ? stockPriceRepository.findByStockIdAndIntervalAndTimestampBetweenOrderByTimestampAsc(
                        stockId, interval, from, to)
                : stockPriceRepository.findByStockIdAndTimestampBetweenOrderByTimestampAsc(stockId, from, to);
        return Optional.of(prices);
    }
}
