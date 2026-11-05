package com.example.stockanalyzer.marketdata.stream;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataStreamRegistry {

    private final List<MarketDataStream> streams;

    public boolean supports(DataSource dataSource) {
        return streams.stream().anyMatch(stream -> stream.supports(dataSource));
    }

    public MarketDataStream resolve(DataSource dataSource) {
        return streams.stream()
                .filter(stream -> stream.supports(dataSource))
                .findFirst()
                .orElseThrow(() -> new MarketDataException(
                        "No live market data stream found for data source: " + dataSource.getName()));
    }

    public List<MarketDataStream> all() {
        return List.copyOf(streams);
    }
}
