CREATE TABLE users (
                       id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                       name            VARCHAR(255)    NOT NULL,
                       email           VARCHAR(255)    NOT NULL UNIQUE,
                       password        VARCHAR(255)    NOT NULL,
                       role            VARCHAR(50)     NOT NULL,
                       active          BOOLEAN         NOT NULL DEFAULT true,
                       created_at      TIMESTAMP       NOT NULL,
                       updated_at      TIMESTAMP
);