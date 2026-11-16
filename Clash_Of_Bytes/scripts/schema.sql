-- =======================================================================
-- CLASH OF BYTES: 2D LOCAL MULTIPLAYER FIGHTING GAME
-- DATABASE SCHEMA: AUTHENTICATION, CHARACTERS & MATCH HISTORY
-- Compatible with SQLite, PostgreSQL, and MySQL
-- =======================================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CHARACTERS TABLE
CREATE TABLE IF NOT EXISTS characters (
    character_id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_name VARCHAR(50) NOT NULL UNIQUE,
    character_description VARCHAR(255) NOT NULL,
    archetype VARCHAR(50) NOT NULL,
    base_hp REAL NOT NULL,
    base_attack REAL NOT NULL,
    base_defense REAL NOT NULL,
    speed REAL NOT NULL
);

-- 3. MATCHES TABLE
CREATE TABLE IF NOT EXISTS matches (
    match_id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    player1_user_id INTEGER NOT NULL,
    player2_user_id INTEGER NOT NULL,
    winner_user_id INTEGER, -- NULL in case of DRAW
    total_rounds INTEGER NOT NULL,
    match_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, ABANDONED
    player1_final_score INTEGER NOT NULL,
    player2_final_score INTEGER NOT NULL,
    FOREIGN KEY (player1_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (player2_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 4. MATCH_PLAYERS TABLE
CREATE TABLE IF NOT EXISTS match_players (
    match_player_id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    final_score INTEGER NOT NULL,
    result VARCHAR(10) NOT NULL, -- WIN, LOSS, DRAW
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (character_id) REFERENCES characters(character_id) ON DELETE CASCADE,
    CONSTRAINT chk_match_result CHECK (result IN ('WIN', 'LOSS', 'DRAW'))
);

-- 5. MATCH_ROUNDS TABLE
CREATE TABLE IF NOT EXISTS match_rounds (
    round_id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id INTEGER NOT NULL,
    round_number INTEGER NOT NULL,
    winner_user_id INTEGER, -- NULL if round was a draw
    player1_score INTEGER NOT NULL,
    player2_score INTEGER NOT NULL,
    round_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- INDEXES FOR OPTIMAL QUERY PERFORMANCE
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_matches_p1 ON matches(player1_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_p2 ON matches(player2_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_winner ON matches(winner_user_id);
CREATE INDEX IF NOT EXISTS idx_match_players_match ON match_players(match_id);
CREATE INDEX IF NOT EXISTS idx_match_players_user ON match_players(user_id);
CREATE INDEX IF NOT EXISTS idx_match_rounds_match ON match_rounds(match_id);

-- PRE-POPULATE 6 FIGHTERS ROSTER IF EMPTY
INSERT OR IGNORE INTO characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES
(1, 'Ironclad Warrior', 'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.', 'WarriorFighter', 115.0, 18.0, 6.0, 5.0),
(2, 'Blaze Berserker', 'Reckless brawler trading armor defense for devastating fury damage multipliers.', 'BerserkerWarrior', 130.0, 22.0, 4.0, 6.5),
(3, 'Shadow Ninja', 'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.', 'NinjaFighter', 95.0, 16.0, 4.0, 9.0),
(4, 'Cyber Brawler', 'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.', 'CyberBrawler', 110.0, 19.0, 3.0, 7.0),
(5, 'Aura Paladin', 'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.', 'PaladinFighter', 120.0, 17.0, 7.5, 4.5),
(6, 'Titan Gladiator', 'Colosseum champion master combining raw berserker fury with elemental magma strikes.', 'HybridGladiator', 140.0, 24.0, 8.0, 4.0);
