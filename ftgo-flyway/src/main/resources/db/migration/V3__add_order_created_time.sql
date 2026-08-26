use ftgo;

-- Time an order entered the APPROVED state, used for SLA aging.
ALTER TABLE orders ADD COLUMN created_time DATETIME NULL;

UPDATE orders
SET created_time = COALESCE(accept_time, preparing_time, ready_for_pickup_time, picked_up_time, delivered_time)
WHERE created_time IS NULL;

CREATE INDEX idx_orders_state_created_time ON orders (order_state, created_time);
