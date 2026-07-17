import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';
import AuthService from '../services/auth.service';
import TokenManager from '../utils/tokenManager';

function Dashboard() {
  const [events, setEvents] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = async () => {
    try {
      setLoading(true);
      const [eventsData, reservationsData] = await Promise.all([
        ApiService.get('/event'),
        ApiService.get('/reserve').catch(() => []),
      ]);
      setEvents(eventsData || []);
      setReservations(reservationsData || []);
      setError('');
    } catch (err) {
      setError('Failed to fetch data');
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleLogout = () => {
    AuthService.logout();
  };

  const handleRefresh = () => {
    fetchData();
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Dashboard</h1>
        <div className="header-actions">
          <span className="user-info">
            ✅ Authenticated
          </span>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </header>

      <div className="dashboard-content">
        <div className="token-info">
          <h3>Token Status</h3>
          <div className="token-display">
            <span className="token-label">Access Token:</span>
            <span className="token-value">
              {TokenManager.getAccessToken()?.substring(0, 20)}...
            </span>
          </div>
        </div>

        {error && (
          <div className="error-banner">
            {error}
            <button onClick={handleRefresh} className="retry-button">
              Retry
            </button>
          </div>
        )}

        <div className="data-section">
          <h3>Events ({events.length})</h3>
          {loading ? (
            <div className="loading-spinner">Loading events...</div>
          ) : (
            <div className="data-grid">
              {events.map((event) => (
                <div key={event.id} className="data-card">
                  <h4>{event.name}</h4>
                  <p>{event.description}</p>
                  <div className="card-details">
                    <span>Price: ${event.ticketPrice}</span>
                    <span>Seats: {event.totalSeats}</span>
                  </div>
                </div>
              ))}
              {events.length === 0 && (
                <div className="empty-state">No events available</div>
              )}
            </div>
          )}
        </div>

        <div className="data-section">
          <h3>My Reservations ({reservations.length})</h3>
          {loading ? (
            <div className="loading-spinner">Loading reservations...</div>
          ) : (
            <div className="data-grid">
              {reservations.map((reservation) => (
                <div key={reservation.id} className="data-card">
                  <h4>Reservation #{reservation.id?.substring(0, 8)}</h4>
                  <div className="card-details">
                    <span>Seats: {reservation.seats}</span>
                    <span className={reservation.isCanceled ? 'canceled' : 'active'}>
                      {reservation.isCanceled ? 'Canceled' : 'Active'}
                    </span>
                  </div>
                </div>
              ))}
              {reservations.length === 0 && (
                <div className="empty-state">No reservations yet</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;