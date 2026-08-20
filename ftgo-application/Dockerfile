# Maintained Temurin 8 JRE (Alpine), pinned by digest. Update tag+digest together.
FROM eclipse-temurin:8u462-b08-jre-alpine@sha256:963a365705214a3ab2880de348027864b3a2afa4e0f44f62c466c6b8d652e8b2
RUN apk --no-cache add curl
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
