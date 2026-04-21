CREATE TABLE vehicles
(
    id            UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    customer_id   UUID                NOT NULL,
    license_plate VARCHAR(7)          NOT NULL UNIQUE,
    model         VARCHAR(255)        NOT NULL,
    brand         VARCHAR(255)        NOT NULL,
    color         VARCHAR(50)         NOT NULL,
    year          INTEGER             NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP                    DEFAULT now(),
    updated_at    TIMESTAMP                    DEFAULT now(),

    CONSTRAINT fk_vehicles_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (id)
);