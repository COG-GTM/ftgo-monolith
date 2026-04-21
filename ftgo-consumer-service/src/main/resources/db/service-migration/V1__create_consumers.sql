create table if not exists consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
) engine = InnoDB;

create table if not exists hibernate_sequence
(
  next_val bigint
) engine = InnoDB;

insert into hibernate_sequence
values (1);
