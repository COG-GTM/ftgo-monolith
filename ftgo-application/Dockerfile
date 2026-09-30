# syntax=docker/dockerfile:1
#
# Build context must be the repository root:
#   docker build -f ftgo-application/Dockerfile -t ftgo-application:dev .

ARG BUILDER_IMAGE=eclipse-temurin:8u492-b09-jdk-jammy
ARG RUNTIME_IMAGE=eclipse-temurin:8u492-b09-jre-jammy

FROM ${BUILDER_IMAGE} AS build
WORKDIR /workspace
ARG MAVEN_MIRROR_URL=
ENV GRADLE_USER_HOME=/root/.gradle \
    GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.console=plain" \
    MAVEN_MIRROR_URL=${MAVEN_MIRROR_URL}
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    { [ -x gradlew ] || { echo "ERROR: build context must be the repository root: docker build -f ftgo-application/Dockerfile ." >&2; exit 1; }; } \
 && mkdir -p "$GRADLE_USER_HOME/init.d" \
 && cp gradle/init.d/maven-mirror.gradle "$GRADLE_USER_HOME/init.d/" \
 && ./gradlew --no-daemon :ftgo-application:bootJar \
 && cp ftgo-application/build/libs/ftgo-application.jar /ftgo-application.jar

FROM ${RUNTIME_IMAGE}
RUN groupadd --system --gid 1000 ftgo \
 && useradd --system --uid 1000 --gid ftgo --home-dir /app --shell /usr/sbin/nologin ftgo
WORKDIR /app
COPY --from=build --chown=1000:1000 /ftgo-application.jar ftgo-application.jar
USER 1000:1000
EXPOSE 8080
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/actuator/health || exit 1
CMD ["sh", "-c", "exec java ${JAVA_OPTS} -jar ftgo-application.jar"]
