create table courier
(
  id                       bigint not null auto_increment,
  available                bit,
  first_name               varchar(255),
  last_name                varchar(255),
  street1                  varchar(255),
  street2                  varchar(255),
  city                     varchar(255),
  state                    varchar(255),
  zip                      varchar(255),
  latitude                 double null,
  longitude                double null,
  current_latitude         double null,
  current_longitude        double null,
  last_location_update     datetime null,
  primary key (id)
) engine = InnoDB;

create table courier_actions
(
  courier_id bigint not null,
  order_id   bigint,
  time       datetime,
  type       varchar(255)
) engine = InnoDB;

alter table courier_actions
  add constraint courier_actions_courier_id foreign key (courier_id) references courier (id);
