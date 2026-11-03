CREATE TABLE outbox_events (
    event_id         UUID         PRIMARY KEY,
    event_type       VARCHAR(100) NOT NULL,
    service_order_id UUID         NOT NULL,
    payload          JSONB        NOT NULL,
    occurred_at      TIMESTAMPTZ  NOT NULL,
    published_at     TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_pending ON outbox_events (occurred_at) WHERE published_at IS NULL;