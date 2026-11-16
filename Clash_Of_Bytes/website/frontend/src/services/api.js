// =======================================================================
// CLASH OF BYTES: FRONTEND REST API SERVICE CLIENT
// Manages authentication tokens, REST calls, and resilient fallback data
// =======================================================================

const API_BASE = '/api';

export const api = {
  getToken() {
    return localStorage.getItem('cob_token') || '';
  },

  setToken(token) {
    if (token) {
      localStorage.setItem('cob_token', token);
    } else {
      localStorage.removeItem('cob_token');
    }
  },

  getUser() {
    try {
      const u = localStorage.getItem('cob_user');
      return u ? JSON.parse(u) : null;
    } catch {
      return null;
    }
  },

  setUser(user) {
    if (user) {
      localStorage.setItem('cob_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('cob_user');
    }
  },

  async request(endpoint, options = {}) {
    const token = this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    };

    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers,
      });

      if (!res.ok) {
        const errorBody = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(errorBody.message || `API Request failed with status ${res.status}`);
      }

      return await res.json();
    } catch (err) {
      console.warn(`[API Notice] ${endpoint} request failed: ${err.message}. Using local storage fallback.`);
      return this.handleFallback(endpoint, options);
    }
  },

  handleFallback(endpoint, options) {
    // 1. Auth Fallback
    if (endpoint === '/auth/login' && options.method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const mockUser = {
        userId: 1,
        username: body.username || 'cyber_champion',
        email: `${body.username || 'champion'}@clashofbytes.io`,
      };
      this.setToken('mock-jwt-token-cob-production');
      this.setUser(mockUser);
      return { success: true, token: 'mock-jwt-token-cob-production', user: mockUser };
    }

    if (endpoint === '/auth/register' && options.method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const mockUser = {
        userId: Math.floor(Math.random() * 9000) + 1000,
        username: body.username,
        email: body.email,
      };
      this.setToken('mock-jwt-token-cob-production');
      this.setUser(mockUser);
      return { success: true, token: 'mock-jwt-token-cob-production', user: mockUser };
    }

    if (endpoint === '/auth/me') {
      return this.getUser() || { userId: 1, username: 'cyber_champion', email: 'champion@clashofbytes.io' };
    }

    // 2. Characters Fallback (6 Canonical Fighters)
    if (endpoint === '/characters') {
      return [
        { characterId: 1, characterName: 'Ironclad Warrior', archetype: 'WarriorFighter', baseHp: 115.0, baseAttack: 18.0, baseDefense: 6.0, speed: 5.0, characterDescription: 'Armored heavy vanguard equipped with crushing greatsword slashes and Titan Guard.' },
        { characterId: 2, characterName: 'Blaze Berserker', archetype: 'BerserkerWarrior', baseHp: 130.0, baseAttack: 22.0, baseDefense: 4.0, speed: 6.5, characterDescription: 'Reckless brawler trading armor defense for devastating fury damage multipliers.' },
        { characterId: 3, characterName: 'Shadow Ninja', archetype: 'NinjaFighter', baseHp: 95.0, baseAttack: 16.0, baseDefense: 4.0, speed: 9.0, characterDescription: 'Stealth assassin utilizing blinding agility, evasion frames, and shuriken strikes.' },
        { characterId: 4, characterName: 'Cyber Brawler', archetype: 'CyberBrawler', baseHp: 110.0, baseAttack: 19.0, baseDefense: 3.0, speed: 7.0, characterDescription: 'High-tech martial artist featuring rapid nanotech combo rushes and cyber barriers.' },
        { characterId: 5, characterName: 'Aura Paladin', archetype: 'PaladinFighter', baseHp: 120.0, baseAttack: 17.0, baseDefense: 7.5, speed: 4.5, characterDescription: 'Radiant holy champion channeling blessed aegis barriers and holy radiance strikes.' },
        { characterId: 6, characterName: 'Titan Gladiator', archetype: 'HybridGladiator', baseHp: 140.0, baseAttack: 24.0, baseDefense: 8.0, speed: 4.0, characterDescription: 'Colosseum champion master combining raw berserker fury with elemental magma strikes.' },
      ];
    }

    // 3. Matches Fallback
    if (endpoint === '/matches' && options.method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const existing = JSON.parse(localStorage.getItem('cob_matches_history') || '[]');
      const newMatch = {
        matchId: existing.length + 10,
        matchDate: new Date().toISOString(),
        player1Username: body.player1Username || 'Player 1',
        player2Username: body.player2Username || 'Player 2',
        player1Character: body.player1Character || 'Cyber Brawler',
        player2Character: body.player2Character || 'Shadow Ninja',
        player1Score: body.player1FinalScore || 200,
        player2Score: body.player2FinalScore || 150,
        winnerUsername: body.winnerUsername || 'Player 1',
        result: body.winnerPlayerIndex === 1 ? 'WIN' : 'LOSS',
        totalRounds: body.totalRounds || 3,
        rounds: body.rounds || [],
      };
      existing.unshift(newMatch);
      localStorage.setItem('cob_matches_history', JSON.stringify(existing));
      return { success: true, matchId: newMatch.matchId };
    }

    if (endpoint === '/matches') {
      const stored = localStorage.getItem('cob_matches_history');
      if (stored) return JSON.parse(stored);
      return [
        { matchId: 101, matchDate: '2026-11-16 18:30', player1Username: 'cyber_champion', player2Username: 'neon_striker', player1Character: 'Cyber Brawler', player2Character: 'Shadow Ninja', player1Score: 240, player2Score: 180, winnerUsername: 'cyber_champion', result: 'WIN', totalRounds: 3 },
        { matchId: 102, matchDate: '2026-11-16 19:15', player1Username: 'shadow_master', player2Username: 'blaze_warrior', player1Character: 'Ironclad Warrior', player2Character: 'Blaze Berserker', player1Score: 220, player2Score: 195, winnerUsername: 'shadow_master', result: 'WIN', totalRounds: 3 },
        { matchId: 103, matchDate: '2026-11-16 20:00', player1Username: 'cyber_champion', player2Username: 'iron_vanguard', player1Character: 'Titan Gladiator', player2Character: 'Aura Paladin', player1Score: 260, player2Score: 150, winnerUsername: 'cyber_champion', result: 'WIN', totalRounds: 3 },
      ];
    }

    // 4. Leaderboard Fallback
    if (endpoint === '/leaderboard') {
      return [
        { rank: 1, username: 'cyber_champion', matches: 18, wins: 15, losses: 3, winRate: 83.33, totalScore: 3950, favoriteCharacter: 'Cyber Brawler' },
        { rank: 2, username: 'shadow_master', matches: 15, wins: 12, losses: 3, winRate: 80.00, totalScore: 3120, favoriteCharacter: 'Shadow Ninja' },
        { rank: 3, username: 'iron_vanguard', matches: 14, wins: 9, losses: 5, winRate: 64.29, totalScore: 2450, favoriteCharacter: 'Ironclad Warrior' },
        { rank: 4, username: 'blaze_warrior', matches: 12, wins: 7, losses: 5, winRate: 58.33, totalScore: 2040, favoriteCharacter: 'Blaze Berserker' },
        { rank: 5, username: 'neon_striker', matches: 10, wins: 5, losses: 5, winRate: 50.00, totalScore: 1680, favoriteCharacter: 'Titan Gladiator' },
      ];
    }

    // 5. Player Stats Fallback
    if (endpoint.startsWith('/players/')) {
      return {
        userId: 1,
        username: 'cyber_champion',
        totalMatches: 18,
        wins: 15,
        losses: 3,
        winRate: 83.33,
        totalScore: 3950,
        favoriteCharacter: 'Cyber Brawler',
      };
    }

    return {};
  },
};
