import React from 'react';

export default function CharacterCard({ character, selectedP1, selectedP2, onSelectP1, onSelectP2 }) {
  const isP1 = selectedP1?.characterId === character.characterId;
  const isP2 = selectedP2?.characterId === character.characterId;

  const hpPercent = Math.min(100, (character.baseHp / 150) * 100);
  const atkPercent = Math.min(100, (character.baseAttack / 30) * 100);
  const defPercent = Math.min(100, (character.baseDefense / 15) * 100);
  const spdPercent = Math.min(100, (character.speed / 12) * 100);

  return (
    <div
      className={`card ${isP1 ? '' : isP2 ? 'card-pink' : ''}`}
      style={{
        boxShadow: isP1
          ? '0 0 20px rgba(0, 229, 255, 0.5)'
          : isP2
          ? '0 0 20px rgba(255, 42, 109, 0.5)'
          : 'none',
        borderColor: isP1 ? 'var(--neon-cyan)' : isP2 ? 'var(--neon-pink)' : undefined,
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
        <h3 style={{ fontSize: '1.15rem', color: isP1 ? 'var(--neon-cyan)' : isP2 ? 'var(--neon-pink)' : '#fff' }}>
          {character.characterName}
        </h3>
        <span className="badge" style={{ background: '#1e293b', color: '#94a3b8' }}>
          {character.archetype}
        </span>
      </div>

      <p style={{ fontSize: '0.85rem', color: '#94a3b8', minHeight: '44px', marginBottom: '14px' }}>
        {character.characterDescription}
      </p>

      {/* Stats */}
      <div style={{ marginBottom: '16px' }}>
        <div className="stat-row">
          <span className="stat-name">HP</span>
          <div className="stat-bar-bg">
            <div className="stat-bar-fill" style={{ width: `${hpPercent}%`, background: '#00ff88' }}></div>
          </div>
          <span className="stat-val">{character.baseHp}</span>
        </div>

        <div className="stat-row">
          <span className="stat-name">ATK</span>
          <div className="stat-bar-bg">
            <div className="stat-bar-fill" style={{ width: `${atkPercent}%`, background: '#ff2a6d' }}></div>
          </div>
          <span className="stat-val">{character.baseAttack}</span>
        </div>

        <div className="stat-row">
          <span className="stat-name">DEF</span>
          <div className="stat-bar-bg">
            <div className="stat-bar-fill" style={{ width: `${defPercent}%`, background: '#ffcc00' }}></div>
          </div>
          <span className="stat-val">{character.baseDefense}</span>
        </div>

        <div className="stat-row">
          <span className="stat-name">SPD</span>
          <div className="stat-bar-bg">
            <div className="stat-bar-fill" style={{ width: `${spdPercent}%`, background: '#00e5ff' }}></div>
          </div>
          <span className="stat-val">{character.speed}</span>
        </div>
      </div>

      {/* Selection Buttons */}
      <div style={{ display: 'flex', gap: '8px' }}>
        <button
          className={`btn btn-sm ${isP1 ? 'btn-primary' : 'btn-secondary'}`}
          style={{ flex: 1 }}
          onClick={() => onSelectP1(character)}
        >
          {isP1 ? 'P1 PICKED' : 'PICK P1'}
        </button>
        <button
          className={`btn btn-sm ${isP2 ? 'btn-danger' : 'btn-secondary'}`}
          style={{ flex: 1 }}
          onClick={() => onSelectP2(character)}
        >
          {isP2 ? 'P2 PICKED' : 'PICK P2'}
        </button>
      </div>
    </div>
  );
}
