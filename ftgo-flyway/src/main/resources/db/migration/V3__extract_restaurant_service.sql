-- V3: Extract Restaurant bounded context into a standalone microservice.
-- The Order entity no longer holds a JPA foreign-key to the restaurants table;
-- instead it stores a denormalized snapshot of the restaurant metadata it needs.

-- Drop the FK from orders -> restaurants
ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id;

-- Add denormalized restaurant columns to orders so the Order service owns its data
ALTER TABLE orders ADD COLUMN restaurant_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_street1 VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_street2 VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_city VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_state VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_zip VARCHAR(255);
ALTER TABLE orders ADD COLUMN restaurant_address_latitude DOUBLE;
ALTER TABLE orders ADD COLUMN restaurant_address_longitude DOUBLE;

-- Drop restaurant tables (now managed by the Restaurant microservice)
DROP TABLE IF EXISTS restaurant_menu_items;
DROP TABLE IF EXISTS restaurants;
