-- Create exchange table
CREATE TABLE IF NOT EXISTS exchange (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    country VARCHAR(100) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    open_time TIMESTAMP WITH TIME ZONE NOT NULL,
    close_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create index on exchange code for faster lookups
CREATE INDEX IF NOT EXISTS idx_exchange_code ON exchange(code);

-- Create index on exchange name
CREATE INDEX IF NOT EXISTS idx_exchange_name ON exchange(name);
