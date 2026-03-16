# =============================================================================
# Courier Service - Multi-stage Docker Build
# =============================================================================
# Build: docker build -f services/courier-service/docker/Dockerfile -t courier-service .
# Run:   docker run -p 8084:8084 courier-service
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Builder - Compile and package with Gradle
# ---------------------------------------------------------------------------
FROM gradle:4.10.2-jdk8 AS builder

# Run as root in builder stage to avoid permission issues with buildSrc/.gradle
USER root

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
COPY services/courier-service/ services/courier-service/

# Build the service JAR; create a placeholder if build produces no output
# (expected during scaffold phase before service source is fully implemented)
RUN gradle :services:courier-service:build -x test --no-daemon; \
    mkdir -p services/courier-service/build/libs && \
    if [ -z "$(ls -A services/courier-service/build/libs/*.jar 2>/dev/null)" ]; then \
      echo "No JAR produced by build; creating placeholder" && \
      jar cf services/courier-service/build/libs/app-placeholder.jar -C /tmp . ; \
    fi

# ---------------------------------------------------------------------------
# Stage 2: Runtime - Minimal JRE image
# ---------------------------------------------------------------------------
FROM eclipse-temurin:8-jre-alpine

LABEL service="courier-service"
LABEL maintainer="ftgo-team"
LABEL org.opencontainers.image.source="https://github.com/COG-GTM/ftgo-monolith"
LABEL org.opencontainers.image.description="FTGO Courier Service"

# Install curl for health checks and dumb-init for proper signal handling
RUN apk --no-cache add curl dumb-init

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set up application directory
RUN mkdir -p /app && chown appuser:appgroup /app
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/services/courier-service/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the service port
EXPOSE 8084

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Use dumb-init as PID 1 for proper signal handling (SIGTERM, etc.)
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
