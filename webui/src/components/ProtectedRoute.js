import React, { useEffect, useState } from 'react';
import TokenManager from '../utils/tokenManager';

function ProtectedRoute({ children, onNavigate }) {
  const [isAuthenticated, setIsAuthenticated] = useState(TokenManager.isAuthenticated());

  useEffect(() => {
    const handleAuthChange = (authState) => {
      setIsAuthenticated(authState);
      if (!authState) {
        onNavigate('login');
      }
    };

    TokenManager.addListener(handleAuthChange);
    return () => {
      TokenManager.removeListener(handleAuthChange);
    };
  }, [onNavigate]);

  if (!isAuthenticated) {
    return null;
  }

  return children;
}

export default ProtectedRoute;