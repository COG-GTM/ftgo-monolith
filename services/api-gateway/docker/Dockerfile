FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="FTGO Platform Team"
LABEL service="api-gateway"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY build/libs/api-gateway-*.jar app.jar

RUN chown -R ftgo:ftgo /app

USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
