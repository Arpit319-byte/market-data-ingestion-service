package com.example.stockanalyzer.marketdata.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stockanalyzer.marketdata.entites.DataIngestionJob;
import com.example.stockanalyzer.marketdata.entites.DataIngestionJob.JobStatus;

public interface DataIngestionJobRepository extends JpaRepository<DataIngestionJob, Long> {

    List<DataIngestionJob> findByStockIdOrderByStartedAtDesc(Long stockId, Pageable pageable);

    List<DataIngestionJob> findByStatus(JobStatus status);

    List<DataIngestionJob> findByStockIdAndStatus(Long stockId, JobStatus status);

    List<DataIngestionJob> findByStartedAtBetweenOrderByStartedAtDesc(Instant from, Instant to);
}
