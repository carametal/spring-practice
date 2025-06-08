INSERT INTO users (
    username, email, password, registration_date, created_by, created_at, updated_by, updated_at
) VALUES (
    'admin',
    'admin@example.com',
    crypt('admin', gen_salt('bf')),
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP,
    1,
    CURRENT_TIMESTAMP
);

INSERT INTO roles (role_name, description, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM_ADMIN', 'システム管理者', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('USER_ADMIN', 'ユーザー管理者', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('EMPLOYEE', '従業員', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
