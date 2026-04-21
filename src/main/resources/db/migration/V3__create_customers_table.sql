CREATE TABLE customers
(
    id         UUID PRIMARY KEY    DEFAULT gen_random_uuid(),
    cpf_cnpj   VARCHAR(14)         NOT NULL UNIQUE,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255)        NOT NULL UNIQUE,
    phone      VARCHAR(20)         NOT NULL,
    created_at TIMESTAMP                    DEFAULT now(),
    updated_at TIMESTAMP                    DEFAULT now()
);