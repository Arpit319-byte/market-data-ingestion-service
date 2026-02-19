package com.example.stockanalyzer.marketdata.dto;

/**
 * DTO representing a single row from the instruments CSV.
 */
public record InstrumentRow(String exchange, String tradingSymbol, String name, String segment, String series) {}
