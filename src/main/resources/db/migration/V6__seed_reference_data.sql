INSERT INTO exchange (name, code, country, currency, open_time, close_time,
                      timezone, is_active, created_at, updated_at)
VALUES ('National Stock Exchange of India', 'NSE', 'India', 'INR',
        '2000-01-01 09:15:00', '2000-01-01 15:30:00', 'Asia/Kolkata',
        1, NOW(6), NOW(6));

INSERT INTO data_sources (name, provider_type, api_endpoint, api_key, api_secret,
                          rate_limit_per_minute, rate_limit_per_day, timeout_seconds,
                          is_active, priority, description, created_at, updated_at)
VALUES ('Kite API', 'KITE', 'https://api.kite.trade', NULL, NULL,
        180, 200000, 30,
        1, 1, 'Zerodha Kite Connect market data provider', NOW(6), NOW(6));
