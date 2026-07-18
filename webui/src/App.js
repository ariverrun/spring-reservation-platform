import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Home from './components/Home';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import ProtectedRoute from './components/ProtectedRoute';
import TokenManager from './utils/tokenManager';
import './App.css';

function App() {
  const [currentPage, setCurrentPage] = useState('home');
  const [isAuthenticated, setIsAuthenticated] = useState(TokenManager.isAuthenticated());

  useEffect(() => {
    const handleAuthChange = (authState) => {
      setIsAuthenticated(authState);
      if (!authState) {
        setCurrentPage('home');
      }
    };

    TokenManager.addListener(handleAuthChange);
    return () => {
      TokenManager.removeListener(handleAuthChange);
    };
  }, []);

  const handleNavigate = (page) => {
    if (page === 'dashboard' && !isAuthenticated) {
      return;
    }
    setCurrentPage(page);
  };

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
    setCurrentPage('home');
  };

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <Home />;
      case 'login':
        return <Login onLoginSuccess={handleLoginSuccess} />;
      case 'dashboard':
        return (
          <ProtectedRoute onNavigate={handleNavigate}>
            <Dashboard />
          </ProtectedRoute>
        );
      default:
        return <Home />;
    }
  };

  return (
    <div className="app">
      <Navbar 
        onNavigate={handleNavigate} 
        currentPage={currentPage}
      />
      <main className="app-content">
        {renderPage()}
      </main>
    </div>
  );
}

export default App;