package com.example.stockanalyzer.marketdata.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.repository.StockRepository;
import com.example.stockanalyzer.marketdata.service.GrowwInstrumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@ConditionalOnProperty(name = "groww.instruments.sync-on-startup", havingValue = "true")
public class InstrumentSyncRunner implements ApplicationRunner {

    private final StockRepository stockRepository;
    private final GrowwInstrumentService growwInstrumentService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        if(stockRepository.count()>0){
            log.info("Stock table has {} records , skipping instruments sync on startup",stockRepository.count());
            return ;
        }

        log.info("Stock table is empty, calling the instrument sync on startup");

        try{
            var result = growwInstrumentService.fetchAndSyncInstrument();
            log.info("Startup instruments sync complete: {} created, {} updated", result.created(), result.updated());
        } catch (Exception ex) {
            log.error("Startup instrument sync failed", ex);
        }
    }
    
}
