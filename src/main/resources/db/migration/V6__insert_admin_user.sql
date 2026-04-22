INSERT INTO users (id, name, email, password, role, active, created_at)
VALUES (
           gen_random_uuid(),
           'Administrador',
           'admin@ofisy.com',
           '$2b$10$pYSIMOpFMWtuVl5Oo0ArO.uzMX3bjyW/k91am2p.rzjINWPUh1aSC',
           'ADMIN',
           true,
           now()
);