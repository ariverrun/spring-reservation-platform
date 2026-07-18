import React, { useState, useEffect } from 'react';
import TokenManager from '../utils/tokenManager';
import AuthService from '../services/auth.service';

function Navbar({ onNavigate, currentPage }) {
  const [isAuthenticated, setIsAuthenticated] = useState(TokenManager.isAuthenticated());

  useEffect(() => {
    const handleAuthChange = (authState) => {
      setIsAuthenticated(authState);
    };

    TokenManager.addListener(handleAuthChange);
    return () => {
      TokenManager.removeListener(handleAuthChange);
    };
  }, []);

  const handleLogout = () => {
    AuthService.logout();
    onNavigate('home');
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
      </div>

      <div className="navbar-actions">
        {isAuthenticated ? (
          <>
            <span className="user-badge">👤 User</span>
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