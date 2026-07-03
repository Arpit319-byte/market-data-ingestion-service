CREATE TABLE instrument (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    symbol                VARCHAR(50)  NOT NULL,
    exchange_id           BIGINT       NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    segment               VARCHAR(50)  NOT NULL,
    series                VARCHAR(20),
    kite_instrument_token BIGINT,
    instrument_type       VARCHAR(50)  NOT NULL,
    is_active             BIT(1)       NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_instrument_exchange
        FOREIGN KEY (exchange_id) REFERENCES exchange (id)
) ENGINE = InnoDB;
-- supports findByIsActiveTrueAndKiteInstrumentTokenIsNotNull
CREATE INDEX idx_instrument_active_token