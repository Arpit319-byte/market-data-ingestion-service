package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.stockanalyzer.marketdata.entites.DataIngestionJob;
import com.example.stockanalyzer.marketdata.entites.DataIngestionJob.JobStatus;
import com.example.stockanalyzer.marketdata.entites.DataSource;
import com.example.stockanalyzer.marketdata.entites.PriceInterval;
import com.example.stockanalyzer.marketdata.entites.Stock;
import com.example.stockanalyzer.marketdata.repository.DataIngestionJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the lifecycle of data ingestion jobs.
 * Single responsibility: create, complete, and fail ingestion jobs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionJobService {

    private static final String JOB_TYPE_FETCH_OHLC = "FETCH_OHLC";
    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    private final DataIngestionJobRepository dataIngestionJobRepository;

    /**
     * Creates and persists a new running job for OHLC fetch.
     */
    public DataIngestionJob createOhlcFetchJob(DataSource dataSource, Stock stock, PriceInterval interval) {
        DataIngestionJob job = new DataIngestionJob();
        job.setDataSource(dataSource);
        job.setStock(stock);
        job.setJobType(JOB_TYPE_FETCH_OHLC);
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        job.setIntervalType(interval);
        job.setRetryCount(0);
        return dataIngestionJobRepository.save(job);
    }

    /**
     * Marks a job as completed with the given metrics.
     */
    public void completeJob(DataIngestionJob job, int recordsFetched, int recordsSaved, String errorMessage) {
        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        job.setRecordsFetched(recordsFetched);
        job.setRecordsSaved(recordsSaved);
        job.setErrorMessage(errorMessage);
        dataIngestionJobRepository.save(job);
    }

    /**
     * Marks a job as failed with the given error message.
     */
    public void failJob(DataIngestionJob job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setCompletedAt(Instant.now());
        String truncated = errorMessage != null && errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH
                ? errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH)
                : errorMessage;
        job.setErrorMessage(truncated);
        dataIngestionJobRepository.save(job);
    }
}
