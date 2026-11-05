package com.example.stockanalyzer.marketdata.stream.impl;

import java.time.ZonedDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.service.ClusterLeaderService;
import com.example.stockanalyzer.marketdata.service.LiveMarketStreamService;
import com.example.stockanalyzer.marketdata.stream.MarketSession;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LiveStreamStartup implements ApplicationRunner {

    private final LiveMarketStreamService liveMarketStreamService;
    private final MarketSession marketSession;
    private final ClusterLeaderService clusterLeaderService;

    @Override
    public void run(ApplicationArguments args) {
        if (clusterLeaderService.renew()
                && marketSession.isOpen(ZonedDateTime.now(marketSession.zone()))) {
            liveMarketStreamService.start();
        }
    }
}
