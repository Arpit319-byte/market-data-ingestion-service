package com.example.stockanalyzer.marketdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockanalyzer.marketdata.entites.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    
    /**
     * Find stock by symbol
     */
    Optional<Stock> findBySymbol(String symbol);

    /**
     * Find all active stocks (for scheduled fetch)
     */
    List<Stock> findByIsActiveTrue();

    Optional<Stock> findBySymbolAndExchangeId(String symbol,Long id);
}
