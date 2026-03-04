# FTGO Order Service Dockerfile
# Multi-stage build for the Order microservice

FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY ../.. /workspace
WORKDIR /workspace
RUN ./gradlew :services:ftgo-order-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/services/ftgo-order-service/build/libs/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
