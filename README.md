# FTGO Monolith

A monolithic food delivery application demonstrating microservices extraction patterns.

## Published Maven Artifacts

The following modules are published as standalone Maven artifacts to [GitHub Packages](https://maven.pkg.github.com/COG-GTM/ftgo-monolith), enabling extracted microservices to depend on them without needing the monolith source.

### Artifact Coordinates

| Module | Group ID | Artifact ID | Version |
|--------|----------|-------------|---------|
| `ftgo-common` | `net.chrisrichardson.ftgo` | `ftgo-common` | `1.0.0` |
| `ftgo-common-jpa` | `net.chrisrichardson.ftgo` | `ftgo-common-jpa` | `1.0.0` |
| `ftgo-consumer-service-api` | `net.chrisrichardson.ftgo` | `ftgo-consumer-service-api` | `1.0.0` |
| `ftgo-restaurant-service-api` | `net.chrisrichardson.ftgo` | `ftgo-restaurant-service-api` | `1.0.0` |

### Consuming the Artifacts

Add the GitHub Packages repository and the dependency to your `build.gradle`:

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/COG-GTM/ftgo-monolith")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") ?: ""
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") ?: ""
        }
    }
}

dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common:1.0.0'
    implementation 'net.chrisrichardson.ftgo:ftgo-common-jpa:1.0.0'
    // API modules for service contracts:
    implementation 'net.chrisrichardson.ftgo:ftgo-consumer-service-api:1.0.0'
    implementation 'net.chrisrichardson.ftgo:ftgo-restaurant-service-api:1.0.0'
}
```

> **Note:** GitHub Packages requires authentication even for reading packages. Set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables, or configure `gpr.user` and `gpr.key` in your `~/.gradle/gradle.properties`.

### Publishing

To publish artifacts to GitHub Packages:

```bash
GITHUB_ACTOR=<your-username> GITHUB_TOKEN=<your-token> ./gradlew publish
```

To publish to your local Maven repository (for testing):

```bash
./gradlew publishToMavenLocal
```

To publish a specific module:

```bash
./gradlew :ftgo-common:publish
./gradlew :ftgo-common-jpa:publish
```

## Building

```bash
./gradlew build
```

## Running with Docker Compose

```bash
docker-compose up -d
```
