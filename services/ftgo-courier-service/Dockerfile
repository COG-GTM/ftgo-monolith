# =============================================================================
# FTGO Courier Service - Multi-stage Dockerfile
# =============================================================================
# Stage 1: Build the application using Gradle
# Stage 2: Create a minimal runtime image with JRE 17
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Builder
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install required build tools
RUN apk add --no-cache bash

# Copy Gradle wrapper and build configuration first for dependency caching
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
COPY buildSrc/ buildSrc/

# Copy shared libraries that the service depends on
COPY ftgo-common/ ftgo-common/
COPY ftgo-common-jpa/ ftgo-common-jpa/
COPY ftgo-domain/ ftgo-domain/
COPY ftgo-courier-service-api/ ftgo-courier-service-api/
COPY common-swagger/ common-swagger/

# Download dependencies (cached layer)
RUN chmod +x gradlew && \
    ./gradlew :ftgo-courier-service:dependencies --no-daemon 2>/dev/null || true

# Copy service source code
COPY ftgo-courier-service/ ftgo-courier-service/

# Build the service JAR
RUN ./gradlew :ftgo-courier-service:build -x test --no-daemon

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Add labels for container registry metadata
LABEL org.opencontainers.image.source="https://github.com/COG-GTM/ftgo-monolith"
LABEL org.opencontainers.image.description="FTGO Courier Service"
LABEL org.opencontainers.image.vendor="COG-GTM"

# Install tini for proper PID 1 signal handling and curl for health checks
RUN apk add --no-cache tini curl

# Create non-root user
RUN addgroup -g 1001 -S ftgo && \
    adduser -u 1001 -S ftgo -G ftgo

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/ftgo-courier-service/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R ftgo:ftgo /app

# Switch to non-root user
USER ftgo

# Expose service port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use tini as init system for proper signal handling
ENTRYPOINT ["tini", "--"]

# Run the application with JVM tuning for containers
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+UseG1GC", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]
