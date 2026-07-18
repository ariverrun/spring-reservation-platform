import React, { useState } from 'react';
import AdminEventList from './AdminEventList';
import AdminEventForm from './AdminEventForm';

function AdminPanel() {
  const [editingEvent, setEditingEvent] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleCreate = () => {
    setEditingEvent(null);
    setShowForm(true);
  };

  const handleEdit = (event) => {
    setEditingEvent(event);
    setShowForm(true);
  };

  const handleSuccess = () => {
    setShowForm(false);
    setEditingEvent(null);
    setRefreshKey(prev => prev + 1);
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingEvent(null);
  };

  return (
    <div className="admin-panel">
      <div className="admin-panel-header">
        <h2>Admin Panel</h2>
        <button className="btn-create" onClick={handleCreate}>
          ➕ Create New Event
        </button>
      </div>

      {showForm && (
        <div className="form-overlay">
          <AdminEventForm
            event={editingEvent}
            onSuccess={handleSuccess}
            onCancel={handleCancel}
          />
        </div>
      )}

      <div className="admin-content">
        <AdminEventList 
          key={refreshKey}
          onEdit={handleEdit}
          onRefresh={() => setRefreshKey(prev => prev + 1)}
        />
      </div>
    </div>
  );
}

export default AdminPanel;