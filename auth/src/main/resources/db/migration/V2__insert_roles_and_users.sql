-- V2__insert_roles_and_users.sql
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- First admin: admin@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    CAST(CAST('01912345-6789-7abc-def0-123456789abc' AS UUID) AS BINARY(16)),
    'admin@example.com',
    'Admin',
    'Adminov',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

-- Second admin: admin2@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    CAST(CAST('019f31c2-7243-7f0d-9c28-d9f36dcaf6b3' AS UUID) AS BINARY(16)),
    'admin2@example.com',
    'Admin2',
    'Adminov2',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

INSERT INTO user_roles (user_id, role_id) VALUES
(
    CAST(CAST('01912345-6789-7abc-def0-123456789abc' AS UUID) AS BINARY(16)),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    CAST(CAST('01912345-6789-7abc-def0-123456789abc' AS UUID) AS BINARY(16)),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
),
(
    CAST(CAST('019f31c2-7243-7f0d-9c28-d9f36dcaf6b3' AS UUID) AS BINARY(16)),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    CAST(CAST('019f31c2-7243-7f0d-9c28-d9f36dcaf6b3' AS UUID) AS BINARY(16)),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
);