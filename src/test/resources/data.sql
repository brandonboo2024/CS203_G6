INSERT INTO users (id, username, email, password_hash, role) VALUES
  (1, 'admin', 'admin@example.com', '$2b$12$cOVUJ8jsLO/8NmCvVbfIVeq8Jxy74LuoxhRgdhSKbkknt6cClW1J2', 'ADMIN'),
  (2, 'user', 'user@example.com', '$2b$12$LxmQPDNH9p1qhi0xpaiqUeyepwWvNGYPdfK6ZZvMFTeZnBG7jdvwi', 'USER');

INSERT INTO products (id, code, hs_code, base_price) VALUES
  (1, 'electronics', '847130', 1200.00),
  (2, 'clothing', '620342', 80.00),
  (3, 'furniture', '940360', 650.00),
  (4, 'food', '210690', 25.00),
  (5, 'books', '490199', 20.00),
  (6, 'toys', '950300', 45.00),
  (7, 'tools', '820559', 75.00),
  (8, 'beauty', '330499', 30.00),
  (9, 'sports', '950691', 110.00),
  (10, 'automotive', '870829', 500.00);

INSERT INTO fee_schedule (code, amount) VALUES
  ('handling', 50.00),
  ('inspection', 80.00),
  ('processing', 65.00),
  ('others', 40.00);
