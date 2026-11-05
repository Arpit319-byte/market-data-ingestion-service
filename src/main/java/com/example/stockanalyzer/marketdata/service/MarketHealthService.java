package com.example.stockanalyzer.marketdata.service;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.dto.MarketHealthResponse;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import com.example.stockanalyzer.marketdata.stream.MarketDataStreamRegistry;
import com.example.stockanalyzer.marketdata.stream.MarketSession;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketHealthService {

    static final String KITE_DATA_SOURCE_NAME = "Kite API";

    private final DataSourceRepository dataSourceRepository;
    private final BrokerSessionService brokerSessionService;
    private final ClusterLeaderService clusterLeaderService;
    private final ObjectProvider<MarketDataStreamRegistry> streamRegistry;
    private final ObjectProvider<MarketSession> marketSession;

    public MarketHealthResponse current() {
        String kiteSession = dataSourceRepository.findByNameIgnoreCase(KITE_DATA_SOURCE_NAME)
                .flatMap(brokerSessionService::findActiveSession)
                .map(session -> "active")
                .orElse("absent");
        return new MarketHealthResponse(clusterLeaderService.isLeader(), kiteSession, streamStatus());
    }

    private String streamStatus() {
        MarketDataStreamRegistry registry = streamRegistry.getIfAvailable();
        if (registry == null) {
            return "disabled";
        }
        boolean connected = registry.all().stream().anyMatch(stream -> stream.isRunning());
        if (connected) {
            return "connected";
        }
        if (!clusterLeaderService.isLeader()) {
            return "standby";
        }
        MarketSession session = marketSession.getIfAvailable();
        if (session != null && session.isOpen(ZonedDateTime.now(session.zone()))) {
            return "stopped";
        }
        return "idle";
    }
}
