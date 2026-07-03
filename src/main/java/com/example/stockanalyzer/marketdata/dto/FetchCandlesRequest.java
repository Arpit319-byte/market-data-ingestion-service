package com.example.stockanalyzer.marketdata.dto;
import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.IntervalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request parameters for fetching and persisting OHLC candles from an external provider.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FetchCandlesRequest{

    private Long instrumentId;
    private Long dataSourceId;
    private IntervalType intervalType;
    private Instant from;
    private Instant to;
      
}
