import React, { useState } from 'react';
import { api } from '../services/api.js';

export default function RegisterPage({ onLoginSuccess, setActivePage }) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Compute simple password strength
  const getPasswordStrength = (pass) => {
    let score = 0;
    if (pass.length >= 8) score++;
    if (/[A-Z]/.test(pass)) score++;
    if (/[0-9]/.test(pass)) score++;
    if (/[^A-Za-z0-9]/.test(pass)) score++;
    return score;
  };

  const strength = getPasswordStrength(password);
  const strengthLabels = ['Too Weak', 'Weak', 'Fair', 'Strong', 'Cybernetic'];
  const strengthColors = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#00e5ff'];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    setLoading(true);
    try {
      const res = await api.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password }),
      });
      if (res.user) {
        onLoginSuccess(res.user, null);
        setActivePage('select');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '480px' }}>
      <div className="card">
        <h2 style={{ fontSize: '2rem', color: '#fff', marginBottom: '8px', textAlign: 'center' }}>
          Player <span style={{ color: 'var(--neon-cyan)' }}>Registration</span>
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '24px', textAlign: 'center' }}>
          Create your Clash of Bytes fighter profile
        </p>

        {error && (
          <div style={{ padding: '10px', background: 'rgba(255, 42, 109, 0.15)', border: '1px solid var(--neon-pink)', color: 'var(--neon-pink)', borderRadius: '4px', marginBottom: '16px', fontSize: '0.85rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              placeholder="e.g. cyber_warrior"
              minLength={3}
              maxLength={30}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input
              type="email"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="e.g. player@domain.com"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="At least 6 characters"
            />
            {password && (
              <div style={{ marginTop: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '4px' }}>
                  <span style={{ color: '#94a3b8' }}>Strength:</span>
                  <span style={{ color: strengthColors[strength], fontWeight: 'bold' }}>{strengthLabels[strength]}</span>
                </div>
                <div style={{ height: '4px', background: '#1e293b', borderRadius: '2px', overflow: 'hidden' }}>
                  <div style={{ width: `${(strength / 4) * 100}%`, height: '100%', background: strengthColors[strength] }}></div>
                </div>
              </div>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Confirm Password</label>
            <input
              type="password"
              className="form-input"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              placeholder="Re-enter password"
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }} disabled={loading}>
            {loading ? 'Creating Profile...' : 'Complete Registration'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Already have an account?{' '}
          <span style={{ color: 'var(--neon-cyan)', cursor: 'pointer', textDecoration: 'underline' }} onClick={() => setActivePage('login')}>
            Sign In Here
          </span>
        </div>
      </div>
    </div>
  );
}
