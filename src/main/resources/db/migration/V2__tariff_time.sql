
-- V2: make tariff rates temporal so we can query “what was valid at time T”
-- Safe to re-run on a fresh DB after V1/V1_1.

-- 0) Enable extension needed for exclusion constraints
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1) Rename current tables to *_hist (they become the history tables)
ALTER TABLE IF EXISTS product_tariff_default RENAME TO product_tariff_default_hist;
ALTER TABLE IF EXISTS route_tariff_override RENAME TO route_tariff_override_hist;

-- 2) Add validity columns (half-open [valid_from, valid_to) )
ALTER TABLE product_tariff_default_hist
  ADD COLUMN IF NOT EXISTS valid_from timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS valid_to   timestamptz;

ALTER TABLE route_tariff_override_hist
  ADD COLUMN IF NOT EXISTS valid_from timestamptz NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS valid_to   timestamptz;

-- 3) Prevent overlaps per key using exclusion constraints
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'product_tariff_default_hist_no_overlap'
  ) THEN
    ALTER TABLE product_tariff_default_hist
      ADD CONSTRAINT product_tariff_default_hist_no_overlap
      EXCLUDE USING gist (
        product_code WITH =,
        tstzrange(valid_from, valid_to, '[)') WITH &&
      );
  END IF;
END$$;

DO $$
BEGIN
  -- drop the old unique if it exists (keyed on product+origin+dest)
  IF EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'route_tariff_override_product_code_origin_country_dest_country_key'
  ) THEN
    ALTER TABLE route_tariff_override_hist
      DROP CONSTRAINT route_tariff_override_product_code_origin_country_dest_country_key;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'route_tariff_override_hist_no_overlap'
  ) THEN
    ALTER TABLE route_tariff_override_hist
      ADD CONSTRAINT route_tariff_override_hist_no_overlap
      EXCLUDE USING gist (
        product_code WITH =,
        origin_country WITH =,
        dest_country   WITH =,
        tstzrange(valid_from, valid_to, '[)') WITH &&
      );
  END IF;
END$$;

-- 4) Helpful indexes for lookups
CREATE INDEX IF NOT EXISTS idx_default_hist_lookup
  ON product_tariff_default_hist (product_code, valid_from, valid_to);

CREATE INDEX IF NOT EXISTS idx_route_hist_lookup
  ON route_tariff_override_hist (product_code, origin_country, dest_country, valid_from, valid_to);

-- 5) Backfill the history window so past queries work (open-ended if NULL)
UPDATE product_tariff_default_hist
SET valid_from = '2020-01-01 00:00:00+00'
WHERE valid_to IS NULL;

UPDATE route_tariff_override_hist
SET valid_from = '2020-01-01 00:00:00+00'
WHERE valid_to IS NULL;

-- 6) (Optional) compatibility views showing only the “current” rows (valid_to IS NULL).
--    This keeps any old queries reading the original table names working.
CREATE OR REPLACE VIEW product_tariff_default AS
SELECT product_code, rate_percent
FROM product_tariff_default_hist
WHERE valid_to IS NULL;

CREATE OR REPLACE VIEW route_tariff_override AS
SELECT id, product_code, origin_country, dest_country, rate_percent
FROM route_tariff_override_hist
WHERE valid_to IS NULL;
