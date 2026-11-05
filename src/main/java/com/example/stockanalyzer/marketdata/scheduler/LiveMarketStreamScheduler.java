package com.example.stockanalyzer.marketdata.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockanalyzer.marketdata.logging.IncidentContext;
import com.example.stockanalyzer.marketdata.service.ClusterLeaderService;
import com.example.stockanalyzer.marketdata.service.LiveMarketStreamService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "kite.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LiveMarketStreamScheduler {

    private final LiveMarketStreamService liveMarketStreamService;
    private final ClusterLeaderService clusterLeaderService;

    @Scheduled(cron = "${kite.stream.start-cron:0 14 9 * * MON-FRI}", zone = "Asia/Kolkata")
    public void startStream() {
        if (clusterLeaderService.isLeader()) {
            try (IncidentContext ignored = IncidentContext.open("stream-start")) {
                liveMarketStreamService.start();
            }
        }
    }

    @Scheduled(cron = "${kite.stream.stop-cron:0 31 15 * * MON-FRI}", zone = "Asia/Kolkata")
    public void stopStream() {
        if (clusterLeaderService.isLeader()) {
            try (IncidentContext ignored = IncidentContext.open("stream-stop")) {
                liveMarketStreamService.stop();
            }
        }
    }

    @Scheduled(cron = "${kite.stream.flush-cron:5 * 9-15 * * MON-FRI}", zone = "Asia/Kolkata")
    public void flushClosedCandles() {
        if (clusterLeaderService.isLeader()) {
            try (IncidentContext ignored = IncidentContext.open("stream-flush")) {
                liveMarketStreamService.flushClosedCandles();
            }
        }
    }
}
