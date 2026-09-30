# Migrations image for the Helm chart (deployment/helm/ftgo): runs `flyway migrate` over the SQL files below.
# Built by scripts/build-images.sh (build context: ftgo-flyway/).
FROM flyway/flyway:10.17.3

# Same JDBC driver as the app, so jdbc:mysql URLs and query parameters (e.g. sslMode) behave identically.
# Flyway otherwise falls back to its bundled MariaDB driver, which ignores sslMode.
ARG MAVEN_MIRROR_URL=https://repo1.maven.org/maven2
ADD --checksum=sha256:e2a3b2fc726a1ac64e998585db86b30fa8bf3f706195b78bb77c5f99bf877bd9 \
    ${MAVEN_MIRROR_URL}/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar /flyway/drivers/
RUN chmod 644 /flyway/drivers/mysql-connector-j-8.0.33.jar

COPY src/main/resources/db/migration /flyway/sql

ENV FLYWAY_LOCATIONS=filesystem:/flyway/sql \
    REDGATE_DISABLE_TELEMETRY=true

USER 1000:1000

CMD ["migrate"]
