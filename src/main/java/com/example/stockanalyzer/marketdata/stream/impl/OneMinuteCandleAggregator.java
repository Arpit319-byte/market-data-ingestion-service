package com.example.stockanalyzer.marketdata.stream.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.IntervalType;
import com.example.stockanalyzer.marketdata.stream.CandleStore;
import com.example.stockanalyzer.marketdata.stream.ClosedCandleFlusher;
import com.example.stockanalyzer.marketdata.stream.TradeTick;
import com.example.stockanalyzer.marketdata.stream.TradeTickListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OneMinuteCandleAggregator implements TradeTickListener, ClosedCandleFlusher {

    private static final IntervalType LIVE_INTERVAL = IntervalType.ONE_MINUTE;

    private final CandleStore candleStore;
    private final Object lock = new Object();
    private final Map<Long, MinuteBuilder> openCandles = new HashMap<>();
    private final Map<Long, Long> cumulativeVolume = new HashMap<>();

    @Override
    public void onTicks(List<TradeTick> ticks) {
        List<Candle> closed = new ArrayList<>();
        synchronized (lock) {
            for (TradeTick tick : ticks) {
                Candle completed = apply(tick);
                if (completed != null) {
                    closed.add(completed);
                }
            }
        }
        persist(closed);
    }

    @Override
    public void flushClosed(Instant now) {
        Instant currentBucket = floorToMinute(now);
        List<Candle> closed = new ArrayList<>();
        synchronized (lock) {
            openCandles.entrySet().removeIf(entry -> {
                if (entry.getValue().bucket.isBefore(currentBucket)) {
                    closed.add(entry.getValue().toCandle());
                    return true;
                }
                return false;
            });
        }
        persist(closed);
    }

    @Override
    public void flushAll() {
        List<Candle> closed = new ArrayList<>();
        synchronized (lock) {
            for (MinuteBuilder builder : openCandles.values()) {
                closed.add(builder.toCandle());
            }
            openCandles.clear();
        }
        persist(closed);
    }

    private Candle apply(TradeTick tick) {
        if (tick.lastPrice() == null || tick.lastPrice().signum() <= 0) {
            return null;
        }

        long instrumentId = tick.instrument().getId();
        Instant bucket = floorToMinute(tick.timestamp());
        long tradedToday = Math.max(0L, tick.volumeTradedToday());
        long previous = cumulativeVolume.getOrDefault(instrumentId, tradedToday);
        long volumeDelta = Math.max(0L, tradedToday - previous);
        cumulativeVolume.put(instrumentId, tradedToday);

        MinuteBuilder builder = openCandles.get(instrumentId);
        Candle completed = null;
        if (builder != null && !builder.bucket.equals(bucket)) {
            completed = builder.toCandle();
            builder = null;
        }
        if (builder == null) {
            builder = new MinuteBuilder(tick.instrument(), bucket);
            openCandles.put(instrumentId, builder);
        }
        builder.apply(tick.lastPrice(), volumeDelta);
        return completed;
    }

    private void persist(List<Candle> candles) {
        if (candles.isEmpty()) {
            return;
        }
        try {
            int saved = candleStore.save(candles);
            log.info("Saved {} live 1-minute candles", saved);
        } catch (RuntimeException e) {
            log.error("Failed to save {} live candles", candles.size(), e);
        }
    }

    private static Instant floorToMinute(Instant instant) {
        long epochSeconds = instant.getEpochSecond();
        return Instant.ofEpochSecond(epochSeconds - Math.floorMod(epochSeconds, 60));
    }

    private static final class MinuteBuilder {
        private final Instrument instrument;
        private final Instant bucket;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private long volume;

        private MinuteBuilder(Instrument instrument, Instant bucket) {
            this.instrument = instrument;
            this.bucket = bucket;
        }

        private void apply(BigDecimal price, long volumeDelta) {
            if (open == null) {
                open = price;
                high = price;
                low = price;
            } else {
                if (price.compareTo(high) > 0) {
                    high = price;
                }
                if (price.compareTo(low) < 0) {
                    low = price;
                }
            }
            close = price;
            volume += volumeDelta;
        }

        private Candle toCandle() {
            Candle candle = new Candle();
            candle.setInstrument(instrument);
            candle.setIntervalType(LIVE_INTERVAL);
            candle.setCandleStart(bucket);
            candle.setCandleEnd(bucket.plusSeconds(LIVE_INTERVAL.getMinutes() * 60L));
            candle.setOpenPrice(open);
            candle.setHighPrice(high);
            candle.setLowPrice(low);
            candle.setClosePrice(close);
            candle.setVolume(volume);
            return candle;
        }
    }
}
