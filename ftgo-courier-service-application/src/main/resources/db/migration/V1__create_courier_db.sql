-- Courier service schema (H2 in-memory).
-- Derived from the monolith's ftgo-flyway V1__create_ftgo_db.sql (courier + courier_actions)
-- plus the courier columns added in V2__add_courier_optimization_and_api_tracking.sql.
-- MySQL-specific syntax (engine = InnoDB) stripped for H2 compatibility.

create table courier
(
  id                   bigint not null auto_increment,
  available            boolean,
  first_name           varchar(255),
  last_name            varchar(255),
  street1              varchar(255),
  street2              varchar(255),
  city                 varchar(255),
  state                varchar(255),
  zip                  varchar(255),
  current_latitude     double,
  current_longitude    double,
  last_location_update timestamp,
  latitude             double,
  longitude            double,
  primary key (id)
);

create table courier_actions
(
  courier_id bigint not null,
  order_id   bigint,
  time       timestamp,
  type       varchar(255)
);

alter table courier_actions
  add constraint courier_actions_courier_id foreign key (courier_id) references courier (id);

-- Shared sequence table (kept for parity with the monolith; entities use IDENTITY generation).
create table hibernate_sequence
(
  next_val bigint
);

insert into hibernate_sequence
values (1);
