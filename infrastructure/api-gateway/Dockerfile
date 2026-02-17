FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="FTGO Engineering <engineering@ftgo.com>"
LABEL service="ftgo-api-gateway"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY application.yml /app/config/application.yml

RUN chown -R ftgo:ftgo /app

USER ftgo

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/,/app/config/"]
