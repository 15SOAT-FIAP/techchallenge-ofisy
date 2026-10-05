INSERT INTO users (id, name, email, password, role, active, created_at)
VALUES (
           'a1b2c3d4-e5f6-7890-abcd-ef1234560999',
           'Test User',
           'test@ofisy.com',
           '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKPTBaVRF5TzF9x30W', -- 'password'
           'ADMIN',
           true,
           now()
);
