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
