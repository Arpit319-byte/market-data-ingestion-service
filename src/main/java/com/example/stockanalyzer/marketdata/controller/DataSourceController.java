package com.example.stockanalyzer.marketdata.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockanalyzer.marketdata.dto.DataSourceResponse;
import com.example.stockanalyzer.marketdata.entities.DataSource;
import com.example.stockanalyzer.marketdata.repository.DataSourceRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/data-sources")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceRepository dataSourceRepository;

    @GetMapping
    public List<DataSourceResponse> list() {
        return dataSourceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private DataSourceResponse toResponse(DataSource dataSource) {
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
                dataSource.getUpdatedAt());
    }
}
