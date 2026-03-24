create table if not exists courier
(
  id         bigint not null auto_increment,
  available  bit,
  first_name varchar(255),
  last_name  varchar(255),
  street1    varchar(255),
  street2    varchar(255),
  city       varchar(255),
  state      varchar(255),
  zip        varchar(255),
  primary key (id)
) engine = InnoDB;

create table if not exists courier_actions
(
  courier_id bigint not null,
  order_id   bigint,
  time       datetime,
  type       varchar(255)
) engine = InnoDB;

alter table courier_actions
  add constraint fk_courier_actions_courier_id foreign key (courier_id) references courier (id);

create table if not exists hibernate_sequence
(
  next_val bigint
) engine = InnoDB;

insert into hibernate_sequence
values (1);
