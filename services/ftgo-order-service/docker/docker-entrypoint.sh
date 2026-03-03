#!/bin/sh
# =============================================================================
# FTGO Order Service - Docker Entrypoint
# =============================================================================
# Provides runtime configuration and graceful startup/shutdown handling.
# =============================================================================

set -e

# Default JVM options for containerized environments
DEFAULT_JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Merge default and custom JVM options
JAVA_OPTS="${DEFAULT_JAVA_OPTS} ${JAVA_OPTS:-}"

# Log startup information
echo "Starting FTGO Order Service..."
echo "JVM Options: ${JAVA_OPTS}"
echo "Spring Profiles: ${SPRING_PROFILES_ACTIVE:-default}"

# Execute the application
exec java ${JAVA_OPTS} -jar /app/app.jar "$@"
