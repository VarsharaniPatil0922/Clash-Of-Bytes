-- =======================================================================
-- CLASH OF BYTES: 2D LOCAL MULTIPLAYER FIGHTING GAME
-- SUPABASE POSTGRESQL PRODUCTION DATABASE SCHEMA & RLS POLICIES
-- Target: Supabase PostgreSQL (Port 5432 / 6543)
-- Architecture: Supabase Auth (auth.users) + public.users (UUID)
-- =======================================================================

-- Enable UUID extension if required
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =======================================================================
-- 1. USERS PROFILE TABLE (Linked to auth.users via UUID)
-- =======================================================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =======================================================================
-- 2. CHARACTERS TABLE (Canonical 6 Fighter Roster)
-- =======================================================================
CREATE TABLE IF NOT EXISTS public.characters (
    character_id BIGSERIAL PRIMARY KEY,
    character_name VARCHAR(50) NOT NULL UNIQUE,
    character_description VARCHAR(255) NOT NULL,
    archetype VARCHAR(50) NOT NULL,
    base_hp DOUBLE PRECISION NOT NULL,
    base_attack DOUBLE PRECISION NOT NULL,
    base_defense DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION NOT NULL
);

-- =======================================================================
-- 3. MATCHES TABLE (UUID Player References)
-- =======================================================================
CREATE TABLE IF NOT EXISTS public.matches (
    match_id BIGSERIAL PRIMARY KEY,
    match_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    player1_user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    player2_user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    winner_user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    loser_user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    total_rounds INTEGER NOT NULL DEFAULT 3,
    player1_rounds_won INTEGER NOT NULL DEFAULT 0,
    player2_rounds_won INTEGER NOT NULL DEFAULT 0,
    match_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    player1_final_score INTEGER NOT NULL DEFAULT 0,
    player2_final_score INTEGER NOT NULL DEFAULT 0,
    match_duration_seconds INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_different_players CHECK (player1_user_id <> player2_user_id)
);

-- =======================================================================
-- 4. MATCH_PLAYERS TABLE (Per-Player Match Telemetry & Character Pick)
-- =======================================================================
CREATE TABLE IF NOT EXISTS public.match_players (
    match_player_id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES public.matches(match_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    character_id BIGINT NOT NULL REFERENCES public.characters(character_id) ON DELETE CASCADE,
    final_score INTEGER NOT NULL DEFAULT 0,
    rounds_won INTEGER NOT NULL DEFAULT 0,
    result VARCHAR(10) NOT NULL,
    CONSTRAINT chk_match_result CHECK (result IN ('WIN', 'LOSS', 'DRAW'))
);

-- =======================================================================
-- 5. MATCH_ROUNDS TABLE (Granular Round Performance Telemetry)
-- =======================================================================
CREATE TABLE IF NOT EXISTS public.match_rounds (
    round_id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES public.matches(match_id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL,
    winner_user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    loser_user_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    player1_score INTEGER NOT NULL DEFAULT 0,
    player2_score INTEGER NOT NULL DEFAULT 0,
    round_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    duration_seconds INTEGER NOT NULL DEFAULT 0
);

-- =======================================================================
-- 6. INDEXES FOR HIGH-PERFORMANCE LEADERBOARDS & MATCH ARCHIVES
-- =======================================================================
CREATE INDEX IF NOT EXISTS idx_users_username ON public.users(username);
CREATE INDEX IF NOT EXISTS idx_matches_p1 ON public.matches(player1_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_p2 ON public.matches(player2_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_winner ON public.matches(winner_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_loser ON public.matches(loser_user_id);
CREATE INDEX IF NOT EXISTS idx_matches_date ON public.matches(match_date DESC);
CREATE INDEX IF NOT EXISTS idx_match_players_match ON public.match_players(match_id);
CREATE INDEX IF NOT EXISTS idx_match_players_user ON public.match_players(user_id);
CREATE INDEX IF NOT EXISTS idx_match_players_char ON public.match_players(character_id);
CREATE INDEX IF NOT EXISTS idx_match_rounds_match ON public.match_rounds(match_id);

-- =======================================================================
-- 7. AUTOMATIC NEW USER PROFILE TRIGGER (auth.users -> public.users)
-- =======================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, username, created_at)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1)),
        NOW()
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- =======================================================================
-- 8. CANONICAL 6 FIGHTER ROSTER SEED DATA (Idempotent)
-- =======================================================================
INSERT INTO public.characters (character_id, character_name, character_description, archetype, base_hp, base_attack, base_defense, speed) VALUES
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

-- Synchronize characters sequence
SELECT setval('public.characters_character_id_seq', COALESCE((SELECT MAX(character_id) FROM public.characters), 1));

-- =======================================================================
-- 9. SUPABASE ROW LEVEL SECURITY (RLS) POLICIES
-- =======================================================================
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.characters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.match_players ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.match_rounds ENABLE ROW LEVEL SECURITY;

-- CHARACTERS: Public read-only for roster display
DROP POLICY IF EXISTS "Public characters read" ON public.characters;
CREATE POLICY "Public characters read" ON public.characters
    FOR SELECT USING (true);

-- USERS: Users can read and update only their own profile
DROP POLICY IF EXISTS "Users can read own profile" ON public.users;
CREATE POLICY "Users can read own profile" ON public.users
    FOR SELECT TO authenticated
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update own profile" ON public.users;
CREATE POLICY "Users can update own profile" ON public.users
    FOR UPDATE TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- MATCHES: Match participants can view and insert their own matches
DROP POLICY IF EXISTS "Participants can view matches" ON public.matches;
CREATE POLICY "Participants can view matches" ON public.matches
    FOR SELECT TO authenticated
    USING (auth.uid() = player1_user_id OR auth.uid() = player2_user_id);

DROP POLICY IF EXISTS "Participants can insert matches" ON public.matches;
CREATE POLICY "Participants can insert matches" ON public.matches
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = player1_user_id OR auth.uid() = player2_user_id);

-- MATCH_PLAYERS: Match participants can view and insert match player telemetry
DROP POLICY IF EXISTS "Participants can view match_players" ON public.match_players;
CREATE POLICY "Participants can view match_players" ON public.match_players
    FOR SELECT TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.matches m
            WHERE m.match_id = match_players.match_id
            AND (m.player1_user_id = auth.uid() OR m.player2_user_id = auth.uid())
        )
    );

