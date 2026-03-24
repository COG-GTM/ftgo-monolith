create table orders
(
  id                       bigint not null auto_increment,
  version                  bigint,
  consumer_id              bigint,
  restaurant_id            bigint,
  order_state              varchar(255),
  order_minimum            decimal(19, 2),
  ready_by                 datetime,
  accept_time              datetime,
  preparing_time           datetime,
  ready_for_pickup_time    datetime,
  picked_up_time           datetime,
  delivered_time           datetime,
  assigned_courier_id      bigint,
  delivery_time            datetime,
  delivery_address_street1 varchar(255),
  delivery_address_street2 varchar(255),
  delivery_address_city    varchar(255),
  delivery_address_state   varchar(255),
  delivery_address_zip     varchar(255),
  payment_token            varchar(255),
  primary key (id)
) engine = InnoDB;

create table order_line_items
(
  order_id    bigint not null,
  menu_item_id varchar(255),
  name        varchar(255),
  price       decimal(19, 2),
  quantity    int
) engine = InnoDB;

alter table order_line_items
  add constraint order_line_items_order_id foreign key (order_id) references orders (id);
