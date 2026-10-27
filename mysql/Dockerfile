FROM mysql:8.0
COPY schema.sql /docker-entrypoint-initdb.d/schema.sql
# Flyway connects with useSSL=false and no allowPublicKeyRetrieval, which caching_sha2_password rejects
CMD ["mysqld", "--default-authentication-plugin=mysql_native_password"]
