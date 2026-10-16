# MySQL 8.0 replaces the EOL 5.7.13 image; Connector/J 8 (com.mysql:mysql-connector-j) talks to it
# with useSSL=false&allowPublicKeyRetrieval=true (see docker-compose.yml / application.properties).
FROM mysql:8.0
# Creates the `ftgo` database and grants on first container start.
COPY schema.sql /docker-entrypoint-initdb.d/schema.sql
