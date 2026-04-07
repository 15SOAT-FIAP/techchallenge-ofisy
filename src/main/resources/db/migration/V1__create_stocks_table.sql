CREATE TABLE stocks
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    product_name  VARCHAR(255)   NOT NULL,
    description   VARCHAR(255),
    quantity      INTEGER        NOT NULL DEFAULT 0,
    category      VARCHAR(255),
    unit_price    DECIMAL(10, 2) NOT NULL,
    min_threshold INTEGER        NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP      NOT NULL DEFAULT now()
);