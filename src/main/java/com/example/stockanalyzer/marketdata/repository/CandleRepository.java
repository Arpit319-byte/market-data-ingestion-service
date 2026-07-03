package com.example.stockanalyzer.marketdata.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.IntervalType;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    Optional<Candle> findByInstrumentIdAndIntervalTypeAndCandleStart(
            Long instrumentId,
            IntervalType intervalType,
            Instant candleStart);

    List<Candle> findByInstrumentIdAndIntervalTypeAndCandleStartBetweenOrderByCandleStartAsc(
            Long instrumentId,
            IntervalType intervalType,
            Instant from,
            Instant to);
}
