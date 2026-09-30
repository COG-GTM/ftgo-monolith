use ftgo;

ALTER TABLE courier ADD COLUMN access_token_hash VARCHAR(64) NULL;
CREATE UNIQUE INDEX idx_courier_access_token_hash ON courier (access_token_hash);
