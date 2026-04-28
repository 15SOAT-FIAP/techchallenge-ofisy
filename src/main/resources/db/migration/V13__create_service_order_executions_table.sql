-- V13__create_service_order_executions_table.sql
CREATE TABLE service_order_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_catalog_id UUID NOT NULL,
    service_order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    finished_at TIMESTAMP,
    started_at TIMESTAMP,
    FOREIGN KEY (service_catalog_id) REFERENCES services_catalog(id) ON DELETE RESTRICT,
    FOREIGN KEY (service_order_id) REFERENCES service_orders(id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_service_order_executions_service_catalog_id ON service_order_executions(service_catalog_id);
CREATE INDEX idx_service_order_executions_status ON service_order_executions(status);
CREATE INDEX idx_service_order_executions_created_at ON service_order_executions(created_at);
