import React, { useEffect, useState } from 'react';
import { api } from '../services/api.js';

export default function MatchHistoryPage() {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterResult, setFilterResult] = useState('ALL');
  const [selectedMatch, setSelectedMatch] = useState(null);

  useEffect(() => {
    async function loadMatches() {
      try {
        const data = await api.request('/matches');
        setMatches(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadMatches();
  }, []);

  const filteredMatches = matches.filter((m) => {
    const textMatch =
      m.player1Username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.player2Username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.player1Character?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      m.player2Character?.toLowerCase().includes(searchTerm.toLowerCase());

    if (!textMatch) return false;
    if (filterResult === 'ALL') return true;
    return m.result === filterResult;
  });

  return (
    <div className="container">
      {/* Title */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h2 style={{ fontSize: '2.2rem', color: '#fff' }}>
            Match <span style={{ color: 'var(--neon-cyan)' }}>History</span>
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            Telemetry logs and combat records from previous clashes
          </p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '20px', flexWrap: 'wrap' }}>
        <input
          type="text"
          className="form-input"
          style={{ maxWidth: '340px' }}
          placeholder="Search by player or character..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <div style={{ display: 'flex', gap: '8px' }}>
          {['ALL', 'WIN', 'LOSS'].map((filter) => (
            <button
              key={filter}
              className={`btn btn-sm ${filterResult === filter ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setFilterResult(filter)}
            >
              {filter}
            </button>
          ))}
        </div>
      </div>

      {/* JTable Web Equivalent Data Table */}
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Date & Time</th>
              <th>Player 1 (Left)</th>
              <th>Player 2 (Right)</th>
              <th>Final Score</th>
              <th>Winner</th>
              <th>Outcome</th>
              <th>Telemetry</th>
            </tr>
          </thead>
          <tbody>
            {filteredMatches.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                  No match records found.
                </td>
              </tr>
            ) : (
              filteredMatches.map((m) => (
                <tr key={m.matchId}>
                  <td style={{ fontWeight: 'bold', color: 'var(--neon-cyan)' }}>#{m.matchId}</td>
                  <td>{new Date(m.matchDate).toLocaleDateString()} {new Date(m.matchDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                  <td>
                    <strong>{m.player1Username}</strong>
                    <div style={{ fontSize: '0.8rem', color: '#94a3b8' }}>{m.player1Character}</div>
                  </td>
                  <td>
                    <strong>{m.player2Username}</strong>
                    <div style={{ fontSize: '0.8rem', color: '#94a3b8' }}>{m.player2Character}</div>
                  </td>
                  <td style={{ fontWeight: 'bold' }}>
                    {m.player1Score} - {m.player2Score}
                  </td>
                  <td style={{ color: 'var(--neon-gold)', fontWeight: 'bold' }}>{m.winnerUsername}</td>
                  <td>
                    <span className={`badge ${m.result === 'WIN' ? 'badge-win' : 'badge-loss'}`}>
                      {m.result}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-secondary btn-sm" onClick={() => setSelectedMatch(m)}>
                      Details
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Round Telemetry Modal */}
      {selectedMatch && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(6px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '20px',
          }}
          onClick={() => setSelectedMatch(null)}
        >
          <div
            className="card"
            style={{ maxWidth: '540px', width: '100%', padding: '28px' }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ fontSize: '1.4rem', color: 'var(--neon-cyan)', marginBottom: '8px' }}>
              Match #{selectedMatch.matchId} Telemetry
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '20px' }}>
              {selectedMatch.player1Username} ({selectedMatch.player1Character}) vs {selectedMatch.player2Username} ({selectedMatch.player2Character})
            </p>

            <div style={{ marginBottom: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #1e293b' }}>
                <span style={{ color: 'var(--text-muted)' }}>Match Winner:</span>
                <strong style={{ color: 'var(--neon-gold)' }}>{selectedMatch.winnerUsername}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #1e293b' }}>
                <span style={{ color: 'var(--text-muted)' }}>Total Rounds Played:</span>
                <span>{selectedMatch.totalRounds || 3}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #1e293b' }}>
                <span style={{ color: 'var(--text-muted)' }}>P1 Final Score:</span>
                <span style={{ color: 'var(--neon-cyan)', fontWeight: 'bold' }}>{selectedMatch.player1Score}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #1e293b' }}>
                <span style={{ color: 'var(--text-muted)' }}>P2 Final Score:</span>
                <span style={{ color: 'var(--neon-pink)', fontWeight: 'bold' }}>{selectedMatch.player2Score}</span>
              </div>
            </div>

            <div style={{ textAlign: 'right' }}>
              <button className="btn btn-secondary btn-sm" onClick={() => setSelectedMatch(null)}>
                Close Telemetry
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
