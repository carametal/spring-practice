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
