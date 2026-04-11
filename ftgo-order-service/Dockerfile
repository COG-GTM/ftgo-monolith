FROM openjdk:8u171-jdk-alpine
RUN apk add --no-cache bash curl
WORKDIR /app
COPY build/libs/ftgo-order-service.jar .
EXPOSE 8083
CMD java ${JAVA_OPTS} -jar ftgo-order-service.jar
