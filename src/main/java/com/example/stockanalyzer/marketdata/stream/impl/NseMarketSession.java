package com.example.stockanalyzer.marketdata.stream.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.stream.MarketSession;

@Component
public class NseMarketSession implements MarketSession {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime OPEN = LocalTime.of(9, 14);
    private static final LocalTime CLOSE = LocalTime.of(15, 31);
    private static final Set<DayOfWeek> TRADING_DAYS = EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY);

    @Override
    public ZoneId zone() {
        return ZONE;
    }

    @Override
    public boolean isOpen(ZonedDateTime now) {
        ZonedDateTime marketTime = now.withZoneSameInstant(ZONE);
        LocalTime time = marketTime.toLocalTime();
        return TRADING_DAYS.contains(marketTime.getDayOfWeek())
                && !time.isBefore(OPEN)
                && time.isBefore(CLOSE);
    }
}
