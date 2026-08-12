FROM azul/zulu-openjdk-debian:10
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
CMD java ${JAVA_OPTS} -jar ftgo-application.jar
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
COPY build/libs/ftgo-application.jar .
