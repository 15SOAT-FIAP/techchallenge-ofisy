CREATE TABLE quote_service_items
(
    id                           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id                     UUID          NOT NULL,
    service_order_executions_id  UUID          NOT NULL,
    price                        DECIMAL(10, 2) NOT NULL,
    created_at           TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,

    CONSTRAINT fk_quote_service_items_quote
        FOREIGN KEY (quote_id)
            REFERENCES quotes (id),

    CONSTRAINT fk_quote_service_items_execution
        FOREIGN KEY (service_order_executions_id)
            REFERENCES service_order_executions (id),

    CONSTRAINT uq_quote_service_items
        UNIQUE (quote_id, service_order_executions_id)
);