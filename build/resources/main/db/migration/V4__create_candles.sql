CREATE TABLE candles (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    instrument_id BIGINT        NOT NULL,
    interval_type VARCHAR(20)   NOT NULL,
    candle_start  DATETIME(6)   NOT NULL,
    candle_end    DATETIME(6)   NOT NULL,
    open_price    DECIMAL(19,4) NOT NULL,
    high_price    DECIMAL(19,4) NOT NULL,
    low_price     DECIMAL(19,4) NOT NULL,
    close_price   DECIMAL(19,4) NOT NULL,
    volume        BIGINT        NOT NULL,
    trade_count   BIGINT,
    vwap          DECIMAL(19,4),
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_candles_instrument
        FOREIGN KEY (instrument_id) REFERENCES instrument (id),
    CONSTRAINT uk_candles_instrument_interval_start
        UNIQUE (instrument_id, interval_type, candle_start)
) ENGINE = InnoDB;