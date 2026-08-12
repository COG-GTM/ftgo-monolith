# Java 10 runtime: no Alpine JRE variants were ever published for Java 10, so use the Debian-based JRE image.
# Pinned to 10.0.2 because the rolling `openjdk:10*` tags no longer exist on Docker Hub.
# The `-slim` variant is avoided: it is built on Debian sid, whose apt signing keys have since expired,
# so `apt-get update` (needed to install curl) fails in that image.
FROM openjdk:10.0.2-jre
# curl (used by the HEALTHCHECK below) ships in this image, so the Alpine `apk add curl` step is no longer needed.
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
