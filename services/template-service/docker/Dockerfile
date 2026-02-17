FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY ../../../gradlew .
COPY ../../../gradle gradle
COPY ../../../build.gradle .
COPY ../../../settings.gradle .
COPY ../../../gradle.properties .
COPY . service/

RUN ./gradlew :services:template-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

COPY --from=build /workspace/app/service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
