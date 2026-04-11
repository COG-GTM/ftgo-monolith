create table consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
);

create table hibernate_sequence
(
  sequence_name varchar(255) not null,
  next_val bigint,
  primary key (sequence_name)
);

insert into hibernate_sequence (sequence_name, next_val) values ('default', 1);
