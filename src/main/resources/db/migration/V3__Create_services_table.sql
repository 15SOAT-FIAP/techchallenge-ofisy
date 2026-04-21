-- V3__Create_services_table.sql
CREATE TABLE services (
    id UUID PRIMARY KEY,
    catalog_service_id UUID NOT NULL,
    service_execution_time_id UUID,
    price DECIMAL(10, 2) NOT NULL,
    service_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (catalog_service_id) REFERENCES catalog_services(id) ON DELETE RESTRICT
);

CREATE INDEX idx_services_catalog_service_id ON services(catalog_service_id);
CREATE INDEX idx_services_service_status ON services(service_status);
CREATE INDEX idx_services_created_at ON services(created_at);

