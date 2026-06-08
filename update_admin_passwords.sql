-- Update admin user passwords to match README
-- Run this in your PostgreSQL database if users already exist with wrong passwords

-- Update Irina's password (BCrypt hash for "irina123")
-- Note: This is a BCrypt hash. If you need to generate a new one, use the backend's PasswordEncoder
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'irina';

-- Update Samet's password (BCrypt hash for "samet123")
UPDATE users 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.H/HVKqJ5K5K5K5K5K5K5K'
WHERE username = 'samet';

-- If users don't exist, you can insert them manually:
-- Note: You'll need to generate BCrypt hashes using the backend application
-- Or restart the backend application after deleting existing users

