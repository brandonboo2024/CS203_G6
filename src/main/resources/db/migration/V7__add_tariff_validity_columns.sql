ALTER TABLE tariffs
    ADD COLUMN valid_from DATE NOT NULL DEFAULT '2000-01-01',
    ADD COLUMN valid_to DATE NOT NULL DEFAULT '2199-12-31',
    ADD COLUMN label VARCHAR(255) NOT NULL DEFAULT 'Custom tariff',
    ADD COLUMN notes TEXT,
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_tariffs_route_validity
    ON tariffs (origin_country, destination_country, product, valid_from, valid_to);
