package com.example.stockanalyzer.marketdata.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockanalyzer.marketdata.entities.BackgroundJob;

public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, Long> {

    Optional<BackgroundJob> findByActiveKey(String activeKey);
}
