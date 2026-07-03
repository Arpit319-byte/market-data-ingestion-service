package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;
import com.example.stockanalyzer.marketdata.entites.IntervalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Query parameters for reading stored candles for an instrument.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleQueryRequest{
        
    private Instant from;
    private Instant to;
    private IntervalType intervalType;

}
        
