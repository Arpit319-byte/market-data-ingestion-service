package com.example.stockanalyzer.marketdata.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.dto.InstrumentResponse;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import com.example.stockanalyzer.marketdata.service.InstrumentSyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentSyncService instrumentSyncService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<InstrumentResponse> list() {
        return instrumentRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/sync")
    public int syncNseEquities() {
        return instrumentSyncService.syncNseEquities();
    }

    private InstrumentResponse toResponse(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getExchange().getId(),
                instrument.getExchange().getCode(),
                instrument.getSegment(),
                instrument.getSeries(),
                instrument.getInstrumentType(),
                instrument.isActive(),
                instrument.getCreatedAt(),
                instrument.getUpdatedAt());
    }
}
