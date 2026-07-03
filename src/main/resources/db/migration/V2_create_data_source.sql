CREATE TABLE data_sources (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    name                  VARCHAR(255)  NOT NULL,
    provider_type         VARCHAR(30)   NOT NULL,
    api_endpoint          VARCHAR(255)  NOT NULL,
    api_key               VARCHAR(255),
    api_secret            VARCHAR(255),
    rate_limit_per_minute INT,
    rate_limit_per_day    INT,
    timeout_seconds       INT,
    is_active             BIT(1)        NOT NULL,
    priority              INT,
    description           VARCHAR(1000),
    created_at            DATETIME(6)   NOT NULL,
    updated_at            DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_data_sources_name UNIQUE (name)
) ENGINE = InnoDB;
-- supports findByIsActiveTrueOrderByPriorityAsc
CREATE INDEX idx_data_sources_active_priority
    ON data_sources (is_active, priority);