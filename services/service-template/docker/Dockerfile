FROM openjdk:8-jre-alpine

# Replace <service-name> and <port> with actual values
LABEL service="<service-name>"
LABEL maintainer="ftgo-team"

RUN mkdir /app
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE <port>

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:<port>/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
