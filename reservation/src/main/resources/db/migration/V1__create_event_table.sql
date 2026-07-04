-- V1__create_event_table.sql
CREATE TABLE IF NOT EXISTS event (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    start_time TIMESTAMP NOT NULL,
    duration_seconds BIGINT NOT NULL,
    ticket_price DECIMAL(10, 2) NOT NULL,
    total_seats INT NOT NULL
);