package com.example.stockanalyzer.marketdata.entites;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange")
public class Exchange extends BaseModel {


    @Column(name="name", nullable = false)
    private String name;

    @Column(name="code", nullable = false)
    private String code;

    @Column(name="country", nullable = false)
    private String country;

    @Column(name="currency", nullable = false)
    private String currency;

    @Column(name="open_time", nullable = false)
    private Instant openTime;

    @Column(name="close_time", nullable = false)
    private Instant closeTime;

    @Column(name="is_active", nullable = false)
    private Boolean isActive;
}
