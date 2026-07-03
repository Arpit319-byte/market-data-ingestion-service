package com.example.stockanalyzer.marketdata.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.Instrument;
import com.example.stockanalyzer.marketdata.entites.IntervalType;
import com.zerodhatech.models.HistoricalData;

public final class KiteCandleMapper {

    private static final DateTimeFormatter KITE_LOCAL_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private KiteCandleMapper() {
    }

    public static Candle toCandle(
            Instrument instrument,
            IntervalType intervalType,
            HistoricalData bar,
            ZoneId exchangeZone) {

        Instant candleStart = parseTimestamp(bar.timeStamp, exchangeZone);

        Candle candle = new Candle();
        candle.setInstrument(instrument);
        candle.setIntervalType(intervalType);
        candle.setCandleStart(candleStart);
        candle.setCandleEnd(KiteIntervalMapper.candleEnd(candleStart, intervalType));
        candle.setOpenPrice(BigDecimal.valueOf(bar.open));
        candle.setHighPrice(BigDecimal.valueOf(bar.high));
        candle.setLowPrice(BigDecimal.valueOf(bar.low));
        candle.setClosePrice(BigDecimal.valueOf(bar.close));
        candle.setVolume(bar.volume);
        return candle;
    }

    private static Instant parseTimestamp(String timestamp, ZoneId exchangeZone) {
        try {
            return OffsetDateTime.parse(timestamp).toInstant();
        } catch (Exception ignored) {
            return LocalDateTime.parse(timestamp, KITE_LOCAL_TIMESTAMP)
                    .atZone(exchangeZone)
                    .toInstant();
        }
    }
}
