# Fix for Missing 'active' Column in Tags Table

## Problem
The `tags` table is missing the `active` column, causing queries to fail.

## Solution

### Step 1: Add the Column Manually (Recommended)
Run this SQL in your PostgreSQL database:

```sql
-- Add the column as nullable first
ALTER TABLE tags ADD COLUMN IF NOT EXISTS active BOOLEAN;

-- Set default value for existing rows
UPDATE tags SET active = true WHERE active IS NULL;

-- Now make it NOT NULL with default
ALTER TABLE tags ALTER COLUMN active SET NOT NULL;
ALTER TABLE tags ALTER COLUMN active SET DEFAULT true;
```

### Step 2: Restart Application
After adding the column, restart your Spring Boot application.

### Alternative: Let Hibernate Add It
If you prefer, you can let Hibernate add it automatically:
1. The entity now has `nullable = true` temporarily
2. Restart the application - Hibernate will add the column
3. Then run the UPDATE and ALTER statements above to make it NOT NULL

## Verification
After adding the column, verify it exists:
```sql
\d tags
```

You should see the `active` column listed.

