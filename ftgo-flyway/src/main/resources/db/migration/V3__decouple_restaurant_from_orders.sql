use ftgo;

-- Add restaurant_name column to orders table for denormalized storage
ALTER TABLE orders ADD COLUMN restaurant_name VARCHAR(255) NULL;

-- Populate restaurant_name from the restaurants table for existing orders
UPDATE orders o
  JOIN restaurants r ON o.restaurant_id = r.id
  SET o.restaurant_name = r.name
  WHERE o.restaurant_id IS NOT NULL;

-- Drop the foreign key constraint between orders and restaurants
ALTER TABLE orders DROP FOREIGN KEY orders_restaurant_id;
