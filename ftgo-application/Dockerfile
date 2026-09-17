# Java 21 runtime for the Spring Boot 3.5 monolith (replaces the EOL openjdk:8 Alpine image).
FROM eclipse-temurin:21-jre
# curl is used by the HEALTHCHECK below (not present in the temurin base image).
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
# Spring Boot 3 exposes the health endpoint under /actuator/health.
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
