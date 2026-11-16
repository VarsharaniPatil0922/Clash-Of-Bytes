import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar.jsx';
import Footer from './components/Footer.jsx';
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import CharacterSelectPage from './pages/CharacterSelectPage.jsx';
import GamePage from './pages/GamePage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import MatchHistoryPage from './pages/MatchHistoryPage.jsx';
import LeaderboardPage from './pages/LeaderboardPage.jsx';
import { api } from './services/api.js';

export default function App() {
  const [activePage, setActivePage] = useState('home');
  const [user, setUser] = useState(api.getUser());
  const [p1User, setP1User] = useState(api.getUser());
  const [p2User, setP2User] = useState(null);

  const [characters, setCharacters] = useState([]);
  const [p1Char, setP1Char] = useState(null);
  const [p2Char, setP2Char] = useState(null);

  useEffect(() => {
    async function init() {
      try {
        const charData = await api.request('/characters');
        if (Array.isArray(charData) && charData.length > 0) {
          setCharacters(charData);
          setP1Char(charData[3] || charData[0]); // Cyber Brawler
          setP2Char(charData[2] || charData[1]); // Shadow Ninja
        }
      } catch (err) {
        console.error('Failed to load characters:', err);
      }
    }
    init();

    // Hash change listener for browser back/forward buttons
    const handleHash = () => {
      const hash = window.location.hash.replace('#', '');
      if (['home', 'login', 'register', 'select', 'play', 'dashboard', 'history', 'leaderboard'].includes(hash)) {
        setActivePage(hash);
      }
    };
    window.addEventListener('hashchange', handleHash);
    if (window.location.hash) handleHash();

    return () => window.removeEventListener('hashchange', handleHash);
  }, []);

  const navigateTo = (page) => {
    setActivePage(page);
    window.location.hash = page;
  };

  const handleLoginSuccess = (u1, u2 = null) => {
    setUser(u1);
    setP1User(u1);
    if (u2) {
      setP2User(u2);
    }
  };

  const handleLogout = () => {
    api.setUser(null);
    api.setToken(null);
    setUser(null);
    setP1User(null);
    setP2User(null);
    navigateTo('home');
  };

  return (
    <>
      <Navbar
        activePage={activePage}
        setActivePage={navigateTo}
        user={user}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {activePage === 'home' && (
          <HomePage setActivePage={navigateTo} characters={characters} />
        )}
        {activePage === 'login' && (
          <LoginPage onLoginSuccess={handleLoginSuccess} setActivePage={navigateTo} />
        )}
        {activePage === 'register' && (
          <RegisterPage onLoginSuccess={handleLoginSuccess} setActivePage={navigateTo} />
        )}
        {activePage === 'select' && (
          <CharacterSelectPage
            characters={characters}
            p1Char={p1Char}
            p2Char={p2Char}
            setP1Char={setP1Char}
            setP2Char={setP2Char}
            setActivePage={navigateTo}
            p1User={p1User}
            p2User={p2User}
          />
        )}
        {activePage === 'play' && (
          <GamePage
            p1Char={p1Char}
            p2Char={p2Char}
            p1User={p1User}
            p2User={p2User}
            setActivePage={navigateTo}
          />
        )}
        {activePage === 'dashboard' && (
          <DashboardPage user={user} setActivePage={navigateTo} />
        )}
        {activePage === 'history' && (
          <MatchHistoryPage />
        )}
        {activePage === 'leaderboard' && (
          <LeaderboardPage />
        )}
      </main>

      <Footer />
    </>
  );
}
