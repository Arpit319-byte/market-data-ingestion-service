package com.example.stockanalyzer.marketdata.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.IntervalType;

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

    @Modifying(clearAutomatically = true)
    @Query("delete from Candle c where c.intervalType = :intervalType and c.candleStart < :cutoff")
    int deleteByIntervalTypeAndCandleStartBefore(
            @Param("intervalType") IntervalType intervalType,
            @Param("cutoff") Instant cutoff);
}
