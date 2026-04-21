-- V8__Add_foreign_key_service_execution_time.sql
ALTER TABLE services
ADD CONSTRAINT fk_services_execution_time
FOREIGN KEY (service_execution_time_id) REFERENCES service_execution_times(id) ON DELETE SET NULL;
