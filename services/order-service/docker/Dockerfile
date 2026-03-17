FROM openjdk:8-jdk-alpine

ARG JAR_FILE=order-service-impl/build/libs/*.jar

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo
USER ftgo:ftgo

COPY ${JAR_FILE} app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app.jar"]
