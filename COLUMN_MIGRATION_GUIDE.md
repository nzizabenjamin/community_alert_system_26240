# Column Migration Guide

## Problem
Hibernate may have created duplicate columns in the `users` table because explicit `@Column(name = "...")` annotations were missing. This resulted in:
- Original columns: `full_name`, `phone_number`, `created_at` (with data)
- New columns: `fullname`, `phonenumber`, `createdat` (NULL, created by Hibernate)

## Solution

### Step 1: Check Your Database
Run this in PostgreSQL to see all columns:
```sql
\d users
```

Look for duplicate columns like:
- `full_name` vs `fullname`
- `phone_number` vs `phonenumber`  
- `created_at` vs `createdat`
- `reset_token` vs `resettoken`
- `reset_token_expiry` vs `resettokenexpiry`

### Step 2: Migrate Data (if needed)
If the new columns have data and old ones are NULL, or vice versa, migrate:

```sql
-- Migrate from new columns to correct snake_case columns
UPDATE users 
SET full_name = fullname 
WHERE full_name IS NULL AND fullname IS NOT NULL;

UPDATE users 
SET phone_number = phonenumber 
WHERE phone_number IS NULL AND phonenumber IS NOT NULL;

UPDATE users 
SET created_at = createdat 
WHERE created_at IS NULL AND createdat IS NOT NULL;
```

### Step 3: Drop Duplicate Columns
**ONLY after migrating data!**

```sql
ALTER TABLE users DROP COLUMN IF EXISTS fullname;
ALTER TABLE users DROP COLUMN IF EXISTS phonenumber;
ALTER TABLE users DROP COLUMN IF EXISTS createdat;
ALTER TABLE users DROP COLUMN IF EXISTS resettoken;
ALTER TABLE users DROP COLUMN IF EXISTS resettokenexpiry;
```

### Step 4: Verify
```sql
\d users
```

You should now only see the correct columns:
- `full_name` (not `fullname`)
- `phone_number` (not `phonenumber`)
- `created_at` (not `createdat`)
- `reset_token` (not `resettoken`)
- `reset_token_expiry` (not `resettokenexpiry`)

### Step 5: Restart Application
After fixing the columns, restart your Spring Boot application. The entity now has explicit `@Column` annotations matching the correct database column names.

## What Was Fixed
- Added explicit `@Column(name = "full_name")` to `fullName` field
- Added explicit `@Column(name = "phone_number")` to `phoneNumber` field
- Added explicit `@Column(name = "created_at")` to `createdAt` field
- Added explicit `@Column(name = "reset_token")` to `resetToken` field
- Added explicit `@Column(name = "reset_token_expiry")` to `resetTokenExpiry` field

Now Hibernate will use the correct existing columns instead of creating new ones.

