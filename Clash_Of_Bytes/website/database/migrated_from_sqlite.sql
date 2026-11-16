-- Auto-generated SQLite to PostgreSQL Data Dump
-- Clash of Bytes Migration Tool

BEGIN TRANSACTION;

INSERT INTO users (user_id, username, email, password_hash, salt, created_at) VALUES (1, 'shadow_master_9698', 'shadow9698@clashofbytes.edu', 'e91f43bfd42245073c82d13d9ce25a82d258b76d6360f1af6cea6ca8b5642795', '53e10178c9b13cba96f2f55436935abb', '2026-11-16 17:39:49') ON CONFLICT (user_id) DO NOTHING;
INSERT INTO users (user_id, username, email, password_hash, salt, created_at) VALUES (2, 'blaze_warrior_9698', 'blaze9698@clashofbytes.edu', '635123e7875a2acae679d2ab4cf24605af204469a0cb59d8c4b5d239952c62b9', '667c1b234d993441fd58ebe018f11787', '2026-11-16 17:39:49') ON CONFLICT (user_id) DO NOTHING;
INSERT INTO users (user_id, username, email, password_hash, salt, created_at) VALUES (3, 'shadow_master_8786', 'shadow8786@clashofbytes.edu', '6664d11c12db1305694cba887db7b981b8c87be3103e3d7ceb0d8eb3af76e68c', 'c66069126a8c60cdb84853811f352292', '2026-11-16 17:52:58') ON CONFLICT (user_id) DO NOTHING;
INSERT INTO users (user_id, username, email, password_hash, salt, created_at) VALUES (4, 'blaze_warrior_8786', 'blaze8786@clashofbytes.edu', '30ee28fbd796e1b312f8532baed179eedc32bad30da2aefed980df3844017f90', '57d3d93bfd6caf0564f66f21f71e4d19', '2026-11-16 17:52:58') ON CONFLICT (user_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (1, 'Ironclad Warrior', 'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.', 'WarriorFighter', 115.0, 18.0, 6.0, 5.0) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (2, 'Blaze Berserker', 'Reckless brawler trading armor defense for devastating fury damage multipliers.', 'BerserkerWarrior', 130.0, 22.0, 4.0, 6.5) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (3, 'Shadow Ninja', 'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.', 'NinjaFighter', 95.0, 16.0, 4.0, 9.0) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (4, 'Cyber Brawler', 'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.', 'CyberBrawler', 110.0, 19.0, 3.0, 7.0) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (5, 'Aura Paladin', 'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.', 'PaladinFighter', 120.0, 17.0, 7.5, 4.5) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES (6, 'Titan Gladiator', 'Colosseum champion master combining raw berserker fury with elemental magma strikes.', 'HybridGladiator', 140.0, 24.0, 8.0, 4.0) ON CONFLICT (character_id) DO NOTHING;
INSERT INTO matches (match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score) VALUES (1, '2026-11-16 17:39:49', 1, 2, 1, 3, 'COMPLETED', 220, 195) ON CONFLICT (match_id) DO NOTHING;
INSERT INTO matches (match_id, match_date, player1_user_id, player2_user_id, winner_user_id, total_rounds, match_status, player1_final_score, player2_final_score) VALUES (2, '2026-11-16 17:52:58', 3, 4, 3, 3, 'COMPLETED', 220, 195) ON CONFLICT (match_id) DO NOTHING;
INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, result) VALUES (1, 1, 1, 1, 220, 'WIN') ON CONFLICT DO NOTHING;
INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, result) VALUES (2, 1, 2, 3, 195, 'LOSS') ON CONFLICT DO NOTHING;
INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, result) VALUES (3, 2, 3, 1, 220, 'WIN') ON CONFLICT DO NOTHING;
INSERT INTO match_players (match_player_id, match_id, user_id, character_id, final_score, result) VALUES (4, 2, 4, 3, 195, 'LOSS') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (1, 1, 1, 1, 85, 45, 'COMPLETED') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (2, 1, 2, 2, 40, 90, 'COMPLETED') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (3, 1, 3, 1, 95, 60, 'COMPLETED') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (4, 2, 1, 3, 85, 45, 'COMPLETED') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (5, 2, 2, 4, 40, 90, 'COMPLETED') ON CONFLICT DO NOTHING;
INSERT INTO match_rounds (round_id, match_id, round_number, winner_user_id, player1_score, player2_score, round_status) VALUES (6, 2, 3, 3, 95, 60, 'COMPLETED') ON CONFLICT DO NOTHING;

-- Update sequences
SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1));
SELECT setval('characters_character_id_seq', COALESCE((SELECT MAX(character_id) FROM characters), 1));
SELECT setval('matches_match_id_seq', COALESCE((SELECT MAX(match_id) FROM matches), 1));
SELECT setval('match_players_match_player_id_seq', COALESCE((SELECT MAX(match_player_id) FROM match_players), 1));
SELECT setval('match_rounds_round_id_seq', COALESCE((SELECT MAX(round_id) FROM match_rounds), 1));

COMMIT;
