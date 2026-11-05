package com.example.stockanalyzer.marketdata.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.dto.MarketHealthResponse;
import com.example.stockanalyzer.marketdata.service.MarketHealthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class MarketHealthController {

    private final MarketHealthService marketHealthService;

    @GetMapping("/market")
    public MarketHealthResponse market() {
        return marketHealthService.current();
    }
}
