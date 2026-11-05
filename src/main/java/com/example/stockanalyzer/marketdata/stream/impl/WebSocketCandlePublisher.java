package com.example.stockanalyzer.marketdata.stream.impl;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.dto.CandleMessage;
import com.example.stockanalyzer.marketdata.entities.Candle;
import com.example.stockanalyzer.marketdata.entities.Instrument;
import com.example.stockanalyzer.marketdata.stream.CandlePublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketCandlePublisher implements CandlePublisher {

    static final String TOPIC = "/topic/candles";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(List<Candle> candles) {
        for (Candle candle : candles) {
            messagingTemplate.convertAndSend(TOPIC, toMessage(candle));
        }
    }

    private CandleMessage toMessage(Candle candle) {
        Instrument instrument = candle.getInstrument();
        String exchangeCode = instrument.getExchange() == null ? null : instrument.getExchange().getCode();
        return CandleMessage.builder()
                .id(candle.getId())
                .instrumentId(instrument.getId())
                .symbol(instrument.getSymbol())
                .exchangeCode(exchangeCode)
                .instrumentName(instrument.getName())
                .intervalType(candle.getIntervalType())
                .candleStart(candle.getCandleStart())
                .candleEnd(candle.getCandleEnd())
                .open(candle.getOpenPrice())
                .high(candle.getHighPrice())
                .low(candle.getLowPrice())
                .close(candle.getClosePrice())
                .volume(candle.getVolume())
                .tradeCount(candle.getTradeCount())
                .vwap(candle.getVwap())
                .build();
    }
}
