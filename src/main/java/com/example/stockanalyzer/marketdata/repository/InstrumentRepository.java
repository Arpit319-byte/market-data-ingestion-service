package com.example.stockanalyzer.marketdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockanalyzer.marketdata.entites.Instrument;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbolAndExchangeId(String symbol, Long exchangeId);

    List<Instrument> findByIsActiveTrue();

    List<Instrument> findByIsActiveTrueAndKiteInstrumentTokenIsNotNull();
}
