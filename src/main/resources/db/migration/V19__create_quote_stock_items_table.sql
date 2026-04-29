CREATE TABLE quote_stock_items
(
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id   UUID          NOT NULL,
    stock_id   UUID          NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity   INTEGER       NOT NULL,
    created_at           TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,

    CONSTRAINT fk_quote_stock_items_quote
        FOREIGN KEY (quote_id)
            REFERENCES quotes (id),

    CONSTRAINT fk_quote_stock_items_stock
        FOREIGN KEY (stock_id)
            REFERENCES stocks (id),

    CONSTRAINT uq_quote_stock_items
        UNIQUE (quote_id, stock_id)
);