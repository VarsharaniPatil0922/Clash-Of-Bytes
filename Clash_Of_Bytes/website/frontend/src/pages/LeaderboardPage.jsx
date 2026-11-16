import React, { useEffect, useState } from 'react';
import { api } from '../services/api.js';

export default function LeaderboardPage() {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadLeaderboard() {
      try {
        const data = await api.request('/leaderboard');
        setLeaderboard(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadLeaderboard();
  }, []);

  const getRankBadge = (rank) => {
    if (rank === 1) return <span style={{ color: '#ffd700', fontWeight: 'bold' }}>&#129351; 1ST</span>;
    if (rank === 2) return <span style={{ color: '#c0c0c0', fontWeight: 'bold' }}>&#129352; 2ND</span>;
    if (rank === 3) return <span style={{ color: '#cd7f32', fontWeight: 'bold' }}>&#129353; 3RD</span>;
    return <span style={{ color: '#94a3b8' }}>#{rank}</span>;
  };

  return (
    <div className="container">
      <div style={{ textAlign: 'center', marginBottom: '32px' }}>
        <h2 style={{ fontSize: '2.4rem', color: '#fff' }}>
          Global <span style={{ color: 'var(--neon-gold)' }}>Leaderboard</span>
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          The highest ranked warriors across all servers and tournaments
        </p>
      </div>

      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Rank</th>
              <th>Player</th>
              <th>Matches</th>
              <th>Wins</th>
              <th>Losses</th>
              <th>Win Rate</th>
              <th>Favorite Fighter</th>
              <th>Career Points</th>
            </tr>
          </thead>
          <tbody>
            {leaderboard.map((player, idx) => (
              <tr key={player.username || idx}>
                <td>{getRankBadge(idx + 1)}</td>
                <td style={{ fontWeight: 'bold', color: '#fff' }}>
                  {player.username}
                </td>
                <td>{player.matches}</td>
                <td style={{ color: 'var(--neon-green)', fontWeight: 'bold' }}>{player.wins}</td>
                <td style={{ color: 'var(--neon-pink)' }}>{player.losses}</td>
                <td>
                  <span style={{ fontWeight: 'bold', color: player.winRate >= 70 ? 'var(--neon-green)' : 'var(--neon-gold)' }}>
                    {player.winRate}%
                  </span>
                </td>
                <td style={{ color: 'var(--neon-cyan)' }}>{player.favoriteCharacter}</td>
                <td style={{ fontWeight: 'bold', color: '#fff' }}>{player.totalScore}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
