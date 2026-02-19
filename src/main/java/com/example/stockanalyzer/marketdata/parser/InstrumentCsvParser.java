package com.example.stockanalyzer.marketdata.parser;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.dto.InstrumentRow;

/**
 * Parser responsible for parsing instruments CSV content to domain objects.
 * Single responsibility: CSV parsing only.
 */
@Component
public class InstrumentCsvParser {

    private static final String HEADER_EXCHANGE = "exchange";
    private static final String HEADER_TRADING_SYMBOL = "trading_symbol";
    private static final String HEADER_NAME = "name";
    private static final String HEADER_SEGMENT = "segment";
    private static final String HEADER_SERIES = "series";

    /**
     * Parses CSV content and returns list of instrument rows.
     * Returns empty list if headers are invalid or data is missing.
     */
    public List<InstrumentRow> parse(String csvBody) {
        if (csvBody == null || csvBody.isBlank()) {
            return List.of();
        }
        String normalized = csvBody.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n");

        if (lines.length < 2) {
            return List.of();
        }

        String[] headers = parseCsvLine(lines[0]);
        int idxExchange = indexOf(headers, HEADER_EXCHANGE);
        int idxTradingSymbol = indexOf(headers, HEADER_TRADING_SYMBOL);
        int idxName = indexOf(headers, HEADER_NAME);
        int idxSegment = indexOf(headers, HEADER_SEGMENT);
        int idxSeries = indexOf(headers, HEADER_SERIES);

        if (idxExchange < 0 || idxTradingSymbol < 0 || idxName < 0 || idxSegment < 0 || idxSeries < 0) {
            return List.of();
        }

        List<InstrumentRow> rows = new ArrayList<>();
        int maxIdx = Math.max(idxExchange, Math.max(idxTradingSymbol, Math.max(idxName, Math.max(idxSegment, idxSeries))));

        for (int i = 1; i < lines.length; i++) {
            String[] cols = parseCsvLine(lines[i]);
            if (cols.length <= maxIdx) continue;

            rows.add(new InstrumentRow(
                    safeTrim(cols[idxExchange]),
                    safeTrim(cols[idxTradingSymbol]),
                    safeTrim(cols[idxName]),
                    safeTrim(cols[idxSegment]),
                    safeTrim(cols[idxSeries])
            ));
        }
        return rows;
    }

    private static String[] parseCsvLine(String line) {
        if (line == null) return new String[0];
        List<String> result = new ArrayList<>();
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
}
