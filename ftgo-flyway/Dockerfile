# Migrations image for the Helm chart (deployment/helm/ftgo): runs `flyway migrate` over the SQL files below.
# Build context is ftgo-flyway/:  docker build -t ftgo-flyway:dev ftgo-flyway
FROM flyway/flyway:10.17.3

COPY src/main/resources/db/migration /flyway/sql

ENV FLYWAY_LOCATIONS=filesystem:/flyway/sql \
    REDGATE_DISABLE_TELEMETRY=true

USER 1000:1000

CMD ["migrate"]
