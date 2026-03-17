FROM openjdk:8-jre-alpine

# Replace <service-name> with the actual service name
ARG JAR_FILE=build/libs/<service-name>-*.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app.jar"]
