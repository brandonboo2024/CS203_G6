
-- Schema prep: must run before any DML that uses ON CONFLICT
CREATE UNIQUE INDEX IF NOT EXISTS ux_route_tariff_override_hist_unique
  ON route_tariff_override_hist (product_code, origin_country, dest_country, valid_from);
