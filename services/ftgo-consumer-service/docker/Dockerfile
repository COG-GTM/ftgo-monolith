# FTGO Consumer Service Dockerfile
# Multi-stage build for the Consumer microservice

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY ../.. /workspace
WORKDIR /workspace
RUN ./gradlew :services:ftgo-consumer-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/ftgo-consumer-service/build/libs/*.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
