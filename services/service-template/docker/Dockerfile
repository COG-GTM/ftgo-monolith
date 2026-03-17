FROM openjdk:8-jdk-alpine

ARG JAR_FILE={{service-name}}-impl/build/libs/*.jar

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo
USER ftgo:ftgo

COPY ${JAR_FILE} app.jar

EXPOSE {{port}}

ENTRYPOINT ["java", "-jar", "/app.jar"]
