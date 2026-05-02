INSERT INTO quotes (id, service_order_id, status, total_price, quote_refusal_reason, created_at, updated_at) VALUES
('d1e2f3a4-b5c6-7890-defa-012345678901', 'c3d4e5f6-a7b8-9012-cdef-123456780001', 'PENDING', 840.00, NULL, now() - INTERVAL '1 day', now() - INTERVAL '1 day'),
('d1e2f3a4-b5c6-7890-defa-012345678902', 'c3d4e5f6-a7b8-9012-cdef-123456780003', 'APPROVED', 700.00, NULL, now() - INTERVAL '3 days',  now() - INTERVAL '2 days'),
('d1e2f3a4-b5c6-7890-defa-012345678903', 'c3d4e5f6-a7b8-9012-cdef-123456780008', 'REPROVED', 520.00, 'Cliente achou o valor muito alto e desistiu do serviço.', now() - INTERVAL '20 days', now() - INTERVAL '19 days'),
('d1e2f3a4-b5c6-7890-defa-012345678904', 'c3d4e5f6-a7b8-9012-cdef-123456780002', 'PENDING', 780.00, NULL, now() - INTERVAL '2 days', now());