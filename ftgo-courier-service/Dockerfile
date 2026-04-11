FROM openjdk:8u171-jdk-alpine
RUN apk add --no-cache bash curl
WORKDIR /app
COPY build/libs/ftgo-courier-service.jar .
EXPOSE 8082
CMD java ${JAVA_OPTS} -jar ftgo-courier-service.jar
