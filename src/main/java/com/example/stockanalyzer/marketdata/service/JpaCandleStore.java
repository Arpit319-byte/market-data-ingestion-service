package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.IntervalType;
import com.example.stockanalyzer.marketdata.repository.CandleRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;
import com.example.stockanalyzer.marketdata.stream.CandlePublisher;
import com.example.stockanalyzer.marketdata.stream.CandleStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaCandleStore implements CandleStore {

    private final CandleRepository candleRepository;
    private final InstrumentRepository instrumentRepository;
    private final CandlePublisher candlePublisher;

    @Override
    @Transactional
    public int save(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) {
            return 0;
        }
        for (Candle candle : candles) {
            Long instrumentId = candle.getInstrument().getId();
            candle.setInstrument(instrumentRepository.getReferenceById(instrumentId));
        }

        Map<CandleGroup, List<Candle>> groups = new LinkedHashMap<>();
        for (Candle candle : candles) {
            groups.computeIfAbsent(
                    new CandleGroup(candle.getInstrument().getId(), candle.getIntervalType()),
                    ignored -> new ArrayList<>()).add(candle);
        }

        List<Candle> inserted = new ArrayList<>();
        for (Map.Entry<CandleGroup, List<Candle>> entry : groups.entrySet()) {
            inserted.addAll(insertMissing(entry.getKey(), entry.getValue()));
        }
        candlePublisher.publish(inserted);
        return inserted.size();
    }

    private List<Candle> insertMissing(CandleGroup group, List<Candle> candles) {
        Instant from = candles.stream().map(Candle::getCandleStart).min(Instant::compareTo).orElseThrow();
        Instant to = candles.stream().map(Candle::getCandleStart).max(Instant::compareTo).orElseThrow();
        Set<Instant> existingStarts = new HashSet<>();
        for (Candle existing : candleRepository.findByInstrumentIdAndIntervalTypeAndCandleStartBetweenOrderByCandleStartAsc(
                group.instrumentId(), group.intervalType(), from, to)) {
            existingStarts.add(existing.getCandleStart());
        }

        List<Candle> missing = candles.stream()
                .filter(candle -> existingStarts.add(candle.getCandleStart()))
                .toList();
        if (missing.isEmpty()) {
            return List.of();
        }
        return candleRepository.saveAll(missing);
    }

    private record CandleGroup(Long instrumentId, IntervalType intervalType) {
    }
}
