use ftgo;

-- Add location tracking to courier table
ALTER TABLE courier ADD COLUMN current_latitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN current_longitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN last_location_update DATETIME NULL;

-- Add lat/lng to courier address
ALTER TABLE courier ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE courier ADD COLUMN longitude DOUBLE NULL;

-- Add lat/lng to restaurant address
ALTER TABLE restaurants ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE restaurants ADD COLUMN longitude DOUBLE NULL;

-- Add lat/lng to delivery address on orders
ALTER TABLE orders ADD COLUMN delivery_address_latitude DOUBLE NULL;
ALTER TABLE orders ADD COLUMN delivery_address_longitude DOUBLE NULL;

-- Add denormalized restaurant name for microservices decomposition
ALTER TABLE orders ADD COLUMN restaurant_name VARCHAR(255) NULL;

-- API request tracking table
CREATE TABLE api_request_log
(
  id                BIGINT NOT NULL AUTO_INCREMENT,
  correlation_id    VARCHAR(255),
  http_method       VARCHAR(10),
  request_uri       VARCHAR(1024),
  query_string      VARCHAR(2048),
  response_status   INTEGER,
  duration_ms       BIGINT,
  remote_addr       VARCHAR(255),
  user_agent        VARCHAR(1024),
  error_message     VARCHAR(4000),
  request_timestamp DATETIME,
  PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_api_log_timestamp ON api_request_log (request_timestamp);
CREATE INDEX idx_api_log_correlation ON api_request_log (correlation_id);
CREATE INDEX idx_api_log_status ON api_request_log (response_status);
CREATE INDEX idx_api_log_uri ON api_request_log (request_uri(255));
