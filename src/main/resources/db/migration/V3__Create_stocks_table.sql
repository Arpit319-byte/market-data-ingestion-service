-- Create stocks table
CREATE TABLE IF NOT EXISTS stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    exchange_id BIGINT NOT NULL,
    isactive BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_stocks_exchange FOREIGN KEY (exchange_id) REFERENCES exchange(id)
);

-- Create unique index on symbol and exchange_id combination
CREATE UNIQUE INDEX IF NOT EXISTS idx_stocks_symbol_exchange ON stocks(symbol, exchange_id);

-- Create index on symbol for faster lookups
CREATE INDEX IF NOT EXISTS idx_stocks_symbol ON stocks(symbol);

-- Create index on exchange_id for foreign key lookups
CREATE INDEX IF NOT EXISTS idx_stocks_exchange_id ON stocks(exchange_id);

-- Create index on isactive for filtering active stocks
CREATE INDEX IF NOT EXISTS idx_stocks_isactive ON stocks(isactive);
