CREATE TABLE notifications
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    stock_id   UUID         NOT NULL,
    message    VARCHAR(255) NOT NULL,
    read       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_notifications_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks (id)
);