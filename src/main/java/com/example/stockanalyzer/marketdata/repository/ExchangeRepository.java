package com.example.stockanalyzer.marketdata.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.stockanalyzer.marketdata.entites.Exchange;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByCode(String code);
    
}
