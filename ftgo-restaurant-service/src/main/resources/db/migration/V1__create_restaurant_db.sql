create table restaurants (
  id bigint not null auto_increment,
  name varchar(255),
  street1 varchar(255),
  street2 varchar(255),
  city varchar(255),
  state varchar(255),
  zip varchar(255),
  primary key (id)
) engine = InnoDB;

create table restaurant_menu_items (
  restaurant_id bigint not null,
  id varchar(255),
  name varchar(255),
  price decimal(19, 2)
) engine = InnoDB;

alter table restaurant_menu_items
  add constraint restaurant_menu_items_restaurant_id foreign key (restaurant_id) references restaurants (id);

create table hibernate_sequence (
  next_val bigint
) engine = InnoDB;

insert into hibernate_sequence values (1);
