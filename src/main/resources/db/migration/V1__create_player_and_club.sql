CREATE TABLE player (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    handedness  VARCHAR(16),              -- LEFT / RIGHT (Handedness enum)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE club (
    id               UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    city             VARCHAR(255),
    country          VARCHAR(255),
    surface_default  VARCHAR(32),          -- CLAY / HARD / GRASS (CourtSurface enum)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_player_name ON player (name);
CREATE INDEX idx_club_city ON club (city);
