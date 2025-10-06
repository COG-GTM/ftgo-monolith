FROM openjdk:8-jre-alpine
RUN apk --no-cache add curl
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
