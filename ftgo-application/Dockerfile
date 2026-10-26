# Java 10 runtime; openjdk:10-*-alpine JRE variants were never published,
# so use the AdoptOpenJDK Java 10 Alpine image (Debian slim variants of
# openjdk:10 point at an unsigned Debian sid apt repo that no longer resolves).
FROM adoptopenjdk/openjdk10:jdk-10.0.2.13-alpine-slim
RUN apk --no-cache add curl
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
