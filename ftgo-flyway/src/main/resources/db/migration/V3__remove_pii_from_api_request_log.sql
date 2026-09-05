use ftgo;

ALTER TABLE api_request_log DROP COLUMN query_string;
ALTER TABLE api_request_log DROP COLUMN remote_addr;
ALTER TABLE api_request_log DROP COLUMN user_agent;
