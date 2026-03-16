# =============================================================================
# Restaurant Service - Multi-stage Docker Build
# =============================================================================
# Build: docker build -f services/restaurant-service/docker/Dockerfile -t restaurant-service .
# Run:   docker run -p 8083:8083 restaurant-service
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Builder - Compile and package with Gradle
# ---------------------------------------------------------------------------
FROM gradle:4.10.2-jdk8 AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build configuration first for dependency caching
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Copy shared build sources
COPY buildSrc/ buildSrc/

# Copy shared libraries that the service depends on
COPY shared-libraries/ shared-libraries/
COPY ftgo-common/ ftgo-common/
COPY ftgo-common-jpa/ ftgo-common-jpa/
COPY ftgo-domain/ ftgo-domain/

# Copy the service source code
COPY services/restaurant-service/ services/restaurant-service/

# Build the service JAR (offline dependencies are cached in the layer above)
RUN gradle :services:restaurant-service:build -x test --no-daemon || true

# ---------------------------------------------------------------------------
# Stage 2: Runtime - Minimal JRE image
# ---------------------------------------------------------------------------
FROM eclipse-temurin:8-jre-alpine

LABEL service="restaurant-service"
LABEL maintainer="ftgo-team"
LABEL org.opencontainers.image.source="https://github.com/COG-GTM/ftgo-monolith"
LABEL org.opencontainers.image.description="FTGO Restaurant Service"

# Install curl for health checks and dumb-init for proper signal handling
RUN apk --no-cache add curl dumb-init

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set up application directory
RUN mkdir -p /app && chown appuser:appgroup /app
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/services/restaurant-service/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the service port
EXPOSE 8083

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Use dumb-init as PID 1 for proper signal handling (SIGTERM, etc.)
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
