-- Schema for the extracted consumer microservice (H2 in-memory).
-- Mirrors the `consumers` table that previously lived in the monolith's
-- shared FTGO database.

create table consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
);

-- Hibernate's default id generator (@GeneratedValue with AUTO strategy) uses a
-- sequence named hibernate_sequence on databases that support sequences (H2).
create sequence hibernate_sequence start with 1 increment by 1;

-- API request tracking table required by the shared CommonConfiguration
-- (net.chrisrichardson.ftgo.common.tracking.ApiRequestLog).
create table api_request_log
(
  id                bigint not null auto_increment,
  correlation_id    varchar(255),
  http_method       varchar(10),
  request_uri       varchar(1024),
  query_string      varchar(2048),
  response_status   integer,
  duration_ms       bigint,
  remote_addr       varchar(255),
  user_agent        varchar(1024),
  error_message     varchar(4000),
  request_timestamp timestamp,
  primary key (id)
);

create index idx_api_log_timestamp on api_request_log (request_timestamp);
create index idx_api_log_correlation on api_request_log (correlation_id);
create index idx_api_log_status on api_request_log (response_status);
create index idx_api_log_uri on api_request_log (request_uri);
