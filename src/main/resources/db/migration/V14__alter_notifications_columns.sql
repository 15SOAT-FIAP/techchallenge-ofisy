ALTER TABLE notifications
    ADD COLUMN type VARCHAR(100) NOT NULL;

ALTER TABLE notifications
    ALTER COLUMN stock_id DROP NOT NULL;
