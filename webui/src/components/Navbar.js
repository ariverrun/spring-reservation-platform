import React, { useState, useEffect } from 'react';
import TokenManager from '../utils/tokenManager';
import AuthService from '../services/auth.service';

function Navbar({ onNavigate, currentPage }) {
  const [isAuthenticated, setIsAuthenticated] = useState(TokenManager.isAuthenticated());
  const [isAdmin, setIsAdmin] = useState(TokenManager.isAdmin());
  const [userEmail, setUserEmail] = useState(TokenManager.getEmail());

  useEffect(() => {
    const handleAuthChange = (authState) => {
      setIsAuthenticated(authState);
      setIsAdmin(TokenManager.isAdmin());
      setUserEmail(TokenManager.getEmail());
    };

    TokenManager.addListener(handleAuthChange);
    return () => {
      TokenManager.removeListener(handleAuthChange);
    };
  }, []);

  const handleLogout = () => {
    AuthService.logout();
    onNavigate('home');
    // Принудительно обновляем состояние
    setIsAuthenticated(false);
    setIsAdmin(false);
    setUserEmail(null);
  };

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