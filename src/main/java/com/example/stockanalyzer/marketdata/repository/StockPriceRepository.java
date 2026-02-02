package com.example.stockanalyzer.marketdata.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.StockPrice;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    
    /**
     * Check if a stock price record already exists
     */
    @Query("SELECT COUNT(sp) > 0 FROM StockPrice sp " +
           "WHERE sp.stock.id = :stockId " +
           "AND sp.timestamp = :timestamp " +
           "AND sp.interval = :interval")
    boolean existsByStockIdAndTimestampAndInterval(
            @Param("stockId") Long stockId,
            @Param("timestamp") Instant timestamp,
            @Param("interval") PriceInterval interval
    );
}
