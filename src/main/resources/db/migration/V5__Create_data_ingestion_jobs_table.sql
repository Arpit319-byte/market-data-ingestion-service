-- Create data_ingestion_jobs table
CREATE TABLE IF NOT EXISTS data_ingestion_jobs (
    id BIGSERIAL PRIMARY KEY,
    data_source_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    job_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    records_fetched INTEGER,
    records_saved INTEGER,
    error_message VARCHAR(2000),
    retry_count INTEGER,
    interval_type VARCHAR(50),
    date_range_start TIMESTAMP WITH TIME ZONE,
    date_range_end TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_data_ingestion_jobs_data_source FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE CASCADE,
    CONSTRAINT fk_data_ingestion_jobs_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
);

-- Create index on status for filtering jobs by status
CREATE INDEX IF NOT EXISTS idx_data_ingestion_jobs_status ON data_ingestion_jobs(status);

-- Create index on data_source_id
CREATE INDEX IF NOT EXISTS idx_data_ingestion_jobs_data_source_id ON data_ingestion_jobs(data_source_id);

-- Create index on stock_id
CREATE INDEX IF NOT EXISTS idx_data_ingestion_jobs_stock_id ON data_ingestion_jobs(stock_id);

-- Create index on started_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_data_ingestion_jobs_started_at ON data_ingestion_jobs(started_at);

-- Create composite index on status and started_at for efficient job queries
CREATE INDEX IF NOT EXISTS idx_data_ingestion_jobs_status_started_at ON data_ingestion_jobs(status, started_at);
