-- V7__create_service_execution_times_table.sql
CREATE TABLE service_execution_times (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL UNIQUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_service_execution_times_service_id ON service_execution_times(service_id);
CREATE INDEX idx_service_execution_times_start_date ON service_execution_times(start_date);
CREATE INDEX idx_service_execution_times_end_date ON service_execution_times(end_date);
