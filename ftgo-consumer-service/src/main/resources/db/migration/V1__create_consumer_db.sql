create table consumers (
  id         bigint not null auto_increment,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
) engine = InnoDB;
