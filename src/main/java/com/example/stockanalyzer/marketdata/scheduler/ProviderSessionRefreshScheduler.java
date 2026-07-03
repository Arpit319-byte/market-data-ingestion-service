package com.example.stockanalyzer.marketdata.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.stockanalyzer.marketdata.auth.ProviderAuthRegistry;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderSessionRefreshScheduler {

    private final DataSourceRepository dataSourceRepository;
    private final ProviderAuthRegistry providerAuthRegistry;

    @Scheduled(cron = "${provider.scheduler.session-refresh-cron:0 30 8 * * MON-FRI}", zone = "Asia/Kolkata")
    public void fetchData() {
        dataSourceRepository.findByIsActiveTrueOrderByPriorityAsc().forEach(dataSource -> {
            try {
                providerAuthRegistry.resolve(dataSource).refreshSession(dataSource);
                log.info("Refreshed session for data source {}", dataSource.getName());
            } catch (Exception e) {
                log.error("Session refresh failed for data source {}", dataSource.getName(), e);
            }
        });
    }
}
