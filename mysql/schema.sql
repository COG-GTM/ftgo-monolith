create database ftgo;
-- Least-privilege grant for the application account: the DML it uses at runtime
-- plus the DDL the Flyway migrations need to create/alter the schema. Notably this
-- drops ALL PRIVILEGES and WITH GRANT OPTION, so the account can no longer re-grant
-- privileges to other users. If schema migrations are applied by a separate admin
-- account, this can be tightened further to SELECT, INSERT, UPDATE, DELETE only.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES ON ftgo.* TO 'mysqluser'@'%';
