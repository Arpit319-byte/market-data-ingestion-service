package com.example.stockanalyzer.marketdata.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.dto.CandleQueryRequest;
import com.example.stockanalyzer.marketdata.dto.CandleResponse;
import com.example.stockanalyzer.marketdata.dto.FetchCandlesRequest;
import com.example.stockanalyzer.marketdata.service.CandleIngestionService;
import com.example.stockanalyzer.marketdata.service.CandleQueryService;
import com.example.stockanalyzer.marketdata.service.HistoricalCandleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/candles")
@RequiredArgsConstructor
public class CandleController {

    private final CandleQueryService candleQueryService;
    private final CandleIngestionService candleIngestionService;
    private final HistoricalCandleService historicalCandleService;

    @GetMapping
    public List<CandleResponse> query(
            @RequestParam Long instrumentId,
            CandleQueryRequest request) {
        return candleQueryService.query(instrumentId, request);
    }

    @PostMapping("/fetch")
    public int fetch(@RequestBody FetchCandlesRequest request) {
        return candleIngestionService.fetchAndSave(request);
    }

    @PostMapping("/backfill")
    public int backfill(
            @RequestParam(defaultValue = "90") int minuteDays,
            @RequestParam(defaultValue = "365") int dailyDays) {
        return historicalCandleService.backfill(minuteDays, dailyDays);
    }
}
