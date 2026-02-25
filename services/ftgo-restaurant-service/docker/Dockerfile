FROM openjdk:8-jdk-alpine

LABEL maintainer="FTGO Platform Team"
LABEL service="ftgo-restaurant-service"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8083/actuator/health || exit 1

USER ftgo

ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
