-- Fix Duplicate Columns in Users Table
-- This script migrates data and removes duplicate columns

-- Step 1: Check current data distribution
-- Run these to see which columns have data:
-- SELECT COUNT(*) as full_name_count FROM users WHERE full_name IS NOT NULL;
-- SELECT COUNT(*) as fullname_count FROM users WHERE fullname IS NOT NULL;
-- SELECT COUNT(*) as phone_number_count FROM users WHERE phone_number IS NOT NULL;
-- SELECT COUNT(*) as phonenumber_count FROM users WHERE phonenumber IS NOT NULL;

-- Step 2: Migrate data from duplicate columns to correct ones
-- (Only migrate if the correct column is NULL and duplicate has data)

-- Migrate fullname -> full_name
UPDATE users 
SET full_name = fullname 
WHERE full_name IS NULL AND fullname IS NOT NULL;

-- Migrate phonenumber -> phone_number
UPDATE users 
SET phone_number = phonenumber 
WHERE phone_number IS NULL AND phonenumber IS NOT NULL;

-- Migrate createdat -> created_at
UPDATE users 
SET created_at = createdat 
WHERE created_at IS NULL AND createdat IS NOT NULL;

-- Migrate resettoken -> reset_token
UPDATE users 
SET reset_token = resettoken 
WHERE reset_token IS NULL AND resettoken IS NOT NULL;

-- Migrate resettokenexpiry -> reset_token_expiry
UPDATE users 
SET reset_token_expiry = resettokenexpiry 
WHERE reset_token_expiry IS NULL AND resettokenexpiry IS NOT NULL;

-- Step 3: Verify migration (check that correct columns now have data)
-- SELECT COUNT(*) as full_name_count FROM users WHERE full_name IS NOT NULL;
-- SELECT COUNT(*) as phone_number_count FROM users WHERE phone_number IS NOT NULL;

-- Step 4: Drop duplicate columns (ONLY after verifying migration!)
-- WARNING: Make sure you have a backup before running these!

ALTER TABLE users DROP COLUMN IF EXISTS fullname;
ALTER TABLE users DROP COLUMN IF EXISTS phonenumber;
ALTER TABLE users DROP COLUMN IF EXISTS createdat;
ALTER TABLE users DROP COLUMN IF EXISTS resettoken;
ALTER TABLE users DROP COLUMN IF EXISTS resettokenexpiry;

-- Step 5: Verify the fix
-- \d users
-- You should now only see: full_name, phone_number, created_at, reset_token, reset_token_expiry

