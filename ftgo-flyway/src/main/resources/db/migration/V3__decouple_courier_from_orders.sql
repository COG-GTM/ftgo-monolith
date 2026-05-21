-- Drop the foreign key constraint between orders and courier tables
-- The Order Service now stores assigned_courier_id as a plain bigint
-- without a JPA/FK relationship to the courier table.
-- This allows the Courier Service to manage its own schema independently.

ALTER TABLE orders DROP FOREIGN KEY orders_assigned_courier_id;
