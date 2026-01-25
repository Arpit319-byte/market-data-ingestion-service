package com.example.stockanalyzer.marketdata.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_sources")
public class DataSource extends BaseModel {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "provider_type", nullable = false)
    private String providerType;

    @Column(name = "api_endpoint", nullable = false)
    private String apiEndpoint;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "rate_limit_per_day")
    private Integer rateLimitPerDay;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "description", length = 1000)
    private String description;
}
