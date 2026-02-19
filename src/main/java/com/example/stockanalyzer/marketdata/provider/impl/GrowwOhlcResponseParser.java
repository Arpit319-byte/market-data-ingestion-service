package com.example.stockanalyzer.marketdata.provider.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.stockanalyzer.marketdata.dto.GrowApiResponse;
import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;

/**
 * Utility for converting Grow API response format to standard OhlcApiResponse.
 * Stateless, no dependencies - simple class, not a Spring component.
 */
public final class GrowwOhlcResponseParser {

    private static final Pattern OHLC_PATTERN = Pattern.compile(
            "\\{open:\\s*([\\d.]+),\\s*high:\\s*([\\d.]+),\\s*low:\\s*([\\d.]+),\\s*close:\\s*([\\d.]+)\\}"
    );

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private GrowwOhlcResponseParser() {}

    /**
     * Converts Grow API response to standard OhlcApiResponse format.
     */
    public static OhlcApiResponse parse(GrowApiResponse growResponse) {
        if (!"SUCCESS".equalsIgnoreCase(growResponse.getStatus())) {
            throw new MarketDataException(
                    "Grow API returned error: " + growResponse.getError() +
                            " - " + growResponse.getMessage()
            );
        }

        if (growResponse.getPayload() == null || growResponse.getPayload().isEmpty()) {
            throw new MarketDataException("Grow API returned empty payload");
        }

        Map.Entry<String, String> entry = growResponse.getPayload().entrySet().iterator().next();
        String ohlcString = entry.getValue();
        OhlcApiResponse.TimeSeriesData data = parseOhlcString(ohlcString);

        String timestampKey = LocalDate.now(INDIA_ZONE).toString();
        Map<String, OhlcApiResponse.TimeSeriesData> timeSeries = new HashMap<>();
        timeSeries.put(timestampKey, data);

        OhlcApiResponse response = new OhlcApiResponse();
        response.setTimeSeriesDaily(timeSeries);
        return response;
    }

    /**
     * Parses OHLC string format: "{open: 149.50,high: 150.50,low: 148.50,close: 149.50}"
     */
    public static OhlcApiResponse.TimeSeriesData parseOhlcString(String ohlcString) {
        Matcher matcher = OHLC_PATTERN.matcher(ohlcString.trim());

        if (!matcher.matches()) {
            throw new MarketDataException("Failed to parse OHLC string: " + ohlcString);
        }

        OhlcApiResponse.TimeSeriesData data = new OhlcApiResponse.TimeSeriesData();
        data.setOpen(matcher.group(1));
        data.setHigh(matcher.group(2));
        data.setLow(matcher.group(3));
        data.setClose(matcher.group(4));
        data.setVolume("0");
        return data;
    }
}
