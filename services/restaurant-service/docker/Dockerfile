# =============================================================================
# FTGO Restaurant Service - Multi-Stage Docker Build
# =============================================================================
# Stage 1: Build the application with Gradle
# Stage 2: Create minimal runtime image with JRE 17
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Builder - Compile and package the application
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy Gradle wrapper and build configuration first (for dependency caching)
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
COPY buildSrc/ buildSrc/

# Copy shared library sources needed for compilation
COPY shared-libraries/ shared-libraries/
COPY services/restaurant-service-api/ services/restaurant-service-api/

# Copy service source
COPY services/restaurant-service/ services/restaurant-service/

# Build the service JAR (skip tests for Docker build - tests run in CI)
RUN chmod +x gradlew && \
    ./gradlew :services:restaurant-service:bootJar --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2: Runtime - Minimal JRE image
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime

# Add metadata labels
LABEL org.opencontainers.image.source="https://github.com/COG-GTM/ftgo-monolith"
LABEL org.opencontainers.image.description="FTGO Restaurant Service"
LABEL org.opencontainers.image.title="restaurant-service"

# Install curl for health checks and dumb-init for proper signal handling
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl dumb-init && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd --gid 1001 ftgo && \
    useradd --uid 1001 --gid ftgo --shell /bin/bash --create-home ftgo

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/services/restaurant-service/build/libs/restaurant-service-*.jar app.jar

# Set ownership to non-root user
RUN chown -R ftgo:ftgo /app

# Switch to non-root user
USER ftgo

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use dumb-init for proper PID 1 signal handling
ENTRYPOINT ["dumb-init", "--"]

# JVM tuning for containers
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+UseG1GC", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "app.jar"]
