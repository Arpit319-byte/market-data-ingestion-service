package com.example.stockanalyzer.marketdata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.IntervalType;

class CandleIntervalRollupTest {

    @Test
    void rollsFiveOneMinuteCandlesIntoOneFiveMinuteCandle() {
        Instant start = Instant.parse("2026-07-06T03:45:00Z");
        List<Candle> minutes = List.of(
                candle(start, "100", "110", "90", "105", 10),
                candle(start.plusSeconds(60), "105", "120", "100", "115", 20),
                candle(start.plusSeconds(120), "115", "116", "80", "90", 30),
                candle(start.plusSeconds(180), "90", "95", "85", "92", 40),
                candle(start.plusSeconds(240), "92", "130", "91", "125", 50));

        List<Candle> rolled = CandleIntervalRollup.rollUp(minutes, IntervalType.FIVE_MINUTE);

        assertEquals(1, rolled.size());
        Candle fiveMinute = rolled.get(0);
        assertEquals(start, fiveMinute.getCandleStart());
        assertEquals(start.plusSeconds(300), fiveMinute.getCandleEnd());
        assertEquals(new BigDecimal("100"), fiveMinute.getOpenPrice());
        assertEquals(new BigDecimal("130"), fiveMinute.getHighPrice());
        assertEquals(new BigDecimal("80"), fiveMinute.getLowPrice());
        assertEquals(new BigDecimal("125"), fiveMinute.getClosePrice());
        assertEquals(150L, fiveMinute.getVolume());
        assertEquals(IntervalType.FIVE_MINUTE, fiveMinute.getIntervalType());
    }

    private Candle candle(Instant start, String open, String high, String low, String close, long volume) {
        Candle candle = new Candle();
        candle.setCandleStart(start);
        candle.setOpenPrice(new BigDecimal(open));
        candle.setHighPrice(new BigDecimal(high));
        candle.setLowPrice(new BigDecimal(low));
        candle.setClosePrice(new BigDecimal(close));
        candle.setVolume(volume);
        return candle;
    }
}
