import React from 'react';
import CharacterCard from '../components/CharacterCard.jsx';

export default function CharacterSelectPage({ characters = [], p1Char, p2Char, setP1Char, setP2Char, setActivePage, p1User, p2User }) {
  const p1Name = p1User?.username || 'Player 1';
  const p2Name = p2User?.username || 'Player 2';

  const handleStartBattle = () => {
    setActivePage('play');
  };

  return (
    <div className="container">
      {/* Page Title & Matchup Banner */}
      <div style={{ textAlign: 'center', marginBottom: '32px' }}>
        <h2 style={{ fontSize: '2.4rem', color: '#fff', marginBottom: '8px' }}>
          Select Your <span style={{ color: 'var(--neon-cyan)' }}>Fighters</span>
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem' }}>
          Choose fighters for Player 1 and Player 2 before entering the arena
        </p>

        {/* Versus Matchup Card */}
        <div
          className="card"
          style={{
            marginTop: '24px',
            display: 'flex',
            justifyContent: 'space-around',
            alignItems: 'center',
            padding: '20px 30px',
            background: 'linear-gradient(135deg, rgba(0, 229, 255, 0.08), rgba(255, 42, 109, 0.08))',
          }}
        >
          {/* P1 Side */}
          <div style={{ textAlign: 'center', minWidth: '180px' }}>
            <span className="badge" style={{ background: 'rgba(0, 229, 255, 0.2)', color: 'var(--neon-cyan)', marginBottom: '6px' }}>
              P1 &bull; {p1Name}
            </span>
            <div style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 'bold' }}>
              {p1Char ? p1Char.characterName : 'None Selected'}
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--neon-cyan)' }}>
              {p1Char ? p1Char.archetype : 'Click "Pick P1" Below'}
            </div>
          </div>

          {/* VS Center */}
          <div style={{ fontSize: '2.5rem', fontWeight: 900, color: 'var(--neon-gold)', textShadow: '0 0 15px rgba(255, 204, 0, 0.7)' }}>
            VS
          </div>

          {/* P2 Side */}
          <div style={{ textAlign: 'center', minWidth: '180px' }}>
            <span className="badge" style={{ background: 'rgba(255, 42, 109, 0.2)', color: 'var(--neon-pink)', marginBottom: '6px' }}>
              P2 &bull; {p2Name}
            </span>
            <div style={{ fontSize: '1.4rem', color: '#fff', fontWeight: 'bold' }}>
              {p2Char ? p2Char.characterName : 'None Selected'}
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--neon-pink)' }}>
              {p2Char ? p2Char.archetype : 'Click "Pick P2" Below'}
            </div>
          </div>

          {/* Start Battle Action */}
          <div>
            <button
              className="btn btn-primary btn-lg"
              disabled={!p1Char || !p2Char}
              onClick={handleStartBattle}
              style={{
                opacity: (!p1Char || !p2Char) ? 0.4 : 1,
                cursor: (!p1Char || !p2Char) ? 'not-allowed' : 'pointer',
              }}
            >
              Start Battle &rarr;
            </button>
          </div>
        </div>
      </div>

      {/* Roster 6 Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '24px' }}>
        {characters.map((char) => (
          <CharacterCard
            key={char.characterId}
            character={char}
            selectedP1={p1Char}
            selectedP2={p2Char}
            onSelectP1={(c) => setP1Char(c)}
            onSelectP2={(c) => setP2Char(c)}
          />
        ))}
      </div>
    </div>
  );
}
