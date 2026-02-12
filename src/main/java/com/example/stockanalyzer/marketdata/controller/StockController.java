package com.example.stockanalyzer.marketdata.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.service.StockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only APIs for stocks and stock prices.
 */
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * List all stocks (optionally active only).
     * Example: GET /api/stocks?activeOnly=true
     */
    @GetMapping
    public ResponseEntity<List<Stock>> listStocks(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(stockService.listStocks(activeOnly));
    }

    /**
     * Get a stock by ID.
     * Example: GET /api/stocks/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStock(@PathVariable Long id) {
        return stockService.getStock(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get stock prices for a stock within a date range.
     * Example: GET /api/stocks/1/prices?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z&interval=ONE_DAY
     */
    @GetMapping("/{id}/prices")
    public ResponseEntity<List<StockPrice>> getStockPrices(
            @PathVariable Long id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) PriceInterval interval) {
        Instant fromInstant;
        Instant toInstant;
        try {
            fromInstant = (from != null && !from.isBlank()) ? Instant.parse(from) : Instant.EPOCH;
            toInstant = (to != null && !to.isBlank()) ? Instant.parse(to) : Instant.now();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return stockService.getStockPrices(id, fromInstant, toInstant, interval)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
