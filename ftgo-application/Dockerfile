FROM openjdk:8u171-jre-alpine
RUN apk --no-cache add curl \
    && addgroup -S ftgo && adduser -S -G ftgo -H -D ftgo
WORKDIR /app
COPY build/libs/ftgo-application.jar .
RUN chown -R ftgo:ftgo /app
USER ftgo
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
