import React, { useState } from 'react';
import { api } from '../services/api.js';

export default function LoginPage({ onLoginSuccess, setActivePage }) {
  // Mode toggle: 'single' vs 'dual' (2-player local)
  const [mode, setMode] = useState('dual');

  // Single Player Form
  const [singleUser, setSingleUser] = useState('');
  const [singlePass, setSinglePass] = useState('');
  const [singleError, setSingleError] = useState('');

  // Dual Player Form (Matches Swing TwoPlayerLoginFrame)
  const [p1User, setP1User] = useState('cyber_champion');
  const [p1Pass, setP1Pass] = useState('Password@123');
  const [p1Auth, setP1Auth] = useState(null);
  const [p1Error, setP1Error] = useState('');

  const [p2User, setP2User] = useState('neon_striker');
  const [p2Pass, setP2Pass] = useState('Password@123');
  const [p2Auth, setP2Auth] = useState(null);
  const [p2Error, setP2Error] = useState('');

  const handleSingleSubmit = async (e) => {
    e.preventDefault();
    setSingleError('');
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: singleUser, password: singlePass }),
      });
      if (res.user) {
        onLoginSuccess(res.user, null);
        setActivePage('dashboard');
      }
    } catch (err) {
      setSingleError(err.message);
    }
  };

  const handleP1Login = async (e) => {
    e.preventDefault();
    setP1Error('');
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: p1User, password: p1Pass }),
      });
      setP1Auth(res.user);
    } catch (err) {
      setP1Error(err.message);
    }
  };

  const handleP2Login = async (e) => {
    e.preventDefault();
    setP2Error('');
    if (p1Auth && (p1Auth.username.toLowerCase() === p2User.toLowerCase())) {
      setP2Error('Error: Player 1 and Player 2 cannot be the same user account!');
      return;
    }
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: p2User, password: p2Pass }),
      });
      if (p1Auth && res.user.userId === p1Auth.userId) {
        setP2Error('Error: Player 1 and Player 2 cannot be the same user account!');
        return;
      }
      setP2Auth(res.user);
    } catch (err) {
      setP2Error(err.message);
    }
  };

  const handleStartDualMatch = () => {
    if (!p1Auth || !p2Auth) return;
    onLoginSuccess(p1Auth, p2Auth);
    setActivePage('select');
  };

  return (
    <div className="container" style={{ maxWidth: mode === 'dual' ? '960px' : '460px' }}>
      <div style={{ textAlign: 'center', marginBottom: '30px' }}>
        <h2 style={{ fontSize: '2.2rem', color: '#fff', marginBottom: '8px' }}>
          Player <span style={{ color: 'var(--neon-cyan)' }}>Authentication</span>
        </h2>
        <div style={{ display: 'inline-flex', background: '#0f172a', padding: '4px', borderRadius: '6px', border: '1px solid #1e293b' }}>
          <button
            className={`btn btn-sm ${mode === 'dual' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setMode('dual')}
          >
            2-Player Dual Login
          </button>
          <button
            className={`btn btn-sm ${mode === 'single' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setMode('single')}
          >
            Single Sign-In
          </button>
        </div>
      </div>

      {mode === 'single' ? (
        <div className="card">
          <form onSubmit={handleSingleSubmit}>
            {singleError && (
              <div style={{ padding: '10px', background: 'rgba(255, 42, 109, 0.15)', border: '1px solid var(--neon-pink)', color: 'var(--neon-pink)', borderRadius: '4px', marginBottom: '16px', fontSize: '0.85rem' }}>
                {singleError}
              </div>
            )}
            <div className="form-group">
              <label className="form-label">Username or Email</label>
              <input
                type="text"
                className="form-input"
                value={singleUser}
                onChange={(e) => setSingleUser(e.target.value)}
                required
                placeholder="Enter username"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                value={singlePass}
                onChange={(e) => setSinglePass(e.target.value)}
                required
                placeholder="Enter password"
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
              Sign In
            </button>
          </form>
        </div>
      ) : (
        /* DUAL TWO-PLAYER LOGIN INTERFACE */
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px' }}>
            {/* Player 1 Authentication Box */}
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ color: 'var(--neon-cyan)', fontSize: '1.2rem' }}>Player 1 (Left)</h3>
                <span className={`badge ${p1Auth ? 'badge-win' : 'badge-loss'}`}>
                  {p1Auth ? 'AUTHENTICATED' : 'NOT SIGNED IN'}
                </span>
              </div>

              {p1Error && (
                <div style={{ padding: '8px', background: 'rgba(255, 42, 109, 0.15)', border: '1px solid var(--neon-pink)', color: 'var(--neon-pink)', borderRadius: '4px', marginBottom: '14px', fontSize: '0.8rem' }}>
                  {p1Error}
                </div>
              )}

              {p1Auth ? (
                <div style={{ textAlign: 'center', padding: '24px 0' }}>
                  <div style={{ fontSize: '1.3rem', color: '#fff', fontWeight: 'bold' }}>{p1Auth.username}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '16px' }}>{p1Auth.email}</div>
                  <button className="btn btn-secondary btn-sm" onClick={() => setP1Auth(null)}>
                    Switch User
                  </button>
                </div>
              ) : (
                <form onSubmit={handleP1Login}>
                  <div className="form-group">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-input"
                      value={p1User}
                      onChange={(e) => setP1User(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-input"
                      value={p1Pass}
                      onChange={(e) => setP1Pass(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                    Authenticate P1
                  </button>
                </form>
              )}
            </div>

            {/* Player 2 Authentication Box */}
            <div className="card card-pink">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ color: 'var(--neon-pink)', fontSize: '1.2rem' }}>Player 2 (Right)</h3>
                <span className={`badge ${p2Auth ? 'badge-win' : 'badge-loss'}`}>
                  {p2Auth ? 'AUTHENTICATED' : 'NOT SIGNED IN'}
                </span>
              </div>

              {p2Error && (
                <div style={{ padding: '8px', background: 'rgba(255, 42, 109, 0.15)', border: '1px solid var(--neon-pink)', color: 'var(--neon-pink)', borderRadius: '4px', marginBottom: '14px', fontSize: '0.8rem' }}>
                  {p2Error}
                </div>
              )}

              {p2Auth ? (
                <div style={{ textAlign: 'center', padding: '24px 0' }}>
                  <div style={{ fontSize: '1.3rem', color: '#fff', fontWeight: 'bold' }}>{p2Auth.username}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '16px' }}>{p2Auth.email}</div>
                  <button className="btn btn-secondary btn-sm" onClick={() => setP2Auth(null)}>
                    Switch User
                  </button>
                </div>
              ) : (
                <form onSubmit={handleP2Login}>
                  <div className="form-group">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-input"
                      value={p2User}
                      onChange={(e) => setP2User(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Password</label>
                    <input
                      type="password"
                      className="form-input"
                      value={p2Pass}
                      onChange={(e) => setP2Pass(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-danger" style={{ width: '100%' }}>
                    Authenticate P2
                  </button>
                </form>
              )}
            </div>
          </div>

          {/* Start Battle Button */}
          <div style={{ textAlign: 'center', marginTop: '30px' }}>
            <button
              className="btn btn-primary btn-lg"
              disabled={!p1Auth || !p2Auth}
              onClick={handleStartDualMatch}
              style={{
                opacity: (!p1Auth || !p2Auth) ? 0.45 : 1,
                cursor: (!p1Auth || !p2Auth) ? 'not-allowed' : 'pointer',
              }}
            >
              {p1Auth && p2Auth ? 'Proceed to Character Select \u2192' : 'Both Players Must Sign In First'}
            </button>
          </div>
        </div>
      )}

      <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
        Don't have an account?{' '}
        <span
          style={{ color: 'var(--neon-cyan)', cursor: 'pointer', textDecoration: 'underline' }}
          onClick={() => setActivePage('register')}
        >
          Create One Here
        </span>
      </div>
    </div>
  );
}
