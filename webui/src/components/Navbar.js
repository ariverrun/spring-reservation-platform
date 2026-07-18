import React, { useState, useEffect } from 'react';
import TokenManager from '../utils/tokenManager';
import AuthService from '../services/auth.service';

function Navbar({ onNavigate, currentPage }) {
  const [isAuthenticated, setIsAuthenticated] = useState(TokenManager.isAuthenticated());
  const [isAdmin, setIsAdmin] = useState(TokenManager.isAdmin());
  const [userEmail, setUserEmail] = useState(TokenManager.getEmail());
  const [tokenExpiry, setTokenExpiry] = useState(null);
  const [timeUntilExpiry, setTimeUntilExpiry] = useState(0);

  useEffect(() => {
    const handleAuthChange = (authState) => {
      setIsAuthenticated(authState);
      setIsAdmin(TokenManager.isAdmin());
      setUserEmail(TokenManager.getEmail());
      if (authState) {
        const expiry = TokenManager.getTokenExpiry();
        setTokenExpiry(expiry);
        updateTimeUntilExpiry();
      } else {
        setTokenExpiry(null);
        setTimeUntilExpiry(0);
      }
    };

    TokenManager.addListener(handleAuthChange);
    
    // Обновляем таймер каждую секунду
    const timerInterval = setInterval(() => {
      updateTimeUntilExpiry();
    }, 1000);

    return () => {
      TokenManager.removeListener(handleAuthChange);
      clearInterval(timerInterval);
    };
  }, []);

  const updateTimeUntilExpiry = () => {
    if (TokenManager.isAuthenticated()) {
      const remaining = TokenManager.getTimeUntilExpiry();
      setTimeUntilExpiry(remaining);
    }
  };

  const handleLogout = () => {
    AuthService.logout();
    onNavigate('home');
    setIsAuthenticated(false);
    setIsAdmin(false);
    setUserEmail(null);
    setTokenExpiry(null);
    setTimeUntilExpiry(0);
  };

  // Форматируем время до истечения
  const formatTimeUntilExpiry = (ms) => {
    if (ms <= 0) return 'Expired';
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    if (minutes > 0) {
      return `${minutes}m ${remainingSeconds}s`;
    }
    return `${seconds}s`;
  };

  const isTokenNearExpiry = timeUntilExpiry < 120000; // меньше 2 минут

  return (
    <nav className="navbar">
      <div className="navbar-brand" onClick={() => onNavigate('home')}>
        <span className="brand-icon">🎫</span>
        <span className="brand-text">EventApp</span>
      </div>

      <div className="navbar-links">
        <button
          className={`nav-link ${currentPage === 'home' ? 'active' : ''}`}
          onClick={() => onNavigate('home')}
        >
          Home
        </button>

        {isAuthenticated && (
          <button
            className={`nav-link ${currentPage === 'dashboard' ? 'active' : ''}`}
            onClick={() => onNavigate('dashboard')}
          >
            My Reservations
          </button>
        )}

        {isAdmin && (
          <button
            className={`nav-link admin ${currentPage === 'my-events' ? 'active' : ''}`}
            onClick={() => onNavigate('my-events')}
          >
            📋 My Events
          </button>
        )}
      </div>

      <div className="navbar-actions">
        {isAuthenticated ? (
          <>
            <span className="user-badge">
              {isAdmin ? '👑 Admin' : '👤 User'}
              {userEmail && <span className="user-email"> {userEmail}</span>}
            </span>
            
            {/* Индикатор времени до истечения токена */}
            {tokenExpiry && (
              <span className={`token-timer ${isTokenNearExpiry ? 'warning' : ''}`}>
                ⏱️ {formatTimeUntilExpiry(timeUntilExpiry)}
              </span>
            )}
            
            <button className="btn-logout" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <button
            className={`btn-login ${currentPage === 'login' ? 'active' : ''}`}
            onClick={() => onNavigate('login')}
          >
            Login
          </button>
        )}
      </div>
    </nav>
  );
}

export default Navbar;