use ftgo;

create table user_accounts
(
  id            bigint       not null auto_increment,
  username      varchar(255) not null,
  password_hash varchar(255) not null,
  role          varchar(32)  not null,
  entity_id     bigint       null,
  primary key (id),
  unique key user_accounts_username_unique (username)
) engine = InnoDB;
