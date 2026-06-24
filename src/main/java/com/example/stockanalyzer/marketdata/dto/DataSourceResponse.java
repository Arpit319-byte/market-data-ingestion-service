package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.DataSource;

/**
 * API response for {@link DataSource}. Does not expose {@code apiKey}.
 */
public record DataSourceResponse(
        Long id,
        String name,
        String providerType,
        String apiEndpoint,
        Integer rateLimitPerMinute,
        Integer rateLimitPerDay,
        Integer timeoutSeconds,
        boolean active,
        Integer priority,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static DataSourceResponse from(DataSource dataSource) {
        return new DataSourceResponse(
                dataSource.getId(),
                dataSource.getName(),
                dataSource.getProviderType(),
                dataSource.getApiEndpoint(),
                dataSource.getRateLimitPerMinute(),
                dataSource.getRateLimitPerDay(),
                dataSource.getTimeoutSeconds(),
                Boolean.TRUE.equals(dataSource.getIsActive()),
                dataSource.getPriority(),
                dataSource.getDescription(),
                dataSource.getCreatedAt(),
                dataSource.getUpdatedAt()
        );
    }
}
