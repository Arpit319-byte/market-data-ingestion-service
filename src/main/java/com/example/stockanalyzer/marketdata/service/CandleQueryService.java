package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.CandleQueryRequest;
import com.example.stockanalyzer.marketdata.dto.CandleResponse;
import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.entities.IntervalType;
import com.example.stockanalyzer.marketdata.exception.NotFoundException;
import com.example.stockanalyzer.marketdata.repository.CandleRepository;
import com.example.stockanalyzer.marketdata.repository.InstrumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandleQueryService {

    private final CandleRepository candleRepository;
    private final InstrumentRepository instrumentRepository;

    @Transactional(readOnly = true)
    public List<CandleResponse> query(Long instrumentId, CandleQueryRequest request) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found: " + instrumentId));
        IntervalType intervalType = request.getIntervalType() == null ? IntervalType.ONE_MINUTE : request.getIntervalType();
        Instant to = request.getTo() == null ? Instant.now() : request.getTo();
        Instant from = request.getFrom() == null ? to.minus(1, ChronoUnit.DAYS) : request.getFrom();

        List<Candle> candles = switch (intervalType) {
            case ONE_MINUTE, ONE_DAY -> candleRepository
                    .findByInstrumentIdAndIntervalTypeAndCandleStartBetweenOrderByCandleStartAsc(
                            instrumentId, intervalType, from, to);
            case FIVE_MINUTE, FIFTEEN_MINUTE, THIRTY_MINUTE, ONE_HOUR -> CandleIntervalRollup.rollUp(
                    candleRepository.findByInstrumentIdAndIntervalTypeAndCandleStartBetweenOrderByCandleStartAsc(
                            instrumentId, IntervalType.ONE_MINUTE, from, to),
                    intervalType);
        };
        return candles.stream().map(candle -> toResponse(candle, instrument)).toList();
    }

    private CandleResponse toResponse(Candle candle, Instrument instrument) {
        String exchangeCode = instrument.getExchange() == null ? null : instrument.getExchange().getCode();
        return new CandleResponse(
                candle.getId(),
                instrument.getId(),
                instrument.getSymbol(),
                exchangeCode,
                candle.getIntervalType(),
                candle.getCandleStart(),
                candle.getCandleEnd(),
                candle.getOpenPrice(),
                candle.getHighPrice(),
                candle.getLowPrice(),
                candle.getClosePrice(),
                candle.getVolume(),
                candle.getTradeCount(),
                candle.getVwap(),
                candle.getCreatedAt(),
                candle.getUpdatedAt());
    }
}
