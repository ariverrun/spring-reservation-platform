-- V2__insert_roles_and_users.sql
-- Вставляем роли
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- First admin: admin@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    X'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    'admin@example.com',
    'Admin',
    'Adminov',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

-- Second admin: admin2@example.com / admin123
INSERT INTO users (id, email, first_name, last_name, password) VALUES
(
    X'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    'admin2@example.com',
    'Admin2',
    'Adminov2',
    '$2a$10$DgzLPGCcUmurOjWL4b/8r.CkNBbxrJzwGJls/nNkoMJL1szJsBqqu'
);

-- Назначаем роли админам (ROLE_ADMIN и ROLE_USER)
INSERT INTO user_roles (user_id, role_id) VALUES
(
    X'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    X'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
),
(
    X'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),
(
    X'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
);