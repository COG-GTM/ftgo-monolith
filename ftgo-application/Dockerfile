# =============================================================================
# FTGO Application (Monolith) - Multi-stage Docker Build
# =============================================================================
# Build context: repository root (use -f ftgo-application/Dockerfile)
# Stage 1: Gradle build with dependency caching
# Stage 2: Minimal JRE 17 runtime
# =============================================================================

# ---------------------------------------------------------------------------
# Stage 1: Builder
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copy Gradle wrapper and build config first for dependency caching
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./
COPY buildSrc/ buildSrc/

# Copy all module build files for dependency resolution
COPY shared-libraries/ftgo-common/build.gradle shared-libraries/ftgo-common/
COPY shared-libraries/ftgo-common-jpa/build.gradle shared-libraries/ftgo-common-jpa/
COPY shared-libraries/common-swagger/build.gradle shared-libraries/common-swagger/
COPY shared-libraries/ftgo-domain/build.gradle shared-libraries/ftgo-domain/
COPY shared-libraries/ftgo-test-util/build.gradle shared-libraries/ftgo-test-util/
COPY services/consumer-service/build.gradle services/consumer-service/
COPY services/consumer-service-api/build.gradle services/consumer-service-api/
COPY services/order-service/build.gradle services/order-service/
COPY services/order-service-api/build.gradle services/order-service-api/
COPY services/restaurant-service/build.gradle services/restaurant-service/
COPY services/restaurant-service-api/build.gradle services/restaurant-service-api/
COPY services/courier-service/build.gradle services/courier-service/
COPY services/courier-service-api/build.gradle services/courier-service-api/
COPY ftgo-common/build.gradle ftgo-common/
COPY ftgo-common-jpa/build.gradle ftgo-common-jpa/
COPY ftgo-domain/build.gradle ftgo-domain/
COPY ftgo-test-util/build.gradle ftgo-test-util/
COPY common-swagger/build.gradle common-swagger/
COPY ftgo-flyway/build.gradle ftgo-flyway/
COPY ftgo-order-service/build.gradle ftgo-order-service/
COPY ftgo-order-service-api/build.gradle ftgo-order-service-api/
COPY ftgo-consumer-service/build.gradle ftgo-consumer-service/
COPY ftgo-consumer-service-api/build.gradle ftgo-consumer-service-api/
COPY ftgo-restaurant-service/build.gradle ftgo-restaurant-service/
COPY ftgo-restaurant-service-api/build.gradle ftgo-restaurant-service-api/
COPY ftgo-courier-service/build.gradle ftgo-courier-service/
COPY ftgo-courier-service-api/build.gradle ftgo-courier-service-api/
COPY ftgo-application/build.gradle ftgo-application/
COPY ftgo-end-to-end-tests-common/build.gradle ftgo-end-to-end-tests-common/
COPY ftgo-end-to-end-tests/build.gradle ftgo-end-to-end-tests/

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

# Copy full source
COPY . .

# Build the application JAR (skip tests - they run in CI separately)
RUN ./gradlew :ftgo-application:bootJar --no-daemon -x test

# ---------------------------------------------------------------------------
# Stage 2: Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy

# Add non-root user
RUN groupadd --gid 1001 appuser && \
    useradd --uid 1001 --gid appuser --shell /bin/bash --create-home appuser

WORKDIR /app

# Install curl for health checks and dumb-init for proper signal handling
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl dumb-init && \
    rm -rf /var/lib/apt/lists/*

# Copy built JAR from builder
COPY --from=builder /build/ftgo-application/build/libs/ftgo-application-*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
