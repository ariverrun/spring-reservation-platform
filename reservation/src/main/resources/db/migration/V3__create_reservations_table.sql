-- V3__create_reservations_table.sql
CREATE TABLE IF NOT EXISTS reservations (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    event_id BINARY(16) NOT NULL,
    seats INT NOT NULL
);

CREATE INDEX idx_reservation_user_id ON reservations(user_id);
CREATE INDEX idx_reservation_event_id ON reservations(event_id);