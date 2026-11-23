FROM eclipse-temurin:8-jre-alpine
RUN apk --no-cache add curl \
 && addgroup -S ftgo && adduser -S -G ftgo ftgo
WORKDIR /app
COPY --chown=ftgo:ftgo build/libs/ftgo-application.jar .
USER ftgo
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
