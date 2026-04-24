CREATE TABLE service_orders
(
    id           UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    vehicle_id   UUID              NOT NULL,
    customer_id  UUID              NOT NULL,
    report       TEXT,
    status       VARCHAR(50)       NOT NULL,
    created_by   UUID              NOT NULL,
    created_at   TIMESTAMP         NOT NULL DEFAULT now(),
    finished_at  TIMESTAMP,
    updated_at   TIMESTAMP                  DEFAULT now(),

    CONSTRAINT fk_service_orders_vehicle
        FOREIGN KEY (vehicle_id)
            REFERENCES vehicles (id),
    CONSTRAINT fk_service_orders_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (id),
    CONSTRAINT fk_service_orders_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id),
    CONSTRAINT chk_service_orders_status
        CHECK ( status IN ('RECEIVED', 'IN_DIAGNOSTIC', 'AWAITING_APPROVAL', 'AWAITING_EXECUTION', 'IN_PROGRESS', 'FINISHED', 'SHIPPED', 'CANCELLED'))
);