DROP POLICY IF EXISTS "Participants can insert match_players" ON public.match_players;
CREATE POLICY "Participants can insert match_players" ON public.match_players
    FOR INSERT TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.matches m
            WHERE m.match_id = match_players.match_id
            AND (m.player1_user_id = auth.uid() OR m.player2_user_id = auth.uid())
        )
    );

-- MATCH_ROUNDS: Match participants can view and insert match round telemetry
DROP POLICY IF EXISTS "Participants can view match_rounds" ON public.match_rounds;
CREATE POLICY "Participants can view match_rounds" ON public.match_rounds
    FOR SELECT TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.matches m
            WHERE m.match_id = match_rounds.match_id
            AND (m.player1_user_id = auth.uid() OR m.player2_user_id = auth.uid())
        )
    );

DROP POLICY IF EXISTS "Participants can insert match_rounds" ON public.match_rounds;
CREATE POLICY "Participants can insert match_rounds" ON public.match_rounds
    FOR INSERT TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.matches m
            WHERE m.match_id = match_rounds.match_id
            AND (m.player1_user_id = auth.uid() OR m.player2_user_id = auth.uid())
        )
    );

-- =======================================================================
-- 10. ROLE-BASED ACCESS CONTROL GRANTS
-- =======================================================================
-- Revoke all public privileges by default
REVOKE ALL ON public.users FROM public, anon;
REVOKE ALL ON public.matches FROM public, anon;
REVOKE ALL ON public.match_players FROM public, anon;
REVOKE ALL ON public.match_rounds FROM public, anon;

-- Grant characters read access to public (anon + authenticated)
GRANT SELECT ON public.characters TO anon, authenticated;

-- Grant profile access to authenticated users (governed by RLS)
GRANT SELECT, UPDATE ON public.users TO authenticated;

-- Grant match access to authenticated users (governed by RLS)
GRANT SELECT, INSERT ON public.matches TO authenticated;
GRANT SELECT, INSERT ON public.match_players TO authenticated;
GRANT SELECT, INSERT ON public.match_rounds TO authenticated;

-- Grant sequence usage for auto-incrementing serial primary keys
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO authenticated;
