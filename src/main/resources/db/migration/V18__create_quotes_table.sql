CREATE TABLE quotes
(
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id     UUID        NOT NULL UNIQUE,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price          DECIMAL(10, 2) NOT NULL,
    quote_refusal_reason TEXT,
    created_at           TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,

    CONSTRAINT fk_quotes_service_order
        FOREIGN KEY (service_order_id)
            REFERENCES service_orders (id)
);