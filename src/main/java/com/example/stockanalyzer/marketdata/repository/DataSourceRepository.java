package com.example.stockanalyzer.marketdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockanalyzer.marketdata.entites.DataSource;

public interface DataSourceRepository extends JpaRepository<DataSource, Long> {

    /**
     * Find data source by name (e.g. "Grow API")
     */
    Optional<DataSource> findByNameIgnoreCase(String name);

    /**
     * Find active data sources ordered by priority (ascending - lower number = higher priority)
     */
    List<DataSource> findByIsActiveTrueOrderByPriorityAsc();
}
