CREATE TABLE runtime_leader (
    lock_name VARCHAR(64) NOT NULL,
    owner_id VARCHAR(64) NOT NULL,
    heartbeat_at DATETIME(6) NOT NULL,
    PRIMARY KEY (lock_name)
) ENGINE=InnoDB;

CREATE TABLE background_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_type VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL,
    active_key VARCHAR(64) NULL,
    message VARCHAR(2000) NULL,
    saved_count INT NULL,
    minute_days INT NULL,
    daily_days INT NULL,
    started_at DATETIME(6) NULL,
    finished_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_background_jobs_active_key (active_key)
) ENGINE=InnoDB;
