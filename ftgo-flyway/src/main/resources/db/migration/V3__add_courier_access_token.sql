use ftgo;

-- Per-courier credential (SHA-256 of the bearer token issued at registration)
ALTER TABLE courier ADD COLUMN access_token_hash VARCHAR(64) NULL;
