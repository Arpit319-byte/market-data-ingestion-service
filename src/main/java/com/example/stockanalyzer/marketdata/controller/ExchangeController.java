package com.example.stockanalyzer.marketdata.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.dto.ExchangeResponse;
import com.example.stockanalyzer.marketdata.entities.Exchange;
import com.example.stockanalyzer.marketdata.repository.ExchangeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeRepository exchangeRepository;

    @GetMapping
    public List<ExchangeResponse> list() {
        return exchangeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ExchangeResponse toResponse(Exchange exchange) {
        return new ExchangeResponse(
                exchange.getId(),
                exchange.getName(),
                exchange.getCode(),
                exchange.getCountry(),
                exchange.getCurrency(),
                exchange.getOpenTime(),
                exchange.getCloseTime(),
                exchange.getTimezone(),
                exchange.isActive(),
                exchange.getCreatedAt(),
                exchange.getUpdatedAt());
    }
}
