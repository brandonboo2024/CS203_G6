
-- V4: Make *_hist tables truly temporal + seed sample windows (NO user/past_calculation data)

BEGIN;

-- 0) Required extension for GiST exclusion
CREATE EXTENSION IF NOT EXISTS btree_gist;

--------------------------------------------------------------------------------
-- 1) PRODUCT DEFAULT HISTORY
--    PK(product_code, valid_from), drop legacy uniques on (product_code) ONLY,
--    add no-overlap exclusion constraint if missing
--------------------------------------------------------------------------------

-- Drop any UNIQUE constraints that only lock product_code in *_hist (NOT primary key)
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT conname
    FROM   pg_constraint c
    JOIN   pg_class t ON t.oid = c.conrelid
    WHERE  t.relname = 'product_tariff_default_hist'
      AND  c.contype = 'u'
      AND  (
             SELECT string_agg(a.attname, ',' ORDER BY k.ord)
             FROM unnest(c.conkey) WITH ORDINALITY k(attnum,ord)
             JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
           ) = 'product_code'
  LOOP
    EXECUTE format('ALTER TABLE product_tariff_default_hist DROP CONSTRAINT IF EXISTS %I', r.conname);
  END LOOP;
END$$;

-- Drop any UNIQUE index on ONLY (product_code) that is NOT the primary key
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT i.indexrelid::regclass AS idx
    FROM   pg_index i
    JOIN   pg_class t ON t.oid = i.indrelid
    WHERE  t.relname = 'product_tariff_default_hist'
      AND  i.indisunique = true
      AND  i.indisprimary = false
      AND  (i.indexrelid::regclass::text NOT LIKE '%_pkey') -- belt & suspenders
      AND  i.indnatts = 1
      AND  (
             SELECT attname
             FROM   pg_attribute
             WHERE  attrelid = t.oid
                    AND attnum = i.indkey[0]
           ) = 'product_code'
  LOOP
    EXECUTE format('DROP INDEX IF EXISTS %s', r.idx);
  END LOOP;
END$$;

-- Ensure PK(product_code, valid_from)
DO $$
DECLARE pkname text; cols text;
BEGIN
  SELECT c.conname,
         string_agg(a.attname, ',' ORDER BY k.ord) AS cols
  INTO   pkname, cols
  FROM   pg_constraint c
  JOIN   pg_class t ON t.oid = c.conrelid AND t.relname = 'product_tariff_default_hist'
  JOIN   unnest(c.conkey) WITH ORDINALITY k(attnum,ord) ON true
  JOIN   pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
  WHERE  c.contype = 'p'
  GROUP  BY c.conname;

  IF pkname IS NULL THEN
    ALTER TABLE product_tariff_default_hist
      ADD CONSTRAINT product_tariff_default_hist_pkey PRIMARY KEY (product_code, valid_from);
  ELSIF cols <> 'product_code,valid_from' THEN
    EXECUTE format('ALTER TABLE product_tariff_default_hist DROP CONSTRAINT %I', pkname);
    ALTER TABLE product_tariff_default_hist
      ADD CONSTRAINT product_tariff_default_hist_pkey PRIMARY KEY (product_code, valid_from);
  END IF;
END$$;

-- No-overlap exclusion (only if missing)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'product_tariff_default_hist_no_overlap'
  ) THEN
    ALTER TABLE product_tariff_default_hist
      ADD CONSTRAINT product_tariff_default_hist_no_overlap
      EXCLUDE USING gist (
        product_code WITH =,
        tstzrange(valid_from, COALESCE(valid_to, 'infinity')) WITH &&
      );
  END IF;
END$$;

--------------------------------------------------------------------------------
-- 2) ROUTE OVERRIDE HISTORY
--    PK(product_code, origin_country, dest_country, valid_from),
--    drop legacy uniques on (product_code, origin_country, dest_country) ONLY,
--    add no-overlap exclusion constraint if missing
--------------------------------------------------------------------------------

-- Drop any UNIQUE constraints that lock only the triple (NOT primary key)
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT conname
    FROM   pg_constraint c
    JOIN   pg_class t ON t.oid = c.conrelid
    WHERE  t.relname = 'route_tariff_override_hist'
      AND  c.contype = 'u'
      AND  (
             SELECT string_agg(a.attname, ',' ORDER BY k.ord)
             FROM   unnest(c.conkey) WITH ORDINALITY k(attnum,ord)
             JOIN   pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
           ) = 'product_code,origin_country,dest_country'
  LOOP
    EXECUTE format('ALTER TABLE route_tariff_override_hist DROP CONSTRAINT IF EXISTS %I', r.conname);
  END LOOP;
END$$;

-- Drop any UNIQUE index on exactly (product_code, origin_country, dest_country) that is NOT the primary key
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT idx.indexrelid::regclass AS idxname
    FROM   pg_index idx
    JOIN   pg_class t ON t.oid = idx.indrelid
    WHERE  t.relname = 'route_tariff_override_hist'
      AND  idx.indisunique = true
      AND  idx.indisprimary = false
      AND  (idx.indexrelid::regclass::text NOT LIKE '%_pkey')
      AND  idx.indnatts   = 3
      AND  (
             SELECT string_agg(a.attname, ',' ORDER BY ord)
             FROM   unnest(idx.indkey) WITH ORDINALITY k(attnum,ord)
             JOIN   pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
           ) = 'product_code,origin_country,dest_country'
  LOOP
    EXECUTE format('DROP INDEX IF EXISTS %s', r.idxname);
  END LOOP;
