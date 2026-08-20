create database ftgo;
-- Least-privilege grant for the application account. Schema DDL is applied by the
-- ftgo-flyway Gradle plugin as root (see ftgo-flyway/build.gradle), and the app
-- itself has no Flyway dependency and runs with spring.jpa.generate-ddl=false, so
-- the runtime account only needs DML. This drops ALL PRIVILEGES and WITH GRANT
-- OPTION, so the account can no longer re-grant privileges to other users.
GRANT SELECT, INSERT, UPDATE, DELETE ON ftgo.* TO 'mysqluser'@'%';
