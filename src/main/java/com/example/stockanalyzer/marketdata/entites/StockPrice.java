package com.example.stockanalyzer.marketdata.entites;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name="time_stamp", nullable = false)
    private Instant timestamp;

    @Column(name="open", nullable = false, precision = 19, scale = 4)
    private BigDecimal open;

    @Column(name="high", nullable = false, precision = 19, scale = 4)
    private BigDecimal high;

    @Column(name="low", nullable = false, precision = 19, scale = 4)
    private BigDecimal low;

    @Column(name="close", nullable = false, precision = 19, scale = 4)
    private BigDecimal close;

    @Column(name="volume", nullable = false)
    private Long volume;




    
}
