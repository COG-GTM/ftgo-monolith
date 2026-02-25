FROM openjdk:8-jdk-alpine

LABEL maintainer="FTGO Platform Team"
LABEL service="ftgo-<service-name>"

# Add a non-root user
RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

# Set working directory
WORKDIR /app

# Copy the application JAR
COPY build/libs/*.jar app.jar

# Expose the service port (override per service)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Run as non-root user
USER ftgo

# JVM options for containerized environments
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
