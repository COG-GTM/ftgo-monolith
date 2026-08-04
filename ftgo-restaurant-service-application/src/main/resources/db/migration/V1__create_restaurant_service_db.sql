create table restaurants
(
  id        bigint not null auto_increment,
  name      varchar(255),
  street1   varchar(255),
  street2   varchar(255),
  city      varchar(255),
  state     varchar(255),
  zip       varchar(255),
  latitude  double,
  longitude double,
  primary key (id)
);

create table restaurant_menu_items
(
  restaurant_id bigint not null,
  id            varchar(255),
  name          varchar(255),
  price         decimal(19, 2)
);

alter table restaurant_menu_items
  add constraint restaurant_menu_items_restaurant_id foreign key (restaurant_id) references restaurants (id);

-- The shared api tracking concern writes one row per request; it is part of the platform,
-- so the extracted service owns a copy of the table in its own schema.
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
  request_timestamp datetime,
  primary key (id)
);

create index idx_api_log_timestamp on api_request_log (request_timestamp);
create index idx_api_log_correlation on api_request_log (correlation_id);
create index idx_api_log_status on api_request_log (response_status);
