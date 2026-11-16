-- =======================================================================
-- CLASH OF BYTES: 2D MULTIPLAYER FIGHTING GAME
-- SEED DATA: CANONICAL 6 FIGHTERS & BENCHMARK MATCH TELEMETRY
-- Compatible with PostgreSQL
-- =======================================================================

-- 1. INSERT CANONICAL 6 FIGHTERS
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES
(1, 'Ironclad Warrior', 'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.', 'WarriorFighter', 115.0, 18.0, 6.0, 5.0),
(2, 'Blaze Berserker', 'Reckless brawler trading armor defense for devastating fury damage multipliers.', 'BerserkerWarrior', 130.0, 22.0, 4.0, 6.5),
(3, 'Shadow Ninja', 'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.', 'NinjaFighter', 95.0, 16.0, 4.0, 9.0),
(4, 'Cyber Brawler', 'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.', 'CyberBrawler', 110.0, 19.0, 3.0, 7.0),
(5, 'Aura Paladin', 'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.', 'PaladinFighter', 120.0, 17.0, 7.5, 4.5),
(6, 'Titan Gladiator', 'Colosseum champion master combining raw berserker fury with elemental magma strikes.', 'HybridGladiator', 140.0, 24.0, 8.0, 4.0)
ON CONFLICT (character_id) DO UPDATE SET
    character_name = EXCLUDED.character_name,
    character_description = EXCLUDED.character_description,
    archetype = EXCLUDED.archetype,
    base_hp = EXCLUDED.base_hp,
    base_attack = EXCLUDED.base_attack,
    base_defense = EXCLUDED.base_defense,
    speed = EXCLUDED.speed;

-- Reset sequence to continue after ID 6
SELECT setval('characters_character_id_seq', (SELECT MAX(character_id) FROM characters));

-- 2. SEED DEFAULT TOURNAMENT USERS (BCrypt hashes for 'Password@123')
-- BCrypt hash: $2a$12$e876HkK1i6zQhZqA8k6cquqD3zY8gq1C3Z5E0I8Y9J1K2L3M4N5O6 (sample)
INSERT INTO users (user_id, username, email, password_hash, salt, created_at) VALUES
(1, 'cyber_champion', 'champion@clashofbytes.io', '$2a$12$k2q0K5m1E9N.wGfBv0y9vOQ8gD9J4k2L5M1N8P3R6T9V2X5Z8B1D.', '', CURRENT_TIMESTAMP - INTERVAL '7 days'),
(2, 'neon_striker', 'striker@clashofbytes.io', '$2a$12$k2q0K5m1E9N.wGfBv0y9vOQ8gD9J4k2L5M1N8P3R6T9V2X5Z8B1D.', '', CURRENT_TIMESTAMP - INTERVAL '6 days'),
(3, 'shadow_master', 'shadow@clashofbytes.io', '$2a$12$k2q0K5m1E9N.wGfBv0y9vOQ8gD9J4k2L5M1N8P3R6T9V2X5Z8B1D.', '', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(4, 'blaze_warrior', 'blaze@clashofbytes.io', '$2a$12$k2q0K5m1E9N.wGfBv0y9vOQ8gD9J4k2L5M1N8P3R6T9V2X5Z8B1D.', '', CURRENT_TIMESTAMP - INTERVAL '4 days'),
(5, 'iron_vanguard', 'vanguard@clashofbytes.io', '$2a$12$k2q0K5m1E9N.wGfBv0y9vOQ8gD9J4k2L5M1N8P3R6T9V2X5Z8B1D.', '', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (user_id) DO NOTHING;

SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM users));

-- 3. SEED INITIAL MATCHES
INSERT INTO matches (match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score) VALUES
(1, CURRENT_TIMESTAMP - INTERVAL '2 days', 1, 2, 1, 3, 'COMPLETED', 240, 180),
(2, CURRENT_TIMESTAMP - INTERVAL '1 day', 3, 4, 3, 3, 'COMPLETED', 220, 195),
(3, CURRENT_TIMESTAMP - INTERVAL '12 hours', 1, 5, 1, 3, 'COMPLETED', 260, 150)
ON CONFLICT (match_id) DO NOTHING;

SELECT setval('matches_match_id_seq', (SELECT MAX(match_id) FROM matches));

-- 4. SEED MATCH PLAYERS
INSERT INTO match_players (match_id, user_id, character_id, final_score, result) VALUES
(1, 1, 4, 240, 'WIN'),
(1, 2, 3, 180, 'LOSS'),
(2, 3, 1, 220, 'WIN'),
(2, 4, 2, 195, 'LOSS'),
(3, 1, 6, 260, 'WIN'),
(3, 5, 5, 150, 'LOSS')
ON CONFLICT DO NOTHING;

-- 5. SEED MATCH ROUNDS
INSERT INTO match_rounds (match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES
(1, 1, 1, 80, 60, 'COMPLETED'),
(1, 2, 2, 70, 80, 'COMPLETED'),
(1, 3, 1, 90, 40, 'COMPLETED'),
(2, 1, 3, 75, 65, 'COMPLETED'),
(2, 2, 4, 60, 75, 'COMPLETED'),
(2, 3, 3, 85, 55, 'COMPLETED'),
(3, 1, 1, 90, 45, 'COMPLETED'),
(3, 2, 5, 80, 85, 'COMPLETED'),
(3, 3, 1, 90, 20, 'COMPLETED')
ON CONFLICT DO NOTHING;
