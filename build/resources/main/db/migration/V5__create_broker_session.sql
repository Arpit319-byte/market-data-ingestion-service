CREATE TABLE broker_session(
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    data_source_id BIGINT        NOT NULL,
    broker_user_id VARCHAR(100),
    access_token   VARCHAR(2000) NOT NULL,
    refresh_token  VARCHAR(2000),
    login_time     DATETIME(6)   NOT NULL,
    expires_at     DATETIME(6),
    status         VARCHAR(30)   NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_broker_sessions_data_source
        FOREIGN KEY (data_source_id) REFERENCES data_sources (id)
) ENGINE = InnoDB;
-- supports findFirstByDataSourceAndStatusOrderByLoginTimeDesc
CREATE INDEX idx_broker_sessions_lookup
    ON broker_sessions (data_source_id, status, login_time);