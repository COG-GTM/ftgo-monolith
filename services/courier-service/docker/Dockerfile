FROM openjdk:8-jre-alpine

LABEL service="courier-service"
LABEL maintainer="ftgo-team"

RUN mkdir /app
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
