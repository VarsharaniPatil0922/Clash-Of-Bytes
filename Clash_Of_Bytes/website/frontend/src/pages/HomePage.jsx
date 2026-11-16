import React from 'react';

export default function HomePage({ setActivePage, characters = [] }) {
  return (
    <div className="container">
      {/* Hero Section */}
      <section style={{ textAlign: 'center', padding: '50px 0 70px' }}>
        <div style={{ display: 'inline-block', padding: '6px 16px', background: 'rgba(0, 229, 255, 0.1)', border: '1px solid var(--border-cyan)', borderRadius: '20px', fontSize: '0.85rem', color: 'var(--neon-cyan)', marginBottom: '20px', letterSpacing: '1px' }}>
          &bull; 2D LOCAL MULTIPLAYER ARCADE ARENA &bull;
        </div>
        <h1 style={{ fontSize: '3.6rem', color: '#fff', lineHeight: 1.15, textShadow: '0 0 30px rgba(0, 229, 255, 0.4)', marginBottom: '20px' }}>
          CLASH OF <span style={{ color: 'var(--neon-cyan)' }}>BYTES</span>
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--text-muted)', maxWidth: '720px', margin: '0 auto 36px', lineHeight: 1.6 }}>
          Experience competitive 2D cyber combat in your browser. Master 6 high-tech fighters, unleash Gouraud-charged specials, and climb the global leaderboard.
        </p>

        <div style={{ display: 'flex', gap: '20px', justifyContent: 'center' }}>
          <button className="btn btn-primary btn-lg" onClick={() => setActivePage('play')}>
            Play Online Now
          </button>
          <button className="btn btn-secondary btn-lg" onClick={() => setActivePage('select')}>
            View Fighters
          </button>
        </div>
      </section>

      {/* Roster Showcase */}
      <section style={{ marginBottom: '60px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '24px' }}>
          <div>
            <h2 style={{ fontSize: '1.8rem', color: '#fff' }}>Fighter Roster</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>Select your cyber warrior for the arena</p>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={() => setActivePage('select')}>
            All 6 Fighters &rarr;
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px' }}>
          {characters.slice(0, 3).map((char) => (
            <div key={char.characterId} className="card">
              <h3 style={{ fontSize: '1.2rem', color: 'var(--neon-cyan)', marginBottom: '6px' }}>{char.characterName}</h3>
              <div style={{ fontSize: '0.8rem', color: 'var(--neon-gold)', marginBottom: '10px' }}>{char.archetype}</div>
              <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '16px' }}>{char.characterDescription}</p>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#cbd5e1' }}>
                <span>HP: <strong>{char.baseHp}</strong></span>
                <span>ATK: <strong>{char.baseAttack}</strong></span>
                <span>DEF: <strong>{char.baseDefense}</strong></span>
                <span>SPD: <strong>{char.speed}</strong></span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Feature Highlights */}
      <section style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px', marginBottom: '60px' }}>
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', color: 'var(--neon-cyan)', marginBottom: '10px' }}>60 FPS Fixed Engine</h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Built on a fixed-timestep loop with AABB collision detection, hitstun states, and Gouraud-inspired particle shaders.
          </p>
        </div>
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', color: 'var(--neon-pink)', marginBottom: '10px' }}>Local 2-Player Combat</h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Two players on the same keyboard with zero input latency. P1 uses WASD+FGR, while P2 commands Arrow Keys+JKL.
          </p>
        </div>
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', color: 'var(--neon-gold)', marginBottom: '10px' }}>Database Telemetry</h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            Match results, round-by-round telemetry, and player career statistics are committed to PostgreSQL or SQLite.
          </p>
        </div>
      </section>
    </div>
  );
}
