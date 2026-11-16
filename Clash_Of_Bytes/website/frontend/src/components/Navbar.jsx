import React from 'react';

export default function Navbar({ activePage, setActivePage, user, onLogout }) {
  return (
    <nav className="navbar">
      <div className="container nav-container">
        <div className="nav-brand" onClick={() => setActivePage('home')} style={{ cursor: 'pointer' }}>
          <span>CLASH OF <span className="highlight">BYTES</span></span>
        </div>

        <ul className="nav-links">
          <li className={`nav-item ${activePage === 'home' ? 'active' : ''}`} onClick={() => setActivePage('home')}>
            Home
          </li>
          <li className={`nav-item ${activePage === 'select' ? 'active' : ''}`} onClick={() => setActivePage('select')}>
            Fighters
          </li>
          <li className={`nav-item ${activePage === 'play' ? 'active' : ''}`} onClick={() => setActivePage('play')}>
            Battle Arena
          </li>
          <li className={`nav-item ${activePage === 'history' ? 'active' : ''}`} onClick={() => setActivePage('history')}>
            Match History
          </li>
          <li className={`nav-item ${activePage === 'leaderboard' ? 'active' : ''}`} onClick={() => setActivePage('leaderboard')}>
            Leaderboard
          </li>
          {user && (
            <li className={`nav-item ${activePage === 'dashboard' ? 'active' : ''}`} onClick={() => setActivePage('dashboard')}>
              Dashboard
            </li>
          )}
        </ul>

        <div className="nav-auth">
          {user ? (
            <>
              <span style={{ fontSize: '0.9rem', color: 'var(--neon-cyan)', fontWeight: 'bold' }}>
                {user.username}
              </span>
              <button className="btn btn-secondary btn-sm" onClick={onLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <button className="btn btn-secondary btn-sm" onClick={() => setActivePage('login')}>
                Login
              </button>
              <button className="btn btn-primary btn-sm" onClick={() => setActivePage('register')}>
                Sign Up
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
