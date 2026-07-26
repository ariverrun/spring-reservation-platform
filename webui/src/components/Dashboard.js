import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';

function Dashboard() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updating, setUpdating] = useState(null);

  const fetchReservations = async () => {
    try {
      setLoading(true);
      const data = await ApiService.get('/reserve');
      setReservations(data || []);
      setError('');
    } catch (err) {
      setError('Failed to load reservations');
      console.error('Error fetching reservations:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations();
  }, []);

  const handleCancelReservation = async (reservationId, seats) => {
    if (!window.confirm('Are you sure you want to cancel this reservation?')) {
      return;
    }

    try {
      setUpdating(reservationId);
      await ApiService.put(`/reserve/${reservationId}`, {
        seats: seats,
        isCanceled: true,
      });
      await fetchReservations();
      alert('Reservation canceled successfully');
    } catch (err) {
      alert(err.message || 'Failed to cancel reservation');
    } finally {
      setUpdating(null);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      date: 'medium',
      time: 'short',
    });
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>My Reservations</h1>
        <span className="reservation-count">
          {reservations.length} reservation{reservations.length !== 1 ? 's' : ''}
        </span>
      </div>

      {error && (
        <div className="error-banner">
          {error}
          <button onClick={fetchReservations} className="retry-button">
            Retry
          </button>
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading your reservations...</p>
        </div>
      ) : (
        <div className="reservations-list">
          {reservations.length === 0 ? (
            <div className="empty-state">
              <p>You don't have any reservations yet</p>
              <button 
                className="btn-browse"
                onClick={() => window.location.href = '/'}
              >
                Browse Events
              </button>
            </div>
          ) : (
            reservations.map((reservation) => (
              <div key={reservation.id} className="reservation-card">
                <div className="reservation-card-header">
                  <h3>{reservation.event?.name || 'Event'}</h3>
                  <span className={`reservation-status ${reservation.isCanceled ? 'canceled' : 'active'}`}>
                    {reservation.isCanceled ? '❌ Canceled' : '✅ Active'}
                  </span>
                </div>

                <div className="reservation-details">
                  <div className="detail-item">
                    <span className="detail-label">Event ID:</span>
                    <span className="detail-value">{reservation.eventId?.substring(0, 8)}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Seats:</span>
                    <span className="detail-value">{reservation.seats}</span>
                  </div>
                  {reservation.event && (
                    <>
                      <div className="detail-item">
                        <span className="detail-label">Date:</span>
                        <span className="detail-value">{formatDate(reservation.event.startTime)}</span>
                      </div>
                      <div className="detail-item">
                        <span className="detail-label">Price:</span>
                        <span className="detail-value">${reservation.event.ticketPrice}</span>
                      </div>
                    </>
                  )}
                </div>

                {!reservation.isCanceled && (
                  <button
                    className="btn-cancel"
                    onClick={() => handleCancelReservation(reservation.id, reservation.seats)}
                    disabled={updating === reservation.id}
                  >
                    {updating === reservation.id ? 'Processing...' : 'Cancel Reservation'}
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default Dashboard;