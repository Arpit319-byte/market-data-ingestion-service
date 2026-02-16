package com.example.stockanalyzer.marketdata.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.stockanalyzer.marketdata.entites.Exchange;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.repository.ExchangeRepository;
import com.example.stockanalyzer.marketdata.repository.StockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrowwInstrumentService {

    private static final Set<String> ALLOWED_EXCHANGES = Set.of("NSE", "BSE");
    private static final String SEGMENT_CASH = "CASH";
    private static final String SERIES_EQ = "EQ";

    private final ExchangeRepository exchangeRepository;
    private final StockRepository stockRepository;
    private final WebClient webClient;

    @Value("${groww.instruments.url:https://growwapi-assets.groww.in/instruments/instrument.csv}")
    private String instrumentUrl;

    /**
     * Fetches instruments CSV from Groww and syncs CASH/EQ stocks to the database.
     * HTTP fetch runs outside transaction; DB sync runs in a separate transaction.
     */
    public SyncResult fetchAndSyncInstrument() {
        log.info("Starting Groww instruments sync from {}", instrumentUrl);

        String csvBody = webClient.get()
                .uri(instrumentUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (csvBody == null || csvBody.isBlank()) {
            log.warn("Empty response from instruments URL");
            return new SyncResult(0, 0);
        }

        return parseAndSync(csvBody);
    }

    @Transactional
    public SyncResult parseAndSync(String csvBody) {
        String normalized = csvBody.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n");

        if (lines.length < 2) {
            log.warn("CSV has no data rows");
            return new SyncResult(0, 0);
        }

        String[] headers = parseCsvLine(lines[0]);
        int idxExchange = indexOf(headers, "exchange");
        int idxTradingSymbol = indexOf(headers, "trading_symbol");
        int idxName = indexOf(headers, "name");
        int idxSegment = indexOf(headers, "segment");
        int idxSeries = indexOf(headers, "series");

        if (idxExchange < 0 || idxTradingSymbol < 0 || idxName < 0 || idxSegment < 0 || idxSeries < 0) {
            log.error("Required CSV columns not found. Headers: {}", Arrays.toString(headers));
            return new SyncResult(0, 0);
        }

        int created = 0;
        int skipped = 0;

        for (int i = 1; i < lines.length; i++) {
            String[] cols = parseCsvLine(lines[i]);
            int maxIdx = Math.max(idxExchange, Math.max(idxTradingSymbol, Math.max(idxName, Math.max(idxSegment, idxSeries))));
            if (cols.length <= maxIdx) {
                continue;
            }

            String exchange = safeTrim(cols[idxExchange]);
            String tradingSymbol = safeTrim(cols[idxTradingSymbol]);
            String name = safeTrim(cols[idxName]);
            String segment = safeTrim(cols[idxSegment]);
            String series = safeTrim(cols[idxSeries]);

            if (!SEGMENT_CASH.equalsIgnoreCase(segment) || !SERIES_EQ.equalsIgnoreCase(series)) {
                continue;
            }
            if (!ALLOWED_EXCHANGES.contains(exchange.toUpperCase())) {
                continue;
            }
            if (tradingSymbol.isBlank() || name.isBlank()) {
                continue;
            }

            Exchange ex = exchangeRepository.findByCode(exchange.toUpperCase()).orElse(null);
            if (ex == null) {
                log.debug("Exchange {} not found, skipping {}", exchange, tradingSymbol);
                skipped++;
                continue;
            }

            if (stockRepository.findBySymbolAndExchangeId(tradingSymbol, ex.getId()).isPresent()) {
                continue; // already exists
            }

            Stock stock = new Stock();
            stock.setSymbol(tradingSymbol);
            stock.setName(name);
            stock.setExchange(ex);
            stock.setSegment(SEGMENT_CASH);
            stock.setIsActive(true);
            stockRepository.save(stock);
            created++;
        }

        log.info("Instruments sync complete: {} created, {} skipped (exchange not found)", created, skipped);
        return new SyncResult(created, 0);
    }

    private static String[] parseCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if ((c == ',' && !inQuotes) || c == '\r') {
                result.add(sb.toString().trim());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result.toArray(String[]::new);
    }

    private static int indexOf(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null && key.equalsIgnoreCase(arr[i].trim())) {
                return i;
            }
        }
        return -1;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    public record SyncResult(int created, int updated) {}
}