END$$;

-- Ensure PK(product_code, origin_country, dest_country, valid_from)
DO $$
DECLARE pkname text; cols text;
BEGIN
  SELECT c.conname,
         string_agg(a.attname, ',' ORDER BY k.ord) AS cols
  INTO   pkname, cols
  FROM   pg_constraint c
  JOIN   pg_class t ON t.oid = c.conrelid AND t.relname = 'route_tariff_override_hist'
  JOIN   unnest(c.conkey) WITH ORDINALITY k(attnum,ord) ON true
  JOIN   pg_attribute a ON a.attrelid = t.oid AND a.attnum = k.attnum
  WHERE  c.contype = 'p'
  GROUP  BY c.conname;

  IF pkname IS NULL THEN
    ALTER TABLE route_tariff_override_hist
      ADD CONSTRAINT route_tariff_override_hist_pkey
      PRIMARY KEY (product_code, origin_country, dest_country, valid_from);
  ELSIF cols <> 'product_code,origin_country,dest_country,valid_from' THEN
    EXECUTE format('ALTER TABLE route_tariff_override_hist DROP CONSTRAINT %I', pkname);
    ALTER TABLE route_tariff_override_hist
      ADD CONSTRAINT route_tariff_override_hist_pkey
      PRIMARY KEY (product_code, origin_country, dest_country, valid_from);
  END IF;
END$$;

-- No-overlap exclusion per (product, origin, dest)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'route_tariff_override_hist_no_overlap'
  ) THEN
    ALTER TABLE route_tariff_override_hist
      ADD CONSTRAINT route_tariff_override_hist_no_overlap
      EXCLUDE USING gist (
        product_code   WITH =,
        origin_country WITH =,
        dest_country   WITH =,
        tstzrange(valid_from, COALESCE(valid_to, 'infinity')) WITH &&
      );
  END IF;
END$$;

--------------------------------------------------------------------------------
-- 3) Seed/Upsert sample history windows (no overlap; safe to re-run)
--------------------------------------------------------------------------------

-- Product defaults
-- Electronics: 13% [2024-07-01, 2025-04-01), then 14%+
DO $$
DECLARE cut1 timestamptz := '2024-07-01 00:00:00+00';
DECLARE cut2 timestamptz := '2025-04-01 00:00:00+00';
BEGIN
  UPDATE product_tariff_default_hist
     SET valid_to = cut1
   WHERE product_code = 'electronics'
     AND valid_to IS NULL
     AND valid_from < cut1;

  INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
  VALUES ('electronics', 13.000, cut1, cut2)
  ON CONFLICT (product_code, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;

  INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
  VALUES ('electronics', 14.000, cut2, NULL)
  ON CONFLICT (product_code, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;
END$$;

-- Toys: 12% from 2024-10-01+
DO $$
DECLARE cut timestamptz := '2024-10-01 00:00:00+00';
BEGIN
  UPDATE product_tariff_default_hist
     SET valid_to = cut
   WHERE product_code = 'toys'
     AND valid_to IS NULL
     AND valid_from < cut;

  INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
  VALUES ('toys', 12.000, cut, NULL)
  ON CONFLICT (product_code, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;
END$$;

-- Automotive: 18% from 2025-01-01+
DO $$
DECLARE cut timestamptz := '2025-01-01 00:00:00+00';
BEGIN
  UPDATE product_tariff_default_hist
     SET valid_to = cut
   WHERE product_code = 'automotive'
     AND valid_to IS NULL
     AND valid_from < cut;

  INSERT INTO product_tariff_default_hist (product_code, rate_percent, valid_from, valid_to)
  VALUES ('automotive', 18.000, cut, NULL)
  ON CONFLICT (product_code, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;
END$$;

-- Route override example: CN->SG toys
-- 12% [2024-11-01, 2025-03-01), then 11%+
DO $$
DECLARE cut1 timestamptz := '2024-11-01 00:00:00+00';
DECLARE cut2 timestamptz := '2025-03-01 00:00:00+00';
BEGIN
  UPDATE route_tariff_override_hist
     SET valid_to = cut1
   WHERE product_code   = 'toys'
     AND origin_country = 'CN'
     AND dest_country   = 'SG'
     AND valid_to IS NULL
     AND valid_from < cut1;

  INSERT INTO route_tariff_override_hist
    (product_code, origin_country, dest_country, rate_percent, valid_from, valid_to)
  VALUES ('toys', 'CN', 'SG', 12.000, cut1, cut2)
  ON CONFLICT (product_code, origin_country, dest_country, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;

  INSERT INTO route_tariff_override_hist
    (product_code, origin_country, dest_country, rate_percent, valid_from, valid_to)
  VALUES ('toys', 'CN', 'SG', 11.000, cut2, NULL)
  ON CONFLICT (product_code, origin_country, dest_country, valid_from)
  DO UPDATE SET rate_percent = EXCLUDED.rate_percent,
                valid_to     = EXCLUDED.valid_to;
END$$;

COMMIT;
