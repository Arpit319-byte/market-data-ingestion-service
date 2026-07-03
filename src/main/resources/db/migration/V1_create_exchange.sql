CREATE TABLE exchange (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    country     VARCHAR(100) NOT NULL,
    currency    VARCHAR(10)  NOT NULL,
    open_time   DATETIME(6)  NOT NULL,
    close_time  DATETIME(6)  NOT NULL,
    timezone    VARCHAR(50)  NOT NULL,
    is_active   BIT(1)       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_exchange_code UNIQUE (code)
) ENGINE = InnoDB;