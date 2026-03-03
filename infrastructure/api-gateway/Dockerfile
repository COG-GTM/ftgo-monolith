# =============================================================================
# FTGO API Gateway - Multi-stage Docker Build
# =============================================================================
# Stage 1: Build the application with Gradle
# Stage 2: Create a minimal runtime image
# =============================================================================

# --- Build Stage ---
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first for better layer caching
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Download dependencies (cached unless build files change)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-jammy

# Security: run as non-root user
RUN groupadd -r ftgo && useradd -r -g ftgo -d /app ftgo

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership
RUN chown -R ftgo:ftgo /app

USER ftgo

# Expose gateway port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
