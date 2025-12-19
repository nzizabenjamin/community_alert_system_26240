-- Fix Duplicate Columns in Users Table
-- This script helps identify and fix duplicate columns created by Hibernate

-- Step 1: Check for duplicate columns (run this first to see what exists)
-- Look for columns like: fullname, full_name, phonenumber, phone_number, etc.

-- Step 2: If you have both 'fullname' and 'full_name', migrate data and drop the duplicate
-- (Adjust column names based on what you find in your database)

-- Example: If 'full_name' has data and 'fullname' is NULL, keep 'full_name'
-- If 'fullname' has data and 'full_name' is NULL, migrate data to 'full_name' and drop 'fullname'

-- Migrate data from duplicate columns to correct ones (if needed):
-- UPDATE users SET full_name = fullname WHERE full_name IS NULL AND fullname IS NOT NULL;
-- UPDATE users SET phone_number = phonenumber WHERE phone_number IS NULL AND phonenumber IS NOT NULL;
-- UPDATE users SET created_at = createdat WHERE created_at IS NULL AND createdat IS NOT NULL;

-- Step 3: Drop duplicate columns (ONLY after migrating data!)
-- ALTER TABLE users DROP COLUMN IF EXISTS fullname;
-- ALTER TABLE users DROP COLUMN IF EXISTS phonenumber;
-- ALTER TABLE users DROP COLUMN IF EXISTS createdat;
-- ALTER TABLE users DROP COLUMN IF EXISTS resettoken;
-- ALTER TABLE users DROP COLUMN IF EXISTS resettokenexpiry;

-- Step 4: Verify the correct columns exist
-- \d users
-- You should see: full_name, phone_number, created_at, reset_token, reset_token_expiry

-- IMPORTANT: 
-- 1. Backup your database before running DROP statements
-- 2. Check which columns have data before dropping
-- 3. The correct column names should be: full_name, phone_number, created_at, reset_token, reset_token_expiry

