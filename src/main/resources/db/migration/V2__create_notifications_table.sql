CREATE TABLE notifications
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    type       VARCHAR(50)  NOT NULL,
    stock_id   UUID,
    message    VARCHAR(500) NOT NULL,
    read       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_notifications_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks (id)
);
