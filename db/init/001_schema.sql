
-- Tables for database

CREATE TABLE IF NOT EXISTS country (
  code CHAR(2) PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
  code TEXT PRIMARY KEY,         -- e.g., 'electronics'
  name TEXT NOT NULL,            -- e.g., 'Electronics'
  base_price NUMERIC(10,2) NOT NULL  -- price per unit your UI uses
);

-- Default tariff by product
CREATE TABLE IF NOT EXISTS product_tariff_default (
  product_code TEXT PRIMARY KEY REFERENCES product(code) ON DELETE CASCADE,
  rate_percent NUMERIC(6,3) NOT NULL CHECK (rate_percent >= 0)
);

-- Optional route-specific override (origin+dest); if present, use this over default
CREATE TABLE IF NOT EXISTS route_tariff_override (
  id BIGSERIAL PRIMARY KEY,
  product_code TEXT NOT NULL REFERENCES product(code) ON DELETE CASCADE,
  origin_country CHAR(2) NOT NULL REFERENCES country(code),
  dest_country   CHAR(2) NOT NULL REFERENCES country(code),
  rate_percent NUMERIC(6,3) NOT NULL CHECK (rate_percent >= 0),
  UNIQUE (product_code, origin_country, dest_country)
);

-- fees (fixed)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'fee_type') THEN
    CREATE TYPE fee_type AS ENUM ('handling','inspection','processing','others');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS fee_schedule (
  fee fee_type PRIMARY KEY,
  amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0)
);

-- Convenience view (useful for reads in backend)
CREATE OR REPLACE VIEW v_product_defaults AS
SELECT
  p.code AS product_code,
  p.name AS product_name,
  p.base_price,
  t.rate_percent AS default_tariff_percent
FROM product p
LEFT JOIN product_tariff_default t ON t.product_code = p.code;

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

