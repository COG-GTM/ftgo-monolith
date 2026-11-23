-- Add location tracking to courier table
ALTER TABLE courier ADD COLUMN current_latitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN current_longitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN last_location_update DATETIME NULL;

-- Add lat/lng to courier address
ALTER TABLE courier ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN longitude DOUBLE NULL;
