-- Add the missing 'active' column to the tags table
-- This column is required for the tag activation/deactivation feature

ALTER TABLE tags ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

-- Update any existing tags to be active by default
UPDATE tags SET active = true WHERE active IS NULL;

