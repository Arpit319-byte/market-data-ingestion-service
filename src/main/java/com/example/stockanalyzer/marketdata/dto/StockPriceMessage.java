package com.example.stockanalyzer.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.StockPrice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for broadcasting stock price updates over WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceMessage {

    private Long id;
    private Long stockId;
    private String symbol;
    private String stockName;
    private Instant timestamp;
    private PriceInterval interval;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;

    public static StockPriceMessage from(StockPrice p) {
        return StockPriceMessage.builder()
                .id(p.getId())
                .stockId(p.getStock().getId())
                .symbol(p.getStock().getSymbol())
                .stockName(p.getStock().getName())
                .timestamp(p.getTimestamp())
                .interval(p.getInterval())
                .open(p.getOpen())
                .high(p.getHigh())
                .low(p.getLow())
                .close(p.getClose())
                .volume(p.getVolume())
                .build();
    }
}
