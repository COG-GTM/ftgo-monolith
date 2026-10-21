# Supported MySQL 8.0 LTS, pinned by digest (update tag+digest together).
FROM mysql:8.0.36@sha256:a532724022429812ec797c285c1b540a644c15e248579c6bfdf12a8fbaab4964
# Keep native-password auth so the legacy JDBC clients/credentials still connect.
CMD ["mysqld", "--default-authentication-plugin=mysql_native_password"]
COPY schema.sql /docker-entrypoint-initdb.d/schema.sql
