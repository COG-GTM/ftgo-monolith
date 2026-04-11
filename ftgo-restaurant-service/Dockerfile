FROM openjdk:8u171-jdk-alpine
RUN apk add --no-cache bash curl
WORKDIR /app
COPY build/libs/ftgo-restaurant-service.jar .
EXPOSE 8081
CMD java ${JAVA_OPTS} -jar ftgo-restaurant-service.jar
