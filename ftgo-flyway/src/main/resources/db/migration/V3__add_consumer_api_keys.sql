use ftgo;

-- Per-consumer API key, stored as a SHA-256 hex digest of the key handed to the
-- consumer once at registration time. Consumers created before this migration
-- have no key and cannot authenticate until one is provisioned.
ALTER TABLE consumers ADD COLUMN api_key_hash VARCHAR(64) NULL;

CREATE UNIQUE INDEX idx_consumers_api_key_hash ON consumers (api_key_hash);
