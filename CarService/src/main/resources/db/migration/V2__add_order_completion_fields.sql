ALTER TABLE repair_orders
    ADD COLUMN final_price NUMERIC(12, 2);

ALTER TABLE repair_orders
    ADD COLUMN completed_at TIMESTAMP;

ALTER TABLE repair_orders
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;