package com.example.stockanalyzer.marketdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entites.Exchange;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
}
