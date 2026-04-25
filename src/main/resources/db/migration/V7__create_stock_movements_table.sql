CREATE TABLE stock_movements
(
    id                  UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    stock_id            UUID                NOT NULL,
    movement_type       VARCHAR(50)         NOT NULL,
    quantity            INTEGER             NOT NULL,
    previous_quantity   INTEGER             NOT NULL,
    new_quantity        INTEGER             NOT NULL,
    created_at          TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP           DEFAULT now(),

    CONSTRAINT fk_stock_movements_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks(id)
);