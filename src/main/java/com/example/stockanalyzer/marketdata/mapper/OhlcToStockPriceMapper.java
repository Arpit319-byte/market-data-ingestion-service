package com.example.stockanalyzer.marketdata.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.dto.OhlcApiResponse;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.entites.StockPrice;

/**
 * Mapper responsible for converting OHLC API response data to StockPrice entities.
 * Single responsibility: OHLC to StockPrice conversion and timestamp parsing.
 */
@Component
public class OhlcToStockPriceMapper {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Selects the appropriate time series from the response based on interval.
     */
    public Map<String, OhlcApiResponse.TimeSeriesData> selectTimeSeries(OhlcApiResponse response, PriceInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> response.getTimeSeries1min();
            case FIVE_MINUTE -> response.getTimeSeries5min();
            case FIFTEEN_MINUTE -> response.getTimeSeries15min();
            case ONE_HOUR -> response.getTimeSeries60min();
            case ONE_DAY, ONE_WEEK, ONE_MONTH, THIRTY_MINUTE, FOUR_HOUR -> response.getTimeSeriesDaily();
        };
    }

    /**
     * Parses a timestamp string to Instant.
     */
    public Instant parseTimestamp(String key) {
        if (key == null || key.isBlank()) return Instant.now();
        try {
            return Instant.parse(key);
        } catch (Exception ignored) {}
        try {
            return LocalDate.parse(key.trim(), DATE_ONLY).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception ignored) {}
        try {
            return LocalDateTime.parse(key.trim(), DATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {}
        return Instant.now();
    }

    /**
     * Maps a single OHLC time series entry to a StockPrice entity.
     */
    public StockPrice toStockPrice(Stock stock, DataSource dataSource, Instant timestamp, PriceInterval interval,
                                   OhlcApiResponse.TimeSeriesData data) {
        StockPrice sp = new StockPrice();
        sp.setStock(stock);
        sp.setDataSource(dataSource);
        sp.setTimestamp(timestamp);
        sp.setInterval(interval);
        sp.setOpen(toBigDecimal(data.getOpen(), data.getOpenPrice()));
        sp.setHigh(toBigDecimal(data.getHigh(), data.getHighPrice()));
        sp.setLow(toBigDecimal(data.getLow(), data.getLowPrice()));
        sp.setClose(toBigDecimal(data.getClose(), data.getClosePrice()));
        sp.setVolume(toVolume(data.getVolume(), data.getVolumeValue()));
        return sp;
    }

    private static BigDecimal toBigDecimal(String s, BigDecimal fallback) {
        if (fallback != null) return fallback;
        if (s != null && !s.isBlank()) {
            try { return new BigDecimal(s.trim()); } catch (Exception ignored) {}
        }
        return BigDecimal.ZERO;
    }

    private static long toVolume(String s, Long fallback) {
        if (fallback != null) return fallback;
        if (s != null && !s.isBlank()) {
            try { return Long.parseLong(s.trim()); } catch (Exception ignored) {}
        }
        return 0L;
    }
}
