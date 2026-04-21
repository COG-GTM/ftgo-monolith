create table if not exists orders
(
  id                       bigint not null auto_increment,
  accept_time              datetime,
  consumer_id              bigint,
  delivery_address_city    varchar(255),
  delivery_address_state   varchar(255),
  delivery_address_street1 varchar(255),
  delivery_address_street2 varchar(255),
  delivery_address_zip     varchar(255),
  delivery_address_latitude  double,
  delivery_address_longitude double,
  delivery_time            datetime,
  order_state              varchar(255),
  order_minimum            decimal(19, 2),
  payment_token            varchar(255),
  picked_up_time           datetime,
  delivered_time           datetime,
  preparing_time           datetime,
  previous_ticket_state    integer,
  ready_by                 datetime,
  ready_for_pickup_time    datetime,
  version                  bigint,
  assigned_courier_id      bigint,
  restaurant_id            bigint,
  restaurant_name          varchar(255),
  primary key (id)
) engine = InnoDB;

create table if not exists order_line_items
(
  order_id     bigint  not null,
  menu_item_id varchar(255),
  name         varchar(255),
  price        decimal(19, 2),
  quantity     integer not null
) engine = InnoDB;

alter table order_line_items
  add constraint fk_order_line_items_order_id foreign key (order_id) references orders (id);

create table if not exists hibernate_sequence
(
  next_val bigint
) engine = InnoDB;

insert into hibernate_sequence
values (1);
