package com.example.stockanalyzer.marketdata.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.StockPrice;
import com.example.stockanalyzer.marketdata.service.MarketDataProviderService;
import com.example.stockanalyzer.marketdata.service.MarketDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final MarketDataProviderService marketDataProviderService;

    /**
     * Endpoint to fetch and save OHLC data from third-party APIs
     * 
     * Example: POST /api/market-data/fetch?stockId=1&dataSourceId=1&interval=ONE_DAY
     */
    @PostMapping("/fetch")
    public Mono<ResponseEntity<List<StockPrice>>> fetchOhlcData(
            @RequestParam Long stockId,
            @RequestParam Long dataSourceId,
            @RequestParam(defaultValue = "ONE_DAY") PriceInterval interval) {
        
        return marketDataService.fetchAndSaveOhlcData(stockId, dataSourceId, interval)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * Get list of available market data providers
     * 
     * Example: GET /api/market-data/providers
     */
    @GetMapping("/providers")
    public ResponseEntity<List<String>> getAvailableProviders() {
        return ResponseEntity.ok(marketDataProviderService.getAvailableProviders());
    }
}
