package com.example.stockanalyzer.marketdata.dto;

import java.time.Instant;

import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.ProviderType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API response for {@link DataSource}. Does not expose {@code apiKey}.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceResponse{

        private Long id;
        private String name;
        private ProviderType providerType;
        private String apiEndpoint;
        private Integer rateLimitPerMinute;
        private Integer rateLimitPerDay;
        private Integer timeoutSeconds;
        private boolean active;
        private Integer priority;
        private String description;
        private Instant createdAt;
        private Instant updatedAt;

}
