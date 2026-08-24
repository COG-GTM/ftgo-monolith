use ftgo;

-- Client identifiers are no longer collected: query strings can carry personal data and
-- user agents identify individual clients.
ALTER TABLE api_request_log DROP COLUMN query_string;
ALTER TABLE api_request_log DROP COLUMN user_agent;
