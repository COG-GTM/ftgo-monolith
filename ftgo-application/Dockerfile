# Maintained Java 8 runtime (Eclipse Temurin), pinned by digest so the image
# contents are integrity-fixed. Refresh the tag+digest regularly to pick up
# security patches.
FROM eclipse-temurin:8u462-b08-jre-alpine@sha256:963a365705214a3ab2880de348027864b3a2afa4e0f44f62c466c6b8d652e8b2
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
