FROM openjdk:8-jre-alpine

LABEL maintainer="ftgo-team"
LABEL service="__SERVICE_NAME__"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY build/libs/__SERVICE_NAME__-*.jar app.jar

RUN chown -R ftgo:ftgo /app

USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
