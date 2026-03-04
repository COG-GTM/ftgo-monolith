# FTGO Courier Service Dockerfile
# Multi-stage build for the Courier microservice

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY ../.. /workspace
WORKDIR /workspace
RUN ./gradlew :services:ftgo-courier-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/ftgo-courier-service/build/libs/*.jar app.jar

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
