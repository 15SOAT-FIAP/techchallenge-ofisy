INSERT INTO service_order_executions (id, service_catalog_id, service_order_id, status, started_at, finished_at) VALUES
-- Service executions for service order 1
('c1d2e3f4-a5b6-7890-cdef-012345678901', 'b1c2d3e4-f5a6-7890-bcde-f12345678901', 'c3d4e5f6-a7b8-9012-cdef-123456780001', 'COMPLETED', now() - interval '5 days', now() - interval '5 days' + interval '1 hour'),
('c1d2e3f4-a5b6-7890-cdef-012345678902', 'b1c2d3e4-f5a6-7890-bcde-f12345678902', 'c3d4e5f6-a7b8-9012-cdef-123456780001', 'COMPLETED', now() - interval '5 days' + interval '2 hours', now() - interval '5 days' + interval '3 hours'),

-- Service executions for service order 2
('c1d2e3f4-a5b6-7890-cdef-012345678903', 'b1c2d3e4-f5a6-7890-bcde-f12345678903', 'c3d4e5f6-a7b8-9012-cdef-123456780002', 'COMPLETED', now() - interval '4 days', now() - interval '4 days' + interval '2 hours'),
('c1d2e3f4-a5b6-7890-cdef-012345678904', 'b1c2d3e4-f5a6-7890-bcde-f12345678904', 'c3d4e5f6-a7b8-9012-cdef-123456780002', 'IN_PROGRESS', now() - interval '2 days', null),

-- Service executions for service order 3
('c1d2e3f4-a5b6-7890-cdef-012345678905', 'b1c2d3e4-f5a6-7890-bcde-f12345678905', 'c3d4e5f6-a7b8-9012-cdef-123456780003', 'COMPLETED', now() - interval '3 days', now() - interval '3 days' + interval '30 minutes'),
('c1d2e3f4-a5b6-7890-cdef-012345678906', 'b1c2d3e4-f5a6-7890-bcde-f12345678906', 'c3d4e5f6-a7b8-9012-cdef-123456780003', 'PENDING', null, null),

-- Service executions for service order 4
('c1d2e3f4-a5b6-7890-cdef-012345678907', 'b1c2d3e4-f5a6-7890-bcde-f12345678907', 'c3d4e5f6-a7b8-9012-cdef-123456780004', 'IN_PROGRESS', now() - interval '1 day', null),

-- Service executions for service order 5
('c1d2e3f4-a5b6-7890-cdef-012345678908', 'b1c2d3e4-f5a6-7890-bcde-f12345678908', 'c3d4e5f6-a7b8-9012-cdef-123456780005', 'COMPLETED', now() - interval '2 days', now() - interval '2 days' + interval '1.5 hours'),
('c1d2e3f4-a5b6-7890-cdef-012345678909', 'b1c2d3e4-f5a6-7890-bcde-f12345678909', 'c3d4e5f6-a7b8-9012-cdef-123456780005', 'PENDING', null, null),
('c1d2e3f4-a5b6-7890-cdef-01234567890a', 'b1c2d3e4-f5a6-7890-bcde-f1234567890a', 'c3d4e5f6-a7b8-9012-cdef-123456780005', 'PENDING', null, null),

-- Service executions for service order 6
('c1d2e3f4-a5b6-7890-cdef-01234567890b', 'b1c2d3e4-f5a6-7890-bcde-f12345678901', 'c3d4e5f6-a7b8-9012-cdef-123456780006', 'COMPLETED', now() - interval '1 day', now() - interval '1 day' + interval '45 minutes'),
('c1d2e3f4-a5b6-7890-cdef-01234567890c', 'b1c2d3e4-f5a6-7890-bcde-f12345678904', 'c3d4e5f6-a7b8-9012-cdef-123456780006', 'COMPLETED', now() - interval '1 day' + interval '2 hours', now() - interval '1 day' + interval '3 hours');

