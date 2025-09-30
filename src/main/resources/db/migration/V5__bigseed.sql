
-- V5: Big seed of product defaults and route overrides (2020-2025+)
-- Overlap-safe: uses NOT EXISTS for fixed windows and shifts 2024+ start past any existing segment.

BEGIN;

DO $$
DECLARE
  c2020 timestamptz := '2020-01-01 00:00:00+00';
  c2022 timestamptz := '2022-01-01 00:00:00+00';
  c2024 timestamptz := '2024-01-01 00:00:00+00';
  c2025 timestamptz := '2025-01-01 00:00:00+00';

  p text;
  products text[] := ARRAY[
    'electronics','clothing','furniture','food','books',
    'toys','tools','beauty','sports','automotive'
  ];

  start3 timestamptz;
  latest_end timestamptz;
BEGIN
  --------------------------------------------------------------------------
  -- 1) PRODUCT DEFAULT RATES
  --------------------------------------------------------------------------
  FOREACH p IN ARRAY products LOOP
    -- Close any open-ended segments that started before the next cuts
    UPDATE product_tariff_default_hist
       SET valid_to = c2022
     WHERE product_code = p AND valid_to IS NULL AND valid_from < c2022;

    UPDATE product_tariff_default_hist
       SET valid_to = c2024
     WHERE product_code = p AND valid_to IS NULL AND valid_from < c2024;

    UPDATE product_tariff_default_hist
       SET valid_to = c2025
     WHERE product_code = p AND valid_to IS NULL AND valid_from < c2025;

    -- 2020-2021/12/31: only insert if no overlap exists
    INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
    SELECT p,
           CASE p
             WHEN 'electronics' THEN 10.000
             WHEN 'clothing'    THEN 8.000
             WHEN 'furniture'   THEN 9.000
             WHEN 'food'        THEN 5.000
             WHEN 'books'       THEN 2.000
             WHEN 'toys'        THEN 7.000
             WHEN 'tools'       THEN 6.000
             WHEN 'beauty'      THEN 6.500
             WHEN 'sports'      THEN 6.500
             WHEN 'automotive'  THEN 12.000
           END,
           c2020, c2022
    WHERE NOT EXISTS (
      SELECT 1 FROM product_tariff_default_hist d
       WHERE d.product_code = p
         AND tstzrange(d.valid_from, COALESCE(d.valid_to,'infinity'),'[)') &&
             tstzrange(c2020, c2022, '[)')
    )
    ON CONFLICT (product_code, valid_from)
    DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                  valid_to     = EXCLUDED.valid_to;

    -- 2022-2023/12/31: only insert if no overlap exists
    INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
    SELECT p,
           CASE p
             WHEN 'electronics' THEN 11.500
             WHEN 'clothing'    THEN 9.000
             WHEN 'furniture'   THEN 10.000
             WHEN 'food'        THEN 5.500
             WHEN 'books'       THEN 2.500
             WHEN 'toys'        THEN 7.500
             WHEN 'tools'       THEN 6.500
             WHEN 'beauty'      THEN 7.000
             WHEN 'sports'      THEN 7.000
             WHEN 'automotive'  THEN 13.500
           END,
           c2022, c2024
    WHERE NOT EXISTS (
      SELECT 1 FROM product_tariff_default_hist d
       WHERE d.product_code = p
         AND tstzrange(d.valid_from, COALESCE(d.valid_to,'infinity'),'[)') &&
             tstzrange(c2022, c2024, '[)')
    )
    ON CONFLICT (product_code, valid_from)
    DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                  valid_to     = EXCLUDED.valid_to;

    -- 2024-∞ : start right after the latest existing segment that ends after 2024
    start3 := c2024;
    SELECT MAX(d.valid_to) INTO latest_end
      FROM product_tariff_default_hist d
     WHERE d.product_code = p
       AND d.valid_to IS NOT NULL
       AND d.valid_to > c2024;

    IF latest_end IS NOT NULL AND latest_end > start3 THEN
      start3 := latest_end;
    END IF;

    -- Only insert if there is no overlap with anything from start3 onward
    INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
    SELECT p,
           CASE p
             WHEN 'electronics' THEN 12.500
             WHEN 'clothing'    THEN 10.000
             WHEN 'furniture'   THEN 11.000
             WHEN 'food'        THEN 6.000
             WHEN 'books'       THEN 3.000
             WHEN 'toys'        THEN 8.500
             WHEN 'tools'       THEN 7.500
             WHEN 'beauty'      THEN 7.800
             WHEN 'sports'      THEN 7.800
             WHEN 'automotive'  THEN 15.000
           END,
           start3, NULL
    WHERE NOT EXISTS (
      SELECT 1 FROM product_tariff_default_hist d
       WHERE d.product_code = p
         AND tstzrange(d.valid_from, COALESCE(d.valid_to,'infinity'),'[)') &&
             tstzrange(start3, 'infinity','[)')
    )
    ON CONFLICT (product_code, valid_from)
    DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                  valid_to     = EXCLUDED.valid_to;

  END LOOP;
