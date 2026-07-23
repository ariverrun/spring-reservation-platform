import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';

function AdminEventList({ onEdit, onRefresh }) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [canceling, setCanceling] = useState(null);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const data = await ApiService.get('/event/my');
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

  const handleCancel = async (eventId) => {
    const event = events.find(e => e.id === eventId);
    if (event.isCanceled) {
      alert('This event is already canceled');
      return;
    }

    if (!window.confirm('Are you sure you want to cancel this event?')) {
      return;
    }

    try {
      setCanceling(eventId);
      await ApiService.put(`/event/${eventId}`, {
        name: event.name,
        description: event.description || '',
        startTime: new Date(event.startTime).toISOString(),
        durationSeconds: event.durationSeconds,
        ticketPrice: event.ticketPrice,
        totalSeats: event.totalSeats,
        isCanceled: true,
      });
      await fetchEvents();
      if (onRefresh) onRefresh();
      alert('Event canceled successfully');
    } catch (err) {
      alert(err.message || 'Failed to cancel event');
    } finally {
      setCanceling(null);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      date: 'medium',
      time: 'short',
    });
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading events...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-banner">
        {error}
        <button onClick={fetchEvents} className="retry-button">
          Retry
        </button>
      </div>
    );
  }
  
  return (
    <div className="admin-event-list">
      <div className="admin-list-header">
        <h3>Your Events</h3>
        <span className="event-count">{events.length} event{events.length !== 1 ? 's' : ''}</span>
      </div>

      {events.length === 0 ? (
        <div className="empty-state">
          <p>You haven't created any events yet</p>
        </div>
      ) : (
        <div className="events-table-container">
          <table className="events-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Date & Time</th>
                <th>Price</th>
                <th>Seats</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id} className={event.isCanceled ? 'canceled-row' : ''}>
                  <td className="event-name-cell">
                    <div className="event-name">{event.name}</div>
                    {event.description && (
                      <div className="event-description-truncated">
                        {event.description.substring(0, 50)}
                        {event.description.length > 50 ? '...' : ''}
                      </div>
                    )}
                  </td>
                  <td>{formatDate(event.startTime)}</td>
                  <td>${event.ticketPrice}</td>
                  <td>{event.totalSeats}</td>
                  <td>
                    <span className={`status-badge ${event.isCanceled ? 'canceled' : 'active'}`}>
                      {event.isCanceled ? 'Canceled' : 'Active'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-edit"
                        onClick={() => onEdit(event)}
                        title="Edit event"
                      >
                        ✏️ Edit
                      </button>
                      {!event.isCanceled && (
                        <button
                          className="btn-cancel-event"
                          onClick={() => handleCancel(event.id)}
                          disabled={canceling === event.id}
                          title="Cancel event"
                        >
                          {canceling === event.id ? '⏳' : '🚫 Cancel'}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default AdminEventList;