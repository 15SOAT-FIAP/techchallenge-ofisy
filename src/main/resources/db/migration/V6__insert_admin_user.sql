INSERT INTO users (id, name, email, password, role, active, created_at)
VALUES (
           'a1b2c3d4-e5f6-7890-abcd-ef1234560001',
           'Administrador',
           'admin@ofisy.com',
           '$2b$10$pYSIMOpFMWtuVl5Oo0ArO.uzMX3bjyW/k91am2p.rzjINWPUh1aSC',
           'ADMIN',
           true,
           now()
);