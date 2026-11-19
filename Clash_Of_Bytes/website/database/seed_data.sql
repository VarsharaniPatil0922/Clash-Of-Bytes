-- =======================================================================
-- CLASH OF BYTES: 2D LOCAL MULTIPLAYER FIGHTING GAME
-- SEED DATA: CANONICAL 6 FIGHTER ROSTER
-- Target: Supabase PostgreSQL (Port 5432 / 6543)
-- Architecture: Idempotent Character Seeding (Zero Mock Users)
-- =======================================================================

-- 1. INSERT CANONICAL 6 FIGHTERS
INSERT INTO public.characters (
    character_id,
    character_name,
    character_description,
    archetype,
    base_hp,
    base_attack,
    base_defense,
    speed
) VALUES
(
    1,
    'Ironclad Warrior',
    'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.',
    'WarriorFighter',
    115.0,
    18.0,
    6.0,
    5.0
),
(
    2,
    'Blaze Berserker',
    'Reckless brawler trading armor defense for devastating fury damage multipliers.',
    'BerserkerWarrior',
    130.0,
    22.0,
    4.0,
    6.5
),
(
    3,
    'Shadow Ninja',
    'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.',
    'NinjaFighter',
    95.0,
    16.0,
    4.0,
    9.0
),
(
    4,
    'Cyber Brawler',
    'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.',
    'CyberBrawler',
    110.0,
    19.0,
    3.0,
    7.0
),
(
    5,
    'Aura Paladin',
    'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.',
    'PaladinFighter',
    120.0,
    17.0,
    7.5,
    4.5
),
(
    6,
    'Titan Gladiator',
    'Colosseum champion master combining raw berserker fury with elemental magma strikes.',
    'HybridGladiator',
    140.0,
    24.0,
    8.0,
    4.0
)
ON CONFLICT (character_id) DO UPDATE SET
    character_name = EXCLUDED.character_name,
    character_description = EXCLUDED.character_description,
    archetype = EXCLUDED.archetype,
    base_hp = EXCLUDED.base_hp,
    base_attack = EXCLUDED.base_attack,
    base_defense = EXCLUDED.base_defense,
    speed = EXCLUDED.speed;

-- 2. RESET SEQUENCE TO RESUME AFTER ID 6
SELECT setval(
    'public.characters_character_id_seq',
    COALESCE((SELECT MAX(character_id) FROM public.characters), 1)
);
