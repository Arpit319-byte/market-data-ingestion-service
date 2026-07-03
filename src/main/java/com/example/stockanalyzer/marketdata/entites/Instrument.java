package com.example.stockanalyzer.marketdata.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Instrument extends BaseModel{

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "segment", nullable = false, length = 50)
    private String segment;

    @Column(name = "series", length = 20)
    private String series;

    @Column(name = "kite_instrument_token")
    private Long kiteInstrumentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, length = 50)
    private InstrumentType instrumentType;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

}
