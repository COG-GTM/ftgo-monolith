# =============================================================================
# FTGO Consumer Service — Multi-Stage Docker Build
# =============================================================================
# Produces a minimal JRE-based image (<200MB) suitable for production.
#
# Build context: repository root (not the service directory)
#   docker build -f services/ftgo-consumer-service/docker/Dockerfile .
#
# The build stage compiles the service JAR via Gradle; the runtime stage
# copies only the final artifact into a slim JRE image.
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Build
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS build

RUN apk add --no-cache bash

WORKDIR /workspace

# Copy Gradle wrapper and build configuration first (layer caching)
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
COPY buildSrc/ buildSrc/

# Copy shared libraries needed for compilation
COPY shared/ shared/

# Copy the service source
COPY services/ftgo-consumer-service/ services/ftgo-consumer-service/

# Build the service JAR (skip tests — they run in CI)
RUN chmod +x gradlew && \
    ./gradlew :services-ftgo-consumer-service:build -x test --no-daemon

# ---------------------------------------------------------------------------
# Stage 2: Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="ftgo-consumer-service" \
      org.opencontainers.image.description="FTGO Consumer Service microservice" \
      org.opencontainers.image.source="https://github.com/COG-GTM/ftgo-monolith" \
      org.opencontainers.image.vendor="COG-GTM"

# Security: run as non-root
RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

# Install wget for health checks
RUN apk add --no-cache wget

WORKDIR /app

COPY --from=build /workspace/services/ftgo-consumer-service/build/libs/*.jar app.jar

RUN chown ftgo:ftgo /app/app.jar

USER ftgo

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
