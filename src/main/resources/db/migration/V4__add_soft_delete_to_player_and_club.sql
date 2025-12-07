-- Soft delete support for player and club

ALTER TABLE player
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE club
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Ensure existing rows are marked as not deleted
UPDATE player
SET deleted = FALSE
WHERE deleted IS NULL;

UPDATE club
SET deleted = FALSE
WHERE deleted IS NULL;
