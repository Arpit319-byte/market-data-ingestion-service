package com.example.stockanalyzer.marketdata.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.InstrumentRow;
import com.example.stockanalyzer.marketdata.entites.Exchange;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.fetcher.GrowwInstrumentFetcher;
import com.example.stockanalyzer.marketdata.parser.InstrumentCsvParser;
import com.example.stockanalyzer.marketdata.repository.ExchangeRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates instrument sync: fetch CSV, parse, apply business rules, persist.
 * Delegates fetching to GrowwInstrumentFetcher and parsing to InstrumentCsvParser.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrowwInstrumentService {

    private static final Set<String> ALLOWED_EXCHANGES = Set.of("NSE", "BSE");
    private static final String SEGMENT_CASH = "CASH";
    private static final String SERIES_EQ = "EQ";

    private final GrowwInstrumentFetcher growwInstrumentFetcher;
    private final ExchangeRepository exchangeRepository;
    private final StockRepository stockRepository;

    /**
     * Fetches instruments CSV from Groww and syncs CASH/EQ stocks to the database.
     */
    public SyncResult fetchAndSyncInstrument() {
        log.info("Starting Groww instruments sync");

        String csvBody = growwInstrumentFetcher.fetch();
        if (csvBody == null || csvBody.isBlank()) {
            log.warn("Empty response from instruments URL");
            return new SyncResult(0, 0);
        }

        return parseAndSync(csvBody);
    }

    @Transactional
    public SyncResult parseAndSync(String csvBody) {
        var rows = InstrumentCsvParser.parse(csvBody);
        if (rows.isEmpty()) {
            log.warn("CSV has no data rows or invalid headers");
            return new SyncResult(0, 0);
        }

        int created = 0;
        int skipped = 0;

        for (InstrumentRow row : rows) {
            if (!shouldInclude(row)) continue;
            if (row.tradingSymbol().isBlank() || row.name().isBlank()) continue;

            Exchange ex = exchangeRepository.findByCode(row.exchange().toUpperCase()).orElse(null);
            if (ex == null) {
                log.debug("Exchange {} not found, skipping {}", row.exchange(), row.tradingSymbol());
                skipped++;
                continue;
            }

            if (stockRepository.findBySymbolAndExchangeId(row.tradingSymbol(), ex.getId()).isPresent()) {
                continue;
            }

            Stock stock = new Stock();
            stock.setSymbol(row.tradingSymbol());
            stock.setName(row.name());
            stock.setExchange(ex);
            stock.setSegment(SEGMENT_CASH);
            stock.setIsActive(true);
            stockRepository.save(stock);
            created++;
        }

        log.info("Instruments sync complete: {} created, {} skipped (exchange not found)", created, skipped);
        return new SyncResult(created, 0);
    }

    private boolean shouldInclude(InstrumentRow row) {
        return SEGMENT_CASH.equalsIgnoreCase(row.segment())
                && SERIES_EQ.equalsIgnoreCase(row.series())
                && ALLOWED_EXCHANGES.contains(row.exchange().toUpperCase());
    }

    public record SyncResult(int created, int updated) {}
}
