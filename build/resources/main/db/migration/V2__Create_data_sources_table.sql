-- Create data_sources table
CREATE TABLE IF NOT EXISTS data_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    provider_type VARCHAR(100) NOT NULL,
    api_endpoint VARCHAR(500) NOT NULL,
    api_key VARCHAR(500),
    rate_limit_per_minute INTEGER,
    rate_limit_per_day INTEGER,
    timeout_seconds INTEGER,
    is_active BOOLEAN NOT NULL,
    priority INTEGER,
    description VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create index on data source name
CREATE INDEX IF NOT EXISTS idx_data_sources_name ON data_sources(name);

-- Create index on is_active for filtering active sources
CREATE INDEX IF NOT EXISTS idx_data_sources_is_active ON data_sources(is_active);
