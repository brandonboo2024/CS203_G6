CREATE TABLE IF NOT EXISTS wits_tariffs (
    id              BIGSERIAL PRIMARY KEY,
    nomen_code      VARCHAR(10)  NOT NULL,
    reporter_iso    VARCHAR(10)  NOT NULL,
    partner_code    VARCHAR(10)  NOT NULL,
    product_code    VARCHAR(20)  NOT NULL,
    year            INTEGER      NOT NULL,
    sum_of_rates    NUMERIC(12,4),
    min_rate        NUMERIC(12,4),
    max_rate        NUMERIC(12,4),
    simple_average  NUMERIC(12,4),
    total_no_of_lines INTEGER,
    nbr_pref_lines    INTEGER,
    nbr_mfn_lines     INTEGER,
    nbr_na_lines      INTEGER,
    est_code        VARCHAR(10),
    source_file     VARCHAR(255) NOT NULL,
    inserted_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wits_tariffs_combo
    ON wits_tariffs (nomen_code, reporter_iso, partner_code, product_code, year, est_code);

CREATE INDEX IF NOT EXISTS idx_wits_tariffs_lookup
    ON wits_tariffs (reporter_iso, partner_code, product_code, year);
