create table consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
);

create table courier
(
  id         bigserial not null,
  available  boolean,
  first_name varchar(255),
  last_name  varchar(255),
  street1    varchar(255),
  street2    varchar(255),
  city       varchar(255),
  state      varchar(255),
  zip        varchar(255),
  primary key (id)
);

create table courier_actions
(
  courier_id bigint not null,
  order_id   bigint,
  time       timestamp,
  type       varchar(255)
);

create sequence hibernate_sequence start with 1 increment by 50;

create table order_line_items
(
  order_id     bigint  not null,
  menu_item_id varchar(255),
  name         varchar(255),
  price        decimal(19, 2),
  quantity     integer not null
);

create table orders
(
  id                       bigserial not null,
  accept_time              timestamp,
  consumer_id              bigint,
  delivery_address_city    varchar(255),
  delivery_address_state   varchar(255),
  delivery_address_street1 varchar(255),
  delivery_address_street2 varchar(255),
  delivery_address_zip     varchar(255),
  delivery_time            timestamp,
  order_state              varchar(255),
  order_minimum            decimal(19, 2),
  payment_token            varchar(255),
  picked_up_time           timestamp,
  delivered_time           timestamp,
  preparing_time           timestamp,
  previous_ticket_state    integer,
  ready_by                 timestamp,
  ready_for_pickup_time    timestamp,
  version                  bigint,
  assigned_courier_id      bigint,
  restaurant_id            bigint,
  primary key (id)
);

create table restaurant_menu_items
(
  restaurant_id bigint not null,
  id            varchar(255),
  name          varchar(255),
  price         decimal(19, 2)
);

create table restaurants
(
  id      bigserial not null,
  name    varchar(255),
  street1 varchar(255),
  street2 varchar(255),
  city    varchar(255),
  state   varchar(255),
  zip     varchar(255),
  primary key (id)
);

alter table courier_actions
  add constraint courier_actions_order_id foreign key (order_id) references orders (id);

alter table courier_actions
  add constraint courier_actions_courier_id foreign key (courier_id) references courier (id);

alter table order_line_items
  add constraint order_line_items_id foreign key (order_id) references orders (id);

alter table orders
  add constraint orders_assigned_courier_id foreign key (assigned_courier_id) references courier (id);

alter table orders
  add constraint orders_restaurant_id foreign key (restaurant_id) references restaurants (id);

alter table restaurant_menu_items
  add constraint restaurant_menu_items_restaurant_id foreign key (restaurant_id) references restaurants (id);
