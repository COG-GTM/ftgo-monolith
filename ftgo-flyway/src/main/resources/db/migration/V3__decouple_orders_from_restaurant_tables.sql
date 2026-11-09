-- Orders keep a snapshot of the restaurant data they need instead of joining the restaurant service's tables.
ALTER TABLE orders ADD COLUMN restaurant_name VARCHAR(255) NULL;
ALTER TABLE orders ADD COLUMN restaurant_latitude DOUBLE NULL;
ALTER TABLE orders ADD COLUMN restaurant_longitude DOUBLE NULL;

-- One-time backfill of existing orders; nothing reads across these tables after this migration.
UPDATE orders o JOIN restaurants r ON o.restaurant_id = r.id
SET o.restaurant_name = r.name, o.restaurant_latitude = r.latitude, o.restaurant_longitude = r.longitude;

ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id;
