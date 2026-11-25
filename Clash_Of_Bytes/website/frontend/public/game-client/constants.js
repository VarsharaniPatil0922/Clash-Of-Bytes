// =======================================================================
// CLASH OF BYTES: 2D COMBAT GAME CONSTANTS & INPUT MAPPINGS
// Exact Keyboard Controls & Arena Configuration
// =======================================================================

export const ARENA = {
  WIDTH: 960,
  HEIGHT: 540,
  GROUND_Y: 460,
  ROUND_TIME_SECONDS: 60,
  ROUNDS_TO_WIN: 2,
  GRAVITY: 0.65,
};

/**
 * Exact Keyboard Control Specifications (Task 3 Standardized)
 * Player 1:
 *   [A] Move Left
 *   [D] Move Right
 *   [W] Jump
 *   [F] Attack
 *   [G] Defence (Block / Shield)
 *
 * Player 2:
 *   [ArrowLeft] Move Left
 *   [ArrowRight] Move Right
 *   [ArrowUp] Jump
 *   [KeyK] Attack
 *   [KeyL] Defence (Block / Shield)
 */
export const INPUT_MAPPINGS = {
  PLAYER_1: {
    LEFT: ['KeyA'],
    RIGHT: ['KeyD'],
    JUMP: ['KeyW'],
    ATTACK: ['KeyF'],
    DEFENCE: ['KeyG'],
    // Backward-compatibility aliases for legacy engine actions
    BLOCK: ['KeyG'],
    LIGHT_ATTACK: ['KeyF'],
    HEAVY_ATTACK: ['KeyF'],
    SPECIAL: ['KeyF'],
  },
  PLAYER_2: {
    LEFT: ['ArrowLeft'],
    RIGHT: ['ArrowRight'],
    JUMP: ['ArrowUp'],
    ATTACK: ['KeyK'],
    DEFENCE: ['KeyL'],
    // Backward-compatibility aliases for legacy engine actions
    BLOCK: ['KeyL'],
    LIGHT_ATTACK: ['KeyK'],
    HEAVY_ATTACK: ['KeyK'],
    SPECIAL: ['KeyK'],
  },
};

export const CHARACTERS = {
  1: {
    id: 1,
    name: 'Ironclad Warrior',
    archetype: 'WarriorFighter',
    baseHp: 115,
    baseAttack: 18,
    baseDefense: 6,
    speed: 5,
    color: '#00e5ff',
    accentColor: '#0055ff',
    width: 65,
    height: 105,
    specialCooldown: 4.0,
    characterDescription: 'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.',
  },
  2: {
    id: 2,
    name: 'Blaze Berserker',
    archetype: 'BerserkerWarrior',
    baseHp: 130,
    baseAttack: 22,
    baseDefense: 4,
    speed: 6.5,
    color: '#ff5500',
    accentColor: '#ff2200',
    width: 60,
    height: 100,
    specialCooldown: 3.5,
    characterDescription: 'Reckless brawler trading armor defense for devastating fury damage multipliers.',
  },
  3: {
    id: 3,
    name: 'Shadow Ninja',
    archetype: 'NinjaFighter',
    baseHp: 95,
    baseAttack: 16,
    baseDefense: 4,
    speed: 9,
    color: '#a855f7',
    accentColor: '#6366f1',
    width: 50,
    height: 95,
    specialCooldown: 3.0,
    characterDescription: 'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.',
  },
  4: {
    id: 4,
    name: 'Cyber Brawler',
    archetype: 'CyberBrawler',
    baseHp: 110,
    baseAttack: 19,
    baseDefense: 3,
    speed: 7,
    color: '#00ff88',
    accentColor: '#00aa55',
    width: 55,
    height: 100,
    specialCooldown: 3.5,
    characterDescription: 'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.',
  },
  5: {
    id: 5,
    name: 'Aura Paladin',
    archetype: 'PaladinFighter',
    baseHp: 120,
    baseAttack: 17,
    baseDefense: 7.5,
    speed: 4.5,
    color: '#ffcc00',
    accentColor: '#ff8800',
    width: 65,
    height: 105,
    specialCooldown: 4.5,
    characterDescription: 'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.',
  },
  6: {
    id: 6,
    name: 'Titan Gladiator',
    archetype: 'HybridGladiator',
    baseHp: 140,
    baseAttack: 24,
    baseDefense: 8,
    speed: 4,
    color: '#ff2a6d',
    accentColor: '#990033',
    width: 70,
    height: 115,
    specialCooldown: 5.0,
    characterDescription: 'Colosseum champion master combining raw berserker fury with elemental magma strikes.',
  },
};
