FROM openjdk:8u171-jdk-alpine
RUN apk add --no-cache bash curl
WORKDIR /app
COPY build/libs/ftgo-consumer-service.jar .
EXPOSE 8080
CMD java ${JAVA_OPTS} -jar ftgo-consumer-service.jar
