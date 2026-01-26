-- Create stock_prices table
CREATE TABLE IF NOT EXISTS stock_prices (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    data_source_id BIGINT,
    time_stamp TIMESTAMP WITH TIME ZONE NOT NULL,
    interval VARCHAR(50) NOT NULL,
    open_price DECIMAL(19, 4) NOT NULL,
    high_price DECIMAL(19, 4) NOT NULL,
    low_price DECIMAL(19, 4) NOT NULL,
    close_price DECIMAL(19, 4) NOT NULL,
    total_volume BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_stock_prices_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_prices_data_source FOREIGN KEY (data_source_id) REFERENCES data_sources(id) ON DELETE SET NULL
);

-- Create composite index on stock_id and time_stamp for efficient time-series queries
CREATE UNIQUE INDEX IF NOT EXISTS idx_stock_prices_stock_timestamp_interval ON stock_prices(stock_id, time_stamp, interval);

-- Create index on stock_id for foreign key lookups
CREATE INDEX IF NOT EXISTS idx_stock_prices_stock_id ON stock_prices(stock_id);

-- Create index on time_stamp for time-based queries
CREATE INDEX IF NOT EXISTS idx_stock_prices_time_stamp ON stock_prices(time_stamp);

-- Create index on data_source_id
CREATE INDEX IF NOT EXISTS idx_stock_prices_data_source_id ON stock_prices(data_source_id);

-- Create index on interval for filtering by interval type
CREATE INDEX IF NOT EXISTS idx_stock_prices_interval ON stock_prices(interval);
