package com.example.stockanalyzer.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.example.stockanalyzer.marketdata.entites.Candle;
import com.example.stockanalyzer.marketdata.entites.IntervalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API response for {@link Candle} OHLC data.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleResponse{

    private Long id;
    private Long instrumentId;
    private String symbol;
    private String exchangeCode;
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
    private Instant createdAt;
    private Instant updatedAt;
        

}
