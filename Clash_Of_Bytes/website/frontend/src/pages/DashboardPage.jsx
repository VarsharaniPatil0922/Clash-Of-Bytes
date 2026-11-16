import React, { useEffect, useState } from 'react';
import { api } from '../services/api.js';

export default function DashboardPage({ user, setActivePage }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchStats() {
      if (!user) return;
      try {
        const data = await api.request(`/players/${user.userId || 1}/stats`);
        setStats(data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchStats();
  }, [user]);

  if (!user) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '60px 0' }}>
        <h2>Please Sign In to Access Your Dashboard</h2>
        <button className="btn btn-primary" style={{ marginTop: '20px' }} onClick={() => setActivePage('login')}>
          Go to Sign In
        </button>
      </div>
    );
  }

  return (
    <div className="container">
      {/* Header Banner */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h2 style={{ fontSize: '2.2rem', color: '#fff' }}>
            Player <span style={{ color: 'var(--neon-cyan)' }}>Dashboard</span>
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Welcome back, <strong style={{ color: '#fff' }}>{user.username}</strong> ({user.email})
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => setActivePage('play')}>
          Enter Arena &rarr;
        </button>
      </div>

      {/* Career Metric Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '36px' }}>
        <div className="card" style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>Total Matches</div>
          <div style={{ fontSize: '2.4rem', fontWeight: 900, color: '#fff' }}>
            {stats ? stats.totalMatches : 18}
          </div>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>Victories</div>
          <div style={{ fontSize: '2.4rem', fontWeight: 900, color: 'var(--neon-green)' }}>
            {stats ? stats.wins : 15}
          </div>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>Defeats</div>
          <div style={{ fontSize: '2.4rem', fontWeight: 900, color: 'var(--neon-pink)' }}>
            {stats ? stats.losses : 3}
          </div>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>Win Rate</div>
          <div style={{ fontSize: '2.4rem', fontWeight: 900, color: 'var(--neon-gold)' }}>
            {stats ? `${stats.winRate}%` : '83.3%'}
          </div>
        </div>

        <div className="card" style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>Favorite Fighter</div>
          <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--neon-cyan)', marginTop: '8px' }}>
            {stats ? stats.favoriteCharacter : 'Cyber Brawler'}
          </div>
        </div>
      </div>

      {/* Quick Launch Shortcuts */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px' }}>
        <div className="card">
          <h3 style={{ fontSize: '1.2rem', color: 'var(--neon-cyan)', marginBottom: '10px' }}>Battle Telemetry</h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '16px' }}>
            View your complete fight records, including round scores, opponent matchups, and outcome telemetry.
          </p>
          <button className="btn btn-secondary btn-sm" onClick={() => setActivePage('history')}>
            View Match History &rarr;
          </button>
        </div>

        <div className="card">
          <h3 style={{ fontSize: '1.2rem', color: 'var(--neon-gold)', marginBottom: '10px' }}>Tournament Rankings</h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '16px' }}>
            Compare your career victory count and win percentage against the top players on the server.
          </p>
          <button className="btn btn-secondary btn-sm" onClick={() => setActivePage('leaderboard')}>
            View Global Leaderboard &rarr;
          </button>
        </div>
      </div>
    </div>
  );
}
