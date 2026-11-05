package com.example.stockanalyzer.marketdata.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockanalyzer.marketdata.dto.BackgroundJobResponse;
import com.example.stockanalyzer.marketdata.entities.BackgroundJob;
import com.example.stockanalyzer.marketdata.entities.JobStatus;
import com.example.stockanalyzer.marketdata.exception.ConflictException;
import com.example.stockanalyzer.marketdata.exception.MarketDataException;
import com.example.stockanalyzer.marketdata.exception.NotFoundException;
import com.example.stockanalyzer.marketdata.repository.BackgroundJobRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CandleBackfillJobService {

    static final String JOB_TYPE = "CANDLE_BACKFILL";
    private static final int MAX_MINUTE_DAYS = 400;
    private static final int MAX_DAILY_DAYS = 3650;

    private final BackgroundJobRepository backgroundJobRepository;
    private final ObjectProvider<CandleBackfillRunner> candleBackfillRunner;

    public CandleBackfillJobService(
            BackgroundJobRepository backgroundJobRepository,
            ObjectProvider<CandleBackfillRunner> candleBackfillRunner) {
        this.backgroundJobRepository = backgroundJobRepository;
        this.candleBackfillRunner = candleBackfillRunner;
    }

    public BackgroundJobResponse start(int minuteDays, int dailyDays) {
        BackgroundJob job = enqueue(minuteDays, dailyDays);
        candleBackfillRunner.getObject().run(job.getId());
        return toResponse(job);
    }

    @Transactional
    public BackgroundJob enqueue(int minuteDays, int dailyDays) {
        if (minuteDays < 1 || minuteDays > MAX_MINUTE_DAYS) {
            throw new MarketDataException("minuteDays must be between 1 and " + MAX_MINUTE_DAYS);
        }
        if (dailyDays < 1 || dailyDays > MAX_DAILY_DAYS) {
            throw new MarketDataException("dailyDays must be between 1 and " + MAX_DAILY_DAYS);
        }
        releaseStaleJob();
        BackgroundJob job = new BackgroundJob();
        job.setJobType(JOB_TYPE);
        job.setStatus(JobStatus.QUEUED);
        job.setActiveKey(JOB_TYPE);
        job.setMinuteDays(minuteDays);
        job.setDailyDays(dailyDays);
        try {
            return backgroundJobRepository.saveAndFlush(job);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("A candle backfill is already running");
        }
    }

    @Transactional(readOnly = true)
    public BackgroundJobResponse get(Long id) {
        return backgroundJobRepository.findById(id)
                .map(CandleBackfillJobService::toResponse)
                .orElseThrow(() -> new NotFoundException("Background job not found: " + id));
    }

    @Transactional
    public BackgroundJob markRunning(Long id) {
        BackgroundJob job = required(id);
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        return backgroundJobRepository.save(job);
    }

    @Transactional
    public void markSucceeded(Long id, int savedCount) {
        BackgroundJob job = required(id);
        job.setStatus(JobStatus.SUCCEEDED);
        job.setSavedCount(savedCount);
        job.setActiveKey(null);
        job.setFinishedAt(Instant.now());
        job.setMessage("Saved " + savedCount + " candles");
        backgroundJobRepository.save(job);
    }

    @Transactional
    public void markFailed(Long id, String message) {
        backgroundJobRepository.findById(id).ifPresent(job -> {
            job.setStatus(JobStatus.FAILED);
            job.setActiveKey(null);
            job.setFinishedAt(Instant.now());
            job.setMessage(trim(message));
            backgroundJobRepository.save(job);
        });
    }

    private void releaseStaleJob() {
        backgroundJobRepository.findByActiveKey(JOB_TYPE).ifPresent(existing -> {
            Instant updatedAt = existing.getUpdatedAt() == null ? Instant.EPOCH : existing.getUpdatedAt();
            long staleHours = existing.getStatus() == JobStatus.QUEUED ? 1 : 6;
            if (updatedAt.isBefore(Instant.now().minus(staleHours, ChronoUnit.HOURS))) {
                existing.setStatus(JobStatus.FAILED);
                existing.setActiveKey(null);
                existing.setFinishedAt(Instant.now());
                existing.setMessage("Released after this instance stopped updating the job");
                backgroundJobRepository.saveAndFlush(existing);
                log.warn("Released stale backfill job {}", existing.getId());
            }
        });
    }

    private BackgroundJob required(Long id) {
        return backgroundJobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Background job not found: " + id));
    }

    private static BackgroundJobResponse toResponse(BackgroundJob job) {
        return new BackgroundJobResponse(
                job.getId(),
                job.getJobType(),
                job.getStatus().name(),
                job.getMessage(),
                job.getSavedCount(),
                job.getStartedAt(),
                job.getFinishedAt());
    }

    private static String trim(String message) {
        if (message == null || message.isBlank()) {
            return "Candle backfill failed";
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
