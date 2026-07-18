import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';

function MyEvents() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [canceling, setCanceling] = useState(null);
  
  // Состояния для формы создания/редактирования
  const [showForm, setShowForm] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: '',
    durationSeconds: 3600,
    ticketPrice: 0,
    totalSeats: 50,
  });
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  const fetchMyEvents = async () => {
    try {
      setLoading(true);
      // Получаем все события (для админа показываем все)
      const data = await ApiService.get('/event');
      setEvents(data || []);
      setError('');
    } catch (err) {
      setError('Failed to load your events');
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyEvents();
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
      await fetchMyEvents();
      alert('Event canceled successfully');
    } catch (err) {
      alert(err.message || 'Failed to cancel event');
    } finally {
      setCanceling(null);
    }
  };

  const openCreateForm = () => {
    const defaultDate = new Date();
    defaultDate.setHours(defaultDate.getHours() + 1);
    setEditingEvent(null);
    setFormData({
      name: '',
      description: '',
      startTime: defaultDate.toISOString().slice(0, 16),
      durationSeconds: 3600,
      ticketPrice: 0,
      totalSeats: 50,
    });
    setFormError('');
    setShowForm(true);
  };

  const openEditForm = (event) => {
    setEditingEvent(event);
    setFormData({
      name: event.name || '',
      description: event.description || '',
      startTime: event.startTime ? new Date(event.startTime).toISOString().slice(0, 16) : '',
      durationSeconds: event.durationSeconds || 3600,
      ticketPrice: event.ticketPrice || 0,
      totalSeats: event.totalSeats || 50,
    });
    setFormError('');
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingEvent(null);
    setFormError('');
  };

  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
    setFormError('');
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');

    if (!formData.name.trim()) {
      setFormError('Event name is required');
      setFormLoading(false);
      return;
    }

    if (!formData.startTime) {
      setFormError('Start date and time is required');
      setFormLoading(false);
      return;
    }

    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description?.trim() || '',
        startTime: new Date(formData.startTime).toISOString(),
        durationSeconds: parseInt(formData.durationSeconds),
        ticketPrice: parseFloat(formData.ticketPrice),
        totalSeats: parseInt(formData.totalSeats),
        isCanceled: editingEvent?.isCanceled || false,
      };

      if (editingEvent) {
        await ApiService.put(`/event/${editingEvent.id}`, payload);
      } else {
        await ApiService.post('/event', payload);
      }

      closeForm();
      await fetchMyEvents();
      alert(editingEvent ? 'Event updated successfully!' : 'Event created successfully!');
    } catch (err) {
      setFormError(err.message || 'Failed to save event');
      console.error('Error saving event:', err);
    } finally {
      setFormLoading(false);
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
        <p>Loading your events...</p>
      </div>
    );
  }

  return (
    <div className="my-events">
      <div className="my-events-header">
        <div>
          <h1>📋 My Events</h1>
          <p className="subtitle">Manage events you've created</p>
        </div>
        <button className="btn-create" onClick={openCreateForm}>
          ➕ Create New Event
        </button>
      </div>

      {error && (
        <div className="error-banner">
          {error}
          <button onClick={fetchMyEvents} className="retry-button">
            Retry
          </button>
        </div>
      )}

      {/* Форма создания/редактирования */}
      {showForm && (
        <div className="form-overlay">
          <div className="event-form">
            <div className="form-header">
              <h3>{editingEvent ? '✏️ Edit Event' : '➕ Create New Event'}</h3>
              <button type="button" className="btn-close" onClick={closeForm}>✕</button>
            </div>

            {formError && (
              <div className="error-message">{formError}</div>
            )}

            <form onSubmit={handleFormSubmit}>
              <div className="form-group">
                <label>Event Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  required
                  maxLength={255}
                  placeholder="Enter event name"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleFormChange}
                  maxLength={2000}
                  rows={4}
                  placeholder="Enter event description (optional)"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Start Date & Time *</label>
                  <input
                    type="datetime-local"
                    name="startTime"
                    value={formData.startTime}
                    onChange={handleFormChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Duration (seconds) *</label>
                  <input
                    type="number"
                    name="durationSeconds"
                    value={formData.durationSeconds}
                    onChange={handleFormChange}
                    required
                    min={1}
                  />
                  <small className="hint">3600 = 1 hour</small>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Ticket Price ($) *</label>
                  <input
                    type="number"
                    name="ticketPrice"
                    value={formData.ticketPrice}
                    onChange={handleFormChange}
                    required
                    min={0}
                    step={0.01}
                  />
                </div>

                <div className="form-group">
                  <label>Total Seats *</label>
                  <input
                    type="number"
                    name="totalSeats"
                    value={formData.totalSeats}
                    onChange={handleFormChange}
                    required
                    min={1}
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancel-form" onClick={closeForm}>
                  Cancel
                </button>
                <button type="submit" className="btn-submit" disabled={formLoading}>
                  {formLoading ? 'Saving...' : (editingEvent ? 'Update Event' : 'Create Event')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Список событий */}
      <div className="events-list">
        {events.length === 0 ? (
          <div className="empty-state">
            <p>You haven't created any events yet</p>
            <button className="btn-create-first" onClick={openCreateForm}>
              ➕ Create Your First Event
            </button>
          </div>
        ) : (
          <div className="events-grid">
            {events.map((event) => (
              <div key={event.id} className={`event-card ${event.isCanceled ? 'canceled' : ''}`}>
                <div className="event-card-header">
                  <h3>{event.name}</h3>
                  <span className={`status-badge ${event.isCanceled ? 'canceled' : 'active'}`}>
                    {event.isCanceled ? '❌ Canceled' : '✅ Active'}
                  </span>
                </div>

                <p className="event-description">{event.description || 'No description'}</p>

                <div className="event-details">
                  <div className="detail-item">
                    <span className="detail-label">📅</span>
                    <span>{formatDate(event.startTime)}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">⏱️</span>
                    <span>{Math.round(event.durationSeconds / 60)} min</span>
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

                <div className="card-actions">
                  <button 
                    className="btn-edit" 
                    onClick={() => openEditForm(event)}
                    disabled={event.isCanceled}
                  >
                    ✏️ Edit
                  </button>
                  {!event.isCanceled && (
                    <button
                      className="btn-cancel-event"
                      onClick={() => handleCancel(event.id)}
                      disabled={canceling === event.id}
                    >
                      {canceling === event.id ? '⏳' : '🚫 Cancel'}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default MyEvents;