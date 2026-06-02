create table consumers
(
  id         bigint not null,
  first_name varchar(255),
  last_name  varchar(255),
  primary key (id)
);

-- Backing object for @GeneratedValue id generation. On H2 (which supports
-- sequences) Hibernate's default AUTO strategy uses a sequence named
-- "hibernate_sequence" rather than the MySQL-style table used by the monolith.
create sequence hibernate_sequence start with 1 increment by 1;
