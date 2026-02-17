package com.example.stockanalyzer.marketdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.service.GrowwInstrumentService;
import com.example.stockanalyzer.marketdata.service.GrowwInstrumentService.SyncResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/api/admin")
@RequiredArgsConstructor
public class InstrumentSyncController {

    private final GrowwInstrumentService growwInstrumentService;

    @PostMapping("/sync-instruments")
    public ResponseEntity<SyncResult> syncInstruments(){
        log.info("Manual instruments sync triggered");
        SyncResult result = growwInstrumentService.fetchAndSyncInstrument();
        return ResponseEntity.ok(result);
    }
    
}
