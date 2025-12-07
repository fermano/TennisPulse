-- Match table with FKs to player and club

CREATE TABLE match (
    id          UUID PRIMARY KEY,
    club_id     UUID,
    player1_id  UUID NOT NULL,
    player2_id  UUID NOT NULL,
    winner_id   UUID,
    final_score  VARCHAR(64),
    start_time  TIMESTAMPTZ,
    end_time    TIMESTAMPTZ,
    status      VARCHAR(32),

    CONSTRAINT fk_match_club
        FOREIGN KEY (club_id) REFERENCES club (id),

    CONSTRAINT fk_match_player1
        FOREIGN KEY (player1_id) REFERENCES player (id),

    CONSTRAINT fk_match_player2
        FOREIGN KEY (player2_id) REFERENCES player (id),

    CONSTRAINT fk_match_winner
        FOREIGN KEY (winner_id) REFERENCES player (id),

    -- Optional: keep status aligned with MatchStatus enum
    CONSTRAINT chk_match_status
        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Helpful indexes
CREATE INDEX idx_match_club_id ON match (club_id);
CREATE INDEX idx_match_player1_id ON match (player1_id);
CREATE INDEX idx_match_player2_id ON match (player2_id);
CREATE INDEX idx_match_status ON match (status);
