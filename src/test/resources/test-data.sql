INSERT INTO roles (role_name, description, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM_ADMIN', 'システム管理者', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('USER_ADMIN', 'ユーザー管理者', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('EMPLOYEE', '従業員', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, password, registration_date, created_by, created_at, updated_by, updated_at) VALUES
('testadmin', 'testadmin@example.com', crypt('password123', gen_salt('bf')), CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('useradmin', 'useradmin@example.com', crypt('password123', gen_salt('bf')), CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
('employee', 'employee@example.com', crypt('password123', gen_salt('bf')), CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) 
SELECT u.user_id, r.role_id FROM users u, roles r 
WHERE (u.username = 'testadmin' AND r.role_name = 'SYSTEM_ADMIN') 
   OR (u.username = 'useradmin' AND r.role_name = 'USER_ADMIN') 
   OR (u.username = 'employee' AND r.role_name = 'EMPLOYEE');
