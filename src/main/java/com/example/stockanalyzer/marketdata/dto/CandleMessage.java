package com.example.stockanalyzer.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;


import com.example.stockanalyzer.marketdata.entites.IntervalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for broadcasting candle updates over WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleMessage {

    private Long id;
    private Long instrumentId;
    private String symbol;
    private String exchangeCode;
    private String instrumentName;
    private IntervalType intervalType;
    private Instant candleStart;
    private Instant candleEnd;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private Long tradeCount;
    private BigDecimal vwap;

}
