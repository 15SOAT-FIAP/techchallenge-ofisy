-- V4__Create_service_execution_times_table.sql
CREATE TABLE service_execution_times (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL UNIQUE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_service_execution_times_service_id ON service_execution_times(service_id);
CREATE INDEX idx_service_execution_times_start_date ON service_execution_times(start_date);
CREATE INDEX idx_service_execution_times_end_date ON service_execution_times(end_date);

