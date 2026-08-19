use ftgo;

-- Credential used to authenticate a consumer's API requests
ALTER TABLE consumers ADD COLUMN access_token VARCHAR(64) NULL;

CREATE UNIQUE INDEX idx_consumers_access_token ON consumers (access_token);
