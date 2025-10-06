FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
