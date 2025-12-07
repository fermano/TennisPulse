-- ================================
-- Seed data for TennisPulse project
-- ================================

-- Seed Clubs
INSERT INTO club (id, name, city, country)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Green Valley Tennis Club', 'Austin', 'USA'),
    ('00000000-0000-0000-0000-000000000002', 'Lakeside Tennis Center', 'Denver', 'USA'),
    ('00000000-0000-0000-0000-000000000003', 'Sunset Tennis Academy', 'Los Angeles', 'USA');

-- Seed Players
INSERT INTO player (id, name, handedness, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000011', 'Alice Johnson',   'RIGHT', NOW()),
    ('00000000-0000-0000-0000-000000000012', 'Bruno Silva',     'LEFT',  NOW()),
    ('00000000-0000-0000-0000-000000000013', 'Carla Mendes',    'RIGHT', NOW()),
    ('00000000-0000-0000-0000-000000000014', 'Daniel Thompson', 'RIGHT', NOW());

-- Match 1: Alice vs Bruno, Alice wins 6–4 6–3
INSERT INTO match (
    id, club_id, player1_id, player2_id, winner_id,
    final_score, start_time, end_time, status
)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000011',
    '00000000-0000-0000-0000-000000000012',
    '00000000-0000-0000-0000-000000000011',
    '6-4 6-3',
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '3 days' + INTERVAL '1 hour 15 minutes',
    'COMPLETED'
);

-- Match 2: Carla vs Daniel, Carla wins 7–5 6–4
INSERT INTO match (
    id, club_id, player1_id, player2_id, winner_id,
    final_score, start_time, end_time, status
)
VALUES (
    '00000000-0000-0000-0000-000000000102',
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000013',
    '00000000-0000-0000-0000-000000000014',
    '00000000-0000-0000-0000-000000000013',
    '7-5 6-4',
    NOW() - INTERVAL '1 days',
    NOW() - INTERVAL '1 days' + INTERVAL '1 hour 30 minutes',
    'COMPLETED'
);
