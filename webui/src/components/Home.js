import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';
import TokenManager from '../utils/tokenManager';

function Home() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reserving, setReserving] = useState(null);
  const [seatsToReserve, setSeatsToReserve] = useState(1);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const data = await ApiService.get('/event');
      setEvents(data || []);
      setError('');
    } catch (err) {
      setError('Failed to load events');
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleReserve = async (eventId) => {
    const isAuthenticated = TokenManager.isAuthenticated();
    if (!isAuthenticated) {
      alert('Please login to make a reservation');
      return;
    }

    try {
      setReserving(eventId);
      await ApiService.post('/reserve', {
        eventId,
        seats: seatsToReserve,
      });
      alert('Reservation created successfully!');
      setSeatsToReserve(1);
      await fetchEvents();
    } catch (err) {
      alert(err.message || 'Failed to create reservation');
    } finally {
      setReserving(null);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      date: 'medium',
      time: 'short',
    });
  };

  return (
    <div className="home">
      <div className="home-header">
        <h1>Upcoming Events</h1>
        <p className="home-subtitle">Discover and book tickets for amazing events</p>
      </div>

      {error && (
        <div className="error-banner">
          {error}
          <button onClick={fetchEvents} className="retry-button">
            Retry
          </button>
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading events...</p>
        </div>
      ) : (
        <div className="events-grid">
          {events.length === 0 ? (
            <div className="empty-state">
              <p>No events available at the moment</p>
            </div>
          ) : (
            events.map((event) => (
              <div key={event.id} className="event-card">
                <div className="event-card-header">
                  <h3>{event.name}</h3>
                  {event.isCanceled && (
                    <span className="event-badge canceled">Canceled</span>
                  )}
                </div>
                
                <p className="event-description">{event.description || 'No description'}</p>
                
                <div className="event-details">
                  <div className="detail-item">
                    <span className="detail-label">📅</span>
                    <span>{formatDate(event.startTime)}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">⏱️</span>
                    <span>{event.durationSeconds / 60} min</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">💰</span>
                    <span>${event.ticketPrice}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">💺</span>
                    <span>{event.totalSeats} seats</span>
                  </div>
                </div>

                {!event.isCanceled && (
                  <div className="reservation-section">
                    <div className="reservation-controls">
                      <label>Seats:</label>
                      <input
                        type="number"
                        min="1"
                        max={event.totalSeats}
                        value={seatsToReserve}
                        onChange={(e) => setSeatsToReserve(Math.min(
                          parseInt(e.target.value) || 1,
                          event.totalSeats
                        ))}
                        className="seats-input"
                      />
                    </div>
                    <button
                      className="btn-reserve"
                      onClick={() => handleReserve(event.id)}
                      disabled={reserving === event.id}
                    >
                      {reserving === event.id ? 'Reserving...' : 'Reserve Now'}
                    </button>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default Home;