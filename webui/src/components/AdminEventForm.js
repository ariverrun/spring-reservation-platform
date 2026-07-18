import React, { useState, useEffect } from 'react';
import ApiService from '../services/api.service';

function AdminEventForm({ event, onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: '',
    durationSeconds: 3600,
    ticketPrice: 0,
    totalSeats: 50,
    isCanceled: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (event) {
      setFormData({
        name: event.name || '',
        description: event.description || '',
        startTime: event.startTime ? new Date(event.startTime).toISOString().slice(0, 16) : '',
        durationSeconds: event.durationSeconds || 3600,
        ticketPrice: event.ticketPrice || 0,
        totalSeats: event.totalSeats || 50,
        isCanceled: event.isCanceled || false,
      });
    }
  }, [event]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = {
        name: formData.name,
        description: formData.description,
        startTime: new Date(formData.startTime).toISOString(),
        durationSeconds: parseInt(formData.durationSeconds),
        ticketPrice: parseFloat(formData.ticketPrice),
        totalSeats: parseInt(formData.totalSeats),
        isCanceled: formData.isCanceled || false,
      };

      if (event) {
        await ApiService.put(`/event/${event.id}`, payload);
      } else {
        await ApiService.post('/event', payload);
      }

      onSuccess();
    } catch (err) {
      setError(err.message || 'Failed to save event');
      console.error('Error saving event:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="admin-event-form">
      <div className="form-header">
        <h3>{event ? 'Edit Event' : 'Create New Event'}</h3>
        <button type="button" className="btn-close" onClick={onCancel}>✕</button>
      </div>

      {error && (
        <div className="error-message">{error}</div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Event Name *</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
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
            onChange={handleChange}
            maxLength={2000}
            rows={4}
            placeholder="Enter event description"
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Start Date & Time *</label>
            <input
              type="datetime-local"
              name="startTime"
              value={formData.startTime}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Duration (seconds) *</label>
            <input
              type="number"
              name="durationSeconds"
              value={formData.durationSeconds}
              onChange={handleChange}
              required
              min={1}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Ticket Price ($) *</label>
            <input
              type="number"
              name="ticketPrice"
              value={formData.ticketPrice}
              onChange={handleChange}
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
              onChange={handleChange}
              required
              min={1}
            />
          </div>
        </div>

        {event && event.isCanceled && (
          <div className="form-group">
            <div className="info-message">
              ⚠️ This event is currently canceled
            </div>
          </div>
        )}

        <div className="form-actions">
          <button type="button" className="btn-cancel-form" onClick={onCancel}>
            Cancel
          </button>
          <button type="submit" className="btn-submit" disabled={loading}>
            {loading ? 'Saving...' : (event ? 'Update Event' : 'Create Event')}
          </button>
        </div>
      </form>
    </div>
  );
}

export default AdminEventForm;