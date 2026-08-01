-- Regular user: user@example.com / user123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    X'019f31c272437f0d9c28d9f36dcaf6c4',
    'user@example.com',
    'User',
    'Userov',
    '$2a$10$4w8zidR/UuXLBerYEYWYWei4//oTY7nxtFqHVPz6eCSL2qu/.lUjq'
);

INSERT INTO user_roles (user_id, role_id) VALUES
(
    X'019f31c272437f0d9c28d9f36dcaf6c4',
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
);