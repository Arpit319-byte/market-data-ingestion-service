package com.example.stockanalyzer.marketdata.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "background_jobs", uniqueConstraints = @UniqueConstraint(
        name = "uk_background_jobs_active_key",
        columnNames = "active_key"))
public class BackgroundJob extends BaseModel {

    @Column(name = "job_type", nullable = false, length = 64)
    private String jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private JobStatus status;

    @Column(name = "active_key", length = 64)
    private String activeKey;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "saved_count")
    private Integer savedCount;

    @Column(name = "minute_days")
    private Integer minuteDays;

    @Column(name = "daily_days")
    private Integer dailyDays;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}
