# Service Template

This directory serves as an archetype for creating new FTGO microservices.

## How to Create a New Service

1. **Copy this directory** to `services/<your-service-name>/`
2. **Rename packages**: Replace `com.ftgo.template` with `com.ftgo.<yourservice>` in all Java files
3. **Update `build.gradle`**: Adjust dependencies for your service's needs
4. **Update `application.properties`**: Set the service name, port, and database URL
5. **Update `docker/Dockerfile`**: Set the correct service name and port
6. **Update `k8s/deployment.yaml`**: Set the correct service name, port, and image
7. **Register in `settings.gradle`**: Add `include "services:<your-service-name>"` to the root `settings.gradle`

## Directory Structure

```
services/<service-name>/
├── build.gradle                       # Service build configuration
├── src/
│   ├── main/
│   │   ├── java/com/ftgo/<service>/   # Java source root
│   │   │   ├── domain/                # Domain entities, services, repositories
│   │   │   ├── web/                   # REST controllers, DTOs
│   │   │   ├── config/                # Spring configuration classes
│   │   │   └── repository/            # Data access layer
│   │   └── resources/
│   │       └── application.properties # Service configuration
│   └── test/
│       ├── java/com/ftgo/<service>/   # Test source root
│       │   ├── domain/                # Domain layer tests
│       │   └── web/                   # Web layer tests
│       └── resources/
├── docker/
│   └── Dockerfile                     # Docker image definition
└── k8s/
    └── deployment.yaml                # Kubernetes deployment manifest
```

## Naming Conventions

- **Service name**: lowercase, hyphenated (e.g., `order-service`, `payment-service`)
- **Package**: `com.ftgo.<service>.<layer>` (e.g., `com.ftgo.payment.domain`)
- **Gradle module**: `:services:<service-name>` (e.g., `:services:payment-service`)
