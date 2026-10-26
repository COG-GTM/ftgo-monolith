use ftgo;

ALTER TABLE consumers ADD COLUMN password_hash VARCHAR(60) NULL;
