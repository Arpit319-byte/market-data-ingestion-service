package com.example.stockanalyzer.marketdata.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.IntervalType;

public final class CandleIntervalRollup {

    private CandleIntervalRollup() {
    }

    public static List<Candle> rollUp(List<Candle> oneMinuteCandles, IntervalType target) {
        long bucketSeconds = target.getMinutes() * 60L;
        Map<Instant, List<Candle>> buckets = new TreeMap<>();
        for (Candle candle : oneMinuteCandles) {
            Instant bucket = floor(candle.getCandleStart(), bucketSeconds);
            buckets.computeIfAbsent(bucket, ignored -> new ArrayList<>()).add(candle);
        }

        List<Candle> rolled = new ArrayList<>();
        for (Map.Entry<Instant, List<Candle>> entry : buckets.entrySet()) {
            List<Candle> group = entry.getValue();
            group.sort(Comparator.comparing(Candle::getCandleStart));
            Candle first = group.get(0);
            Candle last = group.get(group.size() - 1);
            Candle aggregate = new Candle();
            aggregate.setInstrument(first.getInstrument());
            aggregate.setIntervalType(target);
            aggregate.setCandleStart(entry.getKey());
            aggregate.setCandleEnd(entry.getKey().plusSeconds(bucketSeconds));
            aggregate.setOpenPrice(first.getOpenPrice());
            aggregate.setClosePrice(last.getClosePrice());
            aggregate.setHighPrice(group.stream().map(Candle::getHighPrice).max(Comparator.naturalOrder()).orElseThrow());
            aggregate.setLowPrice(group.stream().map(Candle::getLowPrice).min(Comparator.naturalOrder()).orElseThrow());
            aggregate.setVolume(group.stream().map(Candle::getVolume).reduce(0L, Long::sum));
            rolled.add(aggregate);
        }
        return rolled;
    }

    private static Instant floor(Instant instant, long bucketSeconds) {
        long epochSeconds = instant.getEpochSecond();
        return Instant.ofEpochSecond(epochSeconds - Math.floorMod(epochSeconds, bucketSeconds));
    }
}
