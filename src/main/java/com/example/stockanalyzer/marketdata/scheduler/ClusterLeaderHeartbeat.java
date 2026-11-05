package com.example.stockanalyzer.marketdata.scheduler;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.service.ClusterLeaderService;
import com.example.stockanalyzer.marketdata.service.LiveMarketStreamService;
import com.example.stockanalyzer.marketdata.stream.MarketSession;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cluster.heartbeat-enabled", havingValue = "true", matchIfMissing = true)
public class ClusterLeaderHeartbeat {

    private final ClusterLeaderService clusterLeaderService;
    private final ObjectProvider<LiveMarketStreamService> liveMarketStreamService;
    private final ObjectProvider<MarketSession> marketSession;

    @Scheduled(fixedDelayString = "${app.cluster.heartbeat-ms:20000}")
    public void beat() {
        boolean leader = clusterLeaderService.renew();
        LiveMarketStreamService stream = liveMarketStreamService.getIfAvailable();
        if (stream == null) {
            return;
        }
        if (!leader) {
            stream.stop();
            return;
        }
        MarketSession session = marketSession.getIfAvailable();
        if (session != null && session.isOpen(ZonedDateTime.now(session.zone()))) {
            stream.start();
        } else {
            stream.stop();
        }
    }
}
