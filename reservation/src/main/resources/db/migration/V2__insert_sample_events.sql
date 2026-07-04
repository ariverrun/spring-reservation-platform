-- V2__insert_sample_events.sql
INSERT INTO event (id, user_id, name, description, start_time, duration_seconds, ticket_price, total_seats) VALUES
(
    X'1234567890abcdef1234567890abcdef',
    X'abcdef1234567890abcdef1234567890',
    'Spring Boot Conference 2024',
    'Annual Spring Boot developer conference',
    '2024-12-15 10:00:00',
    28800,
    149.99,
    200
),
(
    X'2234567890abcdef1234567890abcdef',
    X'abcdef1234567890abcdef1234567890',
    'Java Microservices Workshop',
    'Hands-on workshop with Spring Cloud',
    '2024-12-20 09:00:00',
    21600,
    89.50,
    50
),
(
    X'3234567890abcdef1234567890abcdef',
    X'abcdef1234567890abcdef1234567890',
    'Tech Meetup: Modern Java',
    'Monthly meetup about Java features',
    '2024-12-25 18:30:00',
    7200,
    0.00,
    100
);