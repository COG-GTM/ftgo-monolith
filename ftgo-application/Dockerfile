FROM eclipse-temurin:21-jre-alpine
RUN apk --no-cache add curl && addgroup -S ftgo && adduser -S -G ftgo ftgo
WORKDIR /app
COPY --chown=ftgo:ftgo build/libs/ftgo-application.jar .
USER ftgo
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar ftgo-application.jar"]
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
