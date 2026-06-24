package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.Instrument;
import com.example.stockanalyzer.marketdata.entites.InstrumentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API response for {@link Instrument} — avoids exposing JPA entities directly.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InstrumentResponse{
      
    private Long id;
    private String symbol;
    private String name;
    private Long exchangeId;
    private String exchangeCode;
    private String segment;
    private String series;
    private InstrumentType instrumentType;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
