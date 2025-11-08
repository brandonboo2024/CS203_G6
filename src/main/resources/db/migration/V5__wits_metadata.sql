CREATE TABLE IF NOT EXISTS wits_country_metadata (
    country_code     VARCHAR(10) PRIMARY KEY,
    iso3             VARCHAR(3),
    country_name     TEXT,
    long_name        TEXT,
    income_group     TEXT,
    lending_category TEXT,
    region           TEXT,
    currency_unit    TEXT,
    is_group         BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wits_country_metadata_iso3
    ON wits_country_metadata (iso3);

CREATE TABLE IF NOT EXISTS wits_product_metadata (
    nomen_code   VARCHAR(10)  NOT NULL,
    product_code VARCHAR(20)  NOT NULL,
    tier         INTEGER,
    description  TEXT,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (nomen_code, product_code)
);

CREATE INDEX IF NOT EXISTS idx_wits_product_metadata_code
    ON wits_product_metadata (product_code);
