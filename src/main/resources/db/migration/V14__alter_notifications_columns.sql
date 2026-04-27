ALTER TABLE notifications
    ADD COLUMN type VARCHAR(255) NOT NULL;

ALTER TABLE notifications
    ALTER COLUMN stock_id DROP NOT NULL;
