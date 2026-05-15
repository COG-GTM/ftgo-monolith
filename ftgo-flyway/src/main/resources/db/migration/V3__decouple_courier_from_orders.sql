-- Drop the foreign key constraint between orders and courier tables
-- The Order Service now stores assigned_courier_id as a plain bigint
-- without a JPA/FK relationship to the courier table.
-- This allows the Courier Service to manage its own schema independently.

ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_orders_assigned_courier;

-- Also drop the JPA-generated FK if it has a different name
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT constraint_name
              FROM information_schema.table_constraints
              WHERE table_name = 'orders'
                AND constraint_type = 'FOREIGN KEY'
                AND constraint_name LIKE '%courier%')
    LOOP
        EXECUTE 'ALTER TABLE orders DROP CONSTRAINT ' || r.constraint_name;
    END LOOP;
END $$;
