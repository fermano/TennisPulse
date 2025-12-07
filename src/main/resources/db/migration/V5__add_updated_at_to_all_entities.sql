-- Add updated_at column to player, club, match

ALTER TABLE player
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

ALTER TABLE club
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

ALTER TABLE match
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- Initialize updated_at for existing rows
UPDATE player
SET updated_at = NOW()
WHERE updated_at IS NULL;

UPDATE club
SET updated_at = NOW()
WHERE updated_at IS NULL;

UPDATE match
SET updated_at = NOW()
WHERE updated_at IS NULL;

-- Make it non-nullable for consistency
ALTER TABLE player
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE club
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE match
    ALTER COLUMN updated_at SET NOT NULL;
