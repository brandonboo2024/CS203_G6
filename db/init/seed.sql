
-- Countries used by your UI (extend anytime)
INSERT INTO country (code, name) VALUES
('SG','Singapore'), ('US','United States'), ('MY','Malaysia'), ('TH','Thailand'),
('VN','Vietnam'), ('ID','Indonesia'), ('PH','Philippines'), ('KR','South Korea'),
('IN','India'), ('AU','Australia'), ('GB','United Kingdom'), ('DE','Germany'),
('FR','France'), ('IT','Italy'), ('ES','Spain'), ('CA','Canada'),
('BR','Brazil'), ('MX','Mexico'), ('RU','Russia'), ('ZA','South Africa'),
('CN','China'), ('JP','Japan')
ON CONFLICT (code) DO NOTHING;

-- Products with base prices (exactly your React basePrices)
INSERT INTO product (code, name, base_price) VALUES
('electronics','Electronics',100.00),
('clothing','Clothing',25.00),
('furniture','Furniture',200.00),
('food','Food',5.00),
('books','Books',15.00),
('toys','Toys',30.00),
('tools','Tools',75.00),
('beauty','Beauty Products',40.00),
('sports','Sports Equipment',60.00),
('automotive','Automotive Parts',150.00)
ON CONFLICT (code) DO UPDATE SET
  name = EXCLUDED.name,
  base_price = EXCLUDED.base_price;

-- Default tariff rates by product (exactly your React tariffRates)
INSERT INTO product_tariff_default (product_code, rate_percent) VALUES
('electronics',15.000),
('clothing',12.000),
('furniture',8.000),
('food',5.000),
('books',0.000),
('toys',10.000),
('tools',7.000),
('beauty',8.000),
('sports',6.000),
('automotive',20.000)
ON CONFLICT (product_code) DO UPDATE SET
  rate_percent = EXCLUDED.rate_percent;

-- Optional: add route-specific overrides here if you want to vary by country pair later
-- Example: toys from CN to SG are 12% instead of default 10%
-- INSERT INTO route_tariff_override (product_code, origin_country, dest_country, rate_percent)
-- VALUES ('toys','CN','SG',12.000)
-- ON CONFLICT (product_code, origin_country, dest_country) DO UPDATE SET
--   rate_percent = EXCLUDED.rate_percent;

-- Fees to match your UI toggles
INSERT INTO fee_schedule (fee, amount) VALUES
('handling', 25.00),
('inspection', 15.00),
('processing', 8.00),
('others', 10.50)
ON CONFLICT (fee) DO UPDATE SET
  amount = EXCLUDED.amount;
