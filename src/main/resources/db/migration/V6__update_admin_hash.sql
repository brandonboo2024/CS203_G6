--- Enable bcrypt hashing via pgcrypto (ships with Postgres contrib)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Ensure there's exactly one admin account for dev
-- Uses bcrypt: crypt('<plaintext>', gen_salt('bf', 10))
INSERT INTO users (username, email, password_hash, role)
VALUES (
  'admin',
  'admin@example.com',
  crypt('admin123', gen_salt('bf', 10)),
  'ADMIN'
)
ON CONFLICT (username) DO UPDATE
SET
  email = EXCLUDED.email,
  password_hash = crypt('admin123', gen_salt('bf', 10)),
  role = 'ADMIN';

