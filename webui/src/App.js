import React, { useState, useEffect } from 'react';

function App() {
  const [message, setMessage] = useState('Hello World!');
  const [apiResponse, setApiResponse] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/v1/event');
      const data = await response.json();
      setApiResponse(data);
    } catch (error) {
      console.error('Error fetching events:', error);
      setApiResponse({ error: 'Failed to fetch events' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  return (
    <div className="app">
      <header className="app-header">
        <h1>{message}</h1>
        <p className="subtitle">Web UI Service</p>
      </header>
      
      <main className="app-main">
        <section className="status-section">
          <h2>Service Status</h2>
          <div className="status-badge">
            <span className="status-dot"></span>
            <span>Connected to Gateway</span>
          </div>
        </section>

        <section className="api-section">
          <h2>Events API Response</h2>
          {loading ? (
            <div className="loading">Loading events...</div>
          ) : (
            <div className="api-response">
              <pre>{JSON.stringify(apiResponse, null, 2)}</pre>
            </div>
          )}
        </section>

        <section className="endpoints-section">
          <h2>Available Endpoints</h2>
          <ul className="endpoints-list">
            <li><span className="method post">POST</span> /api/v1/user - Register</li>
            <li><span className="method post">POST</span> /api/v1/auth/login - Login</li>
            <li><span className="method post">POST</span> /api/v1/auth/refresh - Refresh Token</li>
            <li><span className="method get">GET</span> /api/v1/event - Get All Events</li>
            <li><span className="method post">POST</span> /api/v1/event - Create Event</li>
            <li><span className="method get">GET</span> /api/v1/event/{'{id}'} - Get Event</li>
            <li><span className="method get">GET</span> /api/v1/reserve - Get User Reservations</li>
            <li><span className="method post">POST</span> /api/v1/reserve - Create Reservation</li>
          </ul>
        </section>
      </main>
    </div>
  );
}

export default App;