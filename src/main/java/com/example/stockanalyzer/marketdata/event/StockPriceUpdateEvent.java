package com.example.stockanalyzer.marketdata.event;

import java.util.List;

import com.example.stockanalyzer.marketdata.entites.StockPrice;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Published when one or more stock price records are saved.
 * Downstream services can subscribe to this event for real-time processing.
 */
@Getter
public class StockPriceUpdateEvent extends ApplicationEvent {

    private final List<StockPrice> savedPrices;

    public StockPriceUpdateEvent(Object source, List<StockPrice> savedPrices) {
        super(source);
        this.savedPrices = savedPrices != null ? List.copyOf(savedPrices) : List.of();
    }
}
