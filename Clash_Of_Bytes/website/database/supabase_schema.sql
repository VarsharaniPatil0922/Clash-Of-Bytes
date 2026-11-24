-- =======================================================================
--   CLASH OF BYTES : SUPABASE CLOUD DATABASE SCHEMA
--   Project Reference : vhxbbfoykbdbypylsapr
-- =======================================================================
--
-- HOW TO RUN THIS IN SUPABASE:
-- 1. Open your Supabase Dashboard:
--    https://supabase.com/dashboard/project/vhxbbfoykbdbypylsapr/sql/new
-- 2. Click "SQL Editor" on the left menu -> Click "New query"
-- 3. Copy and paste all the SQL commands below into the editor.
-- 4. Click the green "RUN" button (or press Ctrl+Enter).
-- =======================================================================

-- 1. CLEANUP (Drop existing tables/triggers if re-running)
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user();

-- 2. CREATE 'profiles' TABLE (Linked to Supabase Auth Users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    matches INTEGER DEFAULT 0,
    highest_damage NUMERIC(8, 2) DEFAULT 0.0,
    favorite_fighter TEXT DEFAULT 'Ironclad Warrior',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. AUTOMATIC TRIGGER: Auto-creates a profile row when a user signs up in Java Swing
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, email)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', SPLIT_PART(NEW.email, '@', 1)),
        NEW.email
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- 4. CREATE 'match_history' TABLE (Stores match records matching Game Launcher Table layout)
-- Columns exactly match the Game Table:
-- [Slot #] [Match Date & Time] [Player 1] [P1 Fighter] [Player 2] [P2 Fighter] [Score / Rounds] [Winner] [Outcome]
CREATE TABLE IF NOT EXISTS public.match_history (
    match_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    played_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    player_1 TEXT NOT NULL,
    p1_fighter TEXT NOT NULL,
    player_2 TEXT NOT NULL,
    p2_fighter TEXT NOT NULL,
    score TEXT NOT NULL,
    winner TEXT NOT NULL,
    outcome TEXT NOT NULL,
    player_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    player_name TEXT,
    result TEXT,
    rounds_p1 INTEGER DEFAULT 0,
    rounds_p2 INTEGER DEFAULT 0
);

-- Quick Schema Migration: Add columns to existing table if already created in Supabase
ALTER TABLE public.match_history ADD COLUMN IF NOT EXISTS player_1 TEXT;
ALTER TABLE public.match_history ADD COLUMN IF NOT EXISTS player_2 TEXT;
ALTER TABLE public.match_history ADD COLUMN IF NOT EXISTS score TEXT;
ALTER TABLE public.match_history ADD COLUMN IF NOT EXISTS outcome TEXT;

-- Backfill any existing rows so they show up cleanly in the table editor
UPDATE public.match_history
SET 
    player_1 = COALESCE(player_1, player_name),
    player_2 = COALESCE(player_2, CASE WHEN winner LIKE player_name || '%' THEN p2_fighter ELSE winner END),
    score = COALESCE(score, rounds_p1 || ' - ' || rounds_p2),
    outcome = COALESCE(outcome, CASE WHEN result = 'WIN' THEN player_name || ' WON' ELSE winner || ' WON' END)
WHERE player_1 IS NULL;

-- 5. ENABLE ROW LEVEL SECURITY (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.match_history ENABLE ROW LEVEL SECURITY;

-- 6. RLS POLICIES (Allows Java Swing application full read/write via anon key)
DROP POLICY IF EXISTS "Public full access to profiles" ON public.profiles;
CREATE POLICY "Public full access to profiles"
    ON public.profiles
    FOR ALL
    USING (true)
    WITH CHECK (true);

DROP POLICY IF EXISTS "Public full access to match_history" ON public.match_history;
CREATE POLICY "Public full access to match_history"
    ON public.match_history
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- 7. PERFORMANCE INDEXES
CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);
CREATE INDEX IF NOT EXISTS idx_match_history_player ON public.match_history(player_name);
CREATE INDEX IF NOT EXISTS idx_match_history_played_at ON public.match_history(played_at DESC);

-- 8. AUTOMATIC STATS TRIGGER: Auto-updates 'profiles' (wins, losses, matches) when match is recorded
CREATE OR REPLACE FUNCTION public.update_profile_stats_on_match()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.profiles
    SET 
        matches = COALESCE(matches, 0) + 1,
        wins = COALESCE(wins, 0) + CASE WHEN NEW.result = 'WIN' THEN 1 ELSE 0 END,
        losses = COALESCE(losses, 0) + CASE WHEN NEW.result = 'LOSS' THEN 1 ELSE 0 END,
        updated_at = NOW()
    WHERE (NEW.player_id IS NOT NULL AND id = NEW.player_id)
       OR (username = NEW.player_name);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_match_history_inserted ON public.match_history;
CREATE TRIGGER on_match_history_inserted
    AFTER INSERT ON public.match_history
    FOR EACH ROW EXECUTE PROCEDURE public.update_profile_stats_on_match();

-- =======================================================================
-- VERIFICATION QUERY:
-- Run this query after running the script above to verify the tables exist:
--
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- =======================================================================