END$$;

---------------------------------------------------------------------------
-- 2) ROUTE OVERRIDES (helper respects existing segments and shifts 2024+)
---------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION seed_route(
  p_product text,
  p_origin  text,
  p_dest    text,
  r1 numeric, r2 numeric, r3 numeric
) RETURNS void LANGUAGE plpgsql AS $f$
DECLARE
  c2020 timestamptz := '2020-01-01 00:00:00+00';
  c2022 timestamptz := '2022-01-01 00:00:00+00';
  c2024 timestamptz := '2024-01-01 00:00:00+00';
  c2025 timestamptz := '2025-01-01 00:00:00+00';
  start3 timestamptz;
  latest_end timestamptz;
BEGIN
  -- Close any open-ended segments before cuts
  UPDATE route_tariff_override_hist
     SET valid_to = c2022
   WHERE product_code=p_product AND origin_country=p_origin AND dest_country=p_dest
     AND valid_to IS NULL AND valid_from < c2022;

  UPDATE route_tariff_override_hist
     SET valid_to = c2024
   WHERE product_code=p_product AND origin_country=p_origin AND dest_country=p_dest
     AND valid_to IS NULL AND valid_from < c2024;

  UPDATE route_tariff_override_hist
     SET valid_to = c2025
   WHERE product_code=p_product AND origin_country=p_origin AND dest_country=p_dest
     AND valid_to IS NULL AND valid_from < c2025;

  -- 2020-2021/12/31 if no overlap
  INSERT INTO route_tariff_override_hist
    (product_code, origin_country, dest_country, rate_percent, valid_from, valid_to)
  SELECT p_product, p_origin, p_dest, r1, c2020, c2022
  WHERE NOT EXISTS (
    SELECT 1 FROM route_tariff_override_hist h
     WHERE h.product_code=p_product AND h.origin_country=p_origin AND h.dest_country=p_dest
       AND tstzrange(h.valid_from, COALESCE(h.valid_to,'infinity'),'[)') &&
           tstzrange(c2020, c2022, '[)')
  )
  ON CONFLICT (product_code, origin_country, dest_country, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;

  -- 2022-2023/12/31 if no overlap
  INSERT INTO route_tariff_override_hist
    (product_code, origin_country, dest_country, rate_percent, valid_from, valid_to)
  SELECT p_product, p_origin, p_dest, r2, c2022, c2024
  WHERE NOT EXISTS (
    SELECT 1 FROM route_tariff_override_hist h
     WHERE h.product_code=p_product AND h.origin_country=p_origin AND h.dest_country=p_dest
       AND tstzrange(h.valid_from, COALESCE(h.valid_to,'infinity'),'[)') &&
           tstzrange(c2022, c2024, '[)')
  )
  ON CONFLICT (product_code, origin_country, dest_country, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;

  -- 2024-∞ start after latest existing segment that ends after 2024
  start3 := c2024;
  SELECT MAX(h.valid_to) INTO latest_end
    FROM route_tariff_override_hist h
   WHERE h.product_code=p_product AND h.origin_country=p_origin AND h.dest_country=p_dest
     AND h.valid_to IS NOT NULL AND h.valid_to > c2024;

  IF latest_end IS NOT NULL AND latest_end > start3 THEN
    start3 := latest_end;
  END IF;

  INSERT INTO route_tariff_override_hist
    (product_code, origin_country, dest_country, rate_percent, valid_from, valid_to)
  SELECT p_product, p_origin, p_dest, r3, start3, NULL
  WHERE NOT EXISTS (
    SELECT 1 FROM route_tariff_override_hist h
     WHERE h.product_code=p_product AND h.origin_country=p_origin AND h.dest_country=p_dest
       AND tstzrange(h.valid_from, COALESCE(h.valid_to,'infinity'),'[)') &&
           tstzrange(start3, 'infinity','[)')
  )
  ON CONFLICT (product_code, origin_country, dest_country, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;
END
$f$;

-- Example routes (same list as before; safe to re-run)
SELECT seed_route('electronics','CN','SG',  9.5,  8.5,  8.0);
SELECT seed_route('electronics','US','SG',  7.0,  6.5,  6.0);
SELECT seed_route('electronics','KR','JP',  6.5,  6.0,  5.8);
SELECT seed_route('electronics','DE','GB',  5.0,  5.5,  6.0);

SELECT seed_route('clothing','VN','US',     8.0,  7.5,  7.0);
SELECT seed_route('clothing','IN','GB',     7.5,  7.0,  6.8);
SELECT seed_route('clothing','CN','DE',     7.0,  6.8,  6.5);

SELECT seed_route('furniture','MY','AU',    6.0,  5.5,  5.2);
SELECT seed_route('furniture','ID','SG',    5.5,  5.2,  5.0);
SELECT seed_route('furniture','ES','FR',    4.8,  5.0,  5.2);

SELECT seed_route('food','TH','SG',         3.5,  3.2,  3.0);
SELECT seed_route('food','BR','US',         4.0,  3.8,  3.5);
SELECT seed_route('food','MX','CA',         3.8,  3.5,  3.3);

SELECT seed_route('books','JP','US',        2.5,  2.3,  2.2);
SELECT seed_route('books','GB','CA',        2.2,  2.1,  2.0);
SELECT seed_route('books','DE','FR',        2.0,  2.0,  2.1);

SELECT seed_route('toys','CN','SG',         9.0,  8.0,  7.5);
SELECT seed_route('toys','US','GB',         7.5,  7.0,  6.5);
SELECT seed_route('toys','KR','AU',         6.8,  6.5,  6.2);

SELECT seed_route('tools','CN','IN',        6.5,  6.2,  6.0);
SELECT seed_route('tools','DE','US',        5.5,  5.8,  6.0);
SELECT seed_route('tools','JP','SG',        5.8,  5.6,  5.5);

SELECT seed_route('beauty','KR','SG',       6.5,  6.0,  5.8);
SELECT seed_route('beauty','FR','GB',       6.0,  6.2,  6.5);
SELECT seed_route('beauty','US','CA',       5.5,  5.3,  5.2);

SELECT seed_route('sports','AU','SG',       6.0,  5.8,  5.5);
SELECT seed_route('sports','US','MX',       6.5,  6.2,  6.0);
SELECT seed_route('sports','IT','ES',       5.5,  5.6,  5.8);

SELECT seed_route('automotive','DE','GB',  12.0, 14.0, 15.0);
SELECT seed_route('automotive','JP','US',  11.0, 12.5, 13.5);
SELECT seed_route('automotive','CN','ZA',  10.5, 11.5, 12.5);

SELECT seed_route('electronics','RU','DE',  6.0,  6.3,  6.7);
SELECT seed_route('clothing','PH','SG',     7.2,  7.0,  6.8);
SELECT seed_route('food','VN','JP',         3.6,  3.4,  3.2);
SELECT seed_route('sports','ZA','GB',       6.8,  6.6,  6.4);

-- Optional: drop helper if you don't want to keep it around
DROP FUNCTION IF EXISTS seed_route(text,text,text,numeric,numeric,numeric);

COMMIT;
