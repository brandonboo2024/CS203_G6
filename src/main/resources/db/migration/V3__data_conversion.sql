
-- Convert NUMERIC columns to double precision to align with Java Double (float8)

-- past_calculation_segments table
ALTER TABLE past_calculation_segments
  ALTER COLUMN rate_percent     TYPE double precision USING rate_percent::double precision,
  ALTER COLUMN quantity_portion TYPE double precision USING quantity_portion::double precision,
  ALTER COLUMN item_price       TYPE double precision USING item_price::double precision,
  ALTER COLUMN tariff_amount    TYPE double precision USING tariff_amount::double precision;

-- past_calculations table
ALTER TABLE past_calculations
  ALTER COLUMN handling_fee     TYPE double precision USING handling_fee::double precision,
  ALTER COLUMN processing_fee   TYPE double precision USING processing_fee::double precision,
  ALTER COLUMN inspection_fee   TYPE double precision USING inspection_fee::double precision,
  ALTER COLUMN other_fees       TYPE double precision USING other_fees::double precision,
  ALTER COLUMN total_price      TYPE double precision USING total_price::double precision;
