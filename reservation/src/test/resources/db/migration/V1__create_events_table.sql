-- V1__create_events_table.sql
CREATE TABLE IF NOT EXISTS events (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    start_time TIMESTAMP NOT NULL,
    duration_seconds BIGINT NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL,
    total_seats INT NOT NULL
);

CREATE INDEX idx_event_user_id ON events(user_id);
CREATE INDEX idx_event_start_time ON events(start_time);