-- Seed NSE and BSE exchanges (idempotent - skips if already present)
-- Indian market hours: 9:15 AM - 3:30 PM IST

INSERT INTO exchange (name, code, country, currency, open_time, close_time, is_active, created_at, updated_at)
SELECT 'National Stock Exchange of India', 'NSE', 'India', 'INR',
       '2024-01-01 09:15:00+00'::timestamptz, '2024-01-01 15:30:00+00'::timestamptz,
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exchange WHERE code = 'NSE');

INSERT INTO exchange (name, code, country, currency, open_time, close_time, is_active, created_at, updated_at)
SELECT 'Bombay Stock Exchange', 'BSE', 'India', 'INR',
       '2024-01-01 09:15:00+00'::timestamptz, '2024-01-01 15:30:00+00'::timestamptz,
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM exchange WHERE code = 'BSE');