package com.example.stockanalyzer.marketdata.mapper;

import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.IntervalType;

public final class KiteIntervalMapper {

    private KiteIntervalMapper() {
    }

    public static String toKiteInterval(IntervalType intervalType) {
        return switch (intervalType) {
            case ONE_MINUTE -> "minute";
            case FIVE_MINUTE -> "5minute";
            case FIFTEEN_MINUTE -> "15minute";
            case THIRTY_MINUTE -> "30minute";
            case ONE_HOUR -> "60minute";
            case ONE_DAY -> "day";
        };
    }

    public static Instant candleEnd(Instant candleStart, IntervalType intervalType) {
        return candleStart.plusSeconds(intervalType.getMinutes() * 60L);
    }
}
