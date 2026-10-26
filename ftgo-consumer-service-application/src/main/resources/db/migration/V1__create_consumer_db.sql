create table consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
);

create sequence hibernate_sequence start with 1 increment by 1;
