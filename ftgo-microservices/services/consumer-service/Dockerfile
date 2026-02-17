FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/consumer-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
