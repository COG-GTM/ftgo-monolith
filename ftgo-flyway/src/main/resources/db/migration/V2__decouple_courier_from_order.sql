-- Migration to decouple Courier Service from Order Service
-- The orders table now stores only the courier ID (not a foreign key reference)
-- The courier_actions table now stores order_id directly (not a foreign key reference)

-- Drop the foreign key constraint from orders to courier
ALTER TABLE orders DROP FOREIGN KEY orders_assigned_courier_id;

-- Drop the foreign key constraint from courier_actions to orders
ALTER TABLE courier_actions DROP FOREIGN KEY courier_actions_order_id;

-- The courier_actions table now stores order_id as a simple bigint (no foreign key)
-- This allows the Courier Service to operate independently
