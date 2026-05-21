-- Remove consumers table from monolith database.
-- The consumer_id column in orders remains as an external reference
-- to the standalone consumer service.

drop table if exists consumers;
