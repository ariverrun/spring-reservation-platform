-- V2__insert_roles_and_users.sql
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- First admin: admin@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    X'0191234567897abcdef0123456789abc',
    'admin@example.com',
    'Admin',
    'Adminov',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

-- Second admin: admin2@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    X'019f31c272437f0d9c28d9f36dcaf6b3',
    'admin2@example.com',
    'Admin2',
    'Adminov2',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

INSERT INTO user_roles (user_id, role_id) VALUES
(
    X'0191234567897abcdef0123456789abc',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    X'0191234567897abcdef0123456789abc',
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
),
(
    X'019f31c272437f0d9c28d9f36dcaf6b3',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    X'019f31c272437f0d9c28d9f36dcaf6b3',
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
);