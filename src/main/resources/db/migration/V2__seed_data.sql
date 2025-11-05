INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin', 'admin@example.com', '$2b$12$cOVUJ8jsLO/8NmCvVbfIVeq8Jxy74LuoxhRgdhSKbkknt6cClW1J2', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, email, password_hash, role) VALUES
    ('user', 'user@example.com', '$2b$12$LxmQPDNH9p1qhi0xpaiqUeyepwWvNGYPdfK6ZZvMFTeZnBG7jdvwi', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO products (code, hs_code, base_price) VALUES
    ('electronics', '847130', 1200.00),
    ('clothing', '620342', 80.00),
    ('furniture', '940360', 650.00),
    ('food', '210690', 25.00),
    ('books', '490199', 20.00),
    ('toys', '950300', 45.00),
    ('tools', '820559', 75.00),
    ('beauty', '330499', 30.00),
    ('sports', '950691', 110.00),
    ('automotive', '870829', 500.00)
ON CONFLICT (code) DO NOTHING;

INSERT INTO fee_schedule (code, amount) VALUES
    ('handling', 50.00),
    ('inspection', 80.00),
    ('processing', 65.00),
    ('others', 40.00)
ON CONFLICT (code) DO NOTHING;
