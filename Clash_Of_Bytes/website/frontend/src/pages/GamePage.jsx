import React from 'react';
import GameCanvas from '../components/GameCanvas.jsx';

export default function GamePage({ p1Char, p2Char, p1User, p2User, setActivePage }) {
  return (
    <div className="container">
      <div style={{ textAlign: 'center', marginBottom: '16px' }}>
        <h2 style={{ fontSize: '2rem', color: '#fff' }}>
          Combat <span style={{ color: 'var(--neon-cyan)' }}>Arena</span>
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Round 1 of 3 &bull; Fixed 60 FPS Engine &bull; AABB Collision Detection
        </p>
      </div>

      {/* Canvas */}
      <GameCanvas
        p1Char={p1Char}
        p2Char={p2Char}
        p1User={p1User}
        p2User={p2User}
        onMatchFinish={() => setActivePage('select')}
      />

      {/* Key Controls Guide */}
      <div
        style={{
          marginTop: '24px',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
          gap: '20px',
        }}
      >
        <div className="card">
          <h3 style={{ color: 'var(--neon-cyan)', fontSize: '1.1rem', marginBottom: '10px' }}>
            Player 1 Controls ({p1Char?.characterName || 'Fighter 1'})
          </h3>
          <div style={{ fontSize: '0.85rem', color: '#cbd5e1', lineHeight: '1.8' }}>
            <div>Movement: <strong style={{ color: '#fff' }}>[A]</strong> Left &bull; <strong style={{ color: '#fff' }}>[D]</strong> Right &bull; <strong style={{ color: '#fff' }}>[W]</strong> Jump</div>
            <div>Defense: <strong style={{ color: '#fff' }}>[S]</strong> Block Shield (Reduces damage)</div>
            <div>Attacks: <strong style={{ color: '#fff' }}>[F]</strong> Light Attack &bull; <strong style={{ color: '#fff' }}>[G]</strong> Heavy Slash</div>
            <div>Special Ability: <strong style={{ color: 'var(--neon-cyan)' }}>[R]</strong> {p1Char?.specialName || 'Special'}</div>
          </div>
        </div>

        <div className="card card-pink">
          <h3 style={{ color: 'var(--neon-pink)', fontSize: '1.1rem', marginBottom: '10px' }}>
            Player 2 Controls ({p2Char?.characterName || 'Fighter 2'})
          </h3>
          <div style={{ fontSize: '0.85rem', color: '#cbd5e1', lineHeight: '1.8' }}>
            <div>Movement: <strong style={{ color: '#fff' }}>[←]</strong> Left &bull; <strong style={{ color: '#fff' }}>[→]</strong> Right &bull; <strong style={{ color: '#fff' }}>[↑]</strong> Jump</div>
            <div>Defense: <strong style={{ color: '#fff' }}>[↓]</strong> Block Shield (Reduces damage)</div>
            <div>Attacks: <strong style={{ color: '#fff' }}>[J]</strong> Light Attack &bull; <strong style={{ color: '#fff' }}>[K]</strong> Heavy Slash</div>
            <div>Special Ability: <strong style={{ color: 'var(--neon-pink)' }}>[L]</strong> {p2Char?.specialName || 'Special'}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
