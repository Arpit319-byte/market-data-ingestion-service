package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import com.example.stockanalyzer.marketdata.entites.Exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API response for {@link Exchange}.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeResponse{

    private Long id;
    private String name;
    private String code;
    private String country;
    private String currency;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String timezone;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
        
}
