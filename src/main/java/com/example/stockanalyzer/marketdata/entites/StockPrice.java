package com.example.stockanalyzer.marketdata.entites;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "stock_prices")
public class StockPrice extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "data_source_id")
    private DataSource dataSource;

    @Column(name="time_stamp", nullable = false)
    private Instant timestamp;

    @Column(name="interval", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceInterval interval;

    @Column(name="open_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal open;

    @Column(name="high_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal high;

    @Column(name="low_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal low;

    @Column(name="close_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal close;

    @Column(name="total_volume", nullable = false)
    private Long volume;




    
}
