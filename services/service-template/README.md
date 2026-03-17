# Service Template

Use this template to bootstrap a new microservice in the FTGO platform.

## How to Create a New Service

1. **Copy this template:**
   ```bash
   cp -r services/service-template services/<service-name>
   ```

2. **Rename directories:**
   - `{{service-name}}-api/`  -> `<service-name>-api/`
   - `{{service-name}}-impl/` -> `<service-name>-impl/`

3. **Rename packages:**
   - Replace `com.ftgo.{{servicename}}` with `com.ftgo.<servicename>`
   - Example: `com.ftgo.paymentservice`

4. **Update `settings.gradle`** (root project):
   ```groovy
   include "services:<service-name>:<service-name>-api"
   include "services:<service-name>:<service-name>-impl"
   ```

5. **Update build.gradle files:**
   - Replace `{{service-name}}` with actual service name in dependency declarations

6. **Configure service:**
   - Update `config/application.properties` with correct port and database name
   - Update `docker/Dockerfile` with correct JAR path and port
   - Update `k8s/deployment.yaml` with correct service metadata and port

7. **Implement domain logic** in the `domain`, `web`, `repository`, and `config` packages.

## Directory Layout

```
<service-name>/
  ├── <service-name>-api/         # Public API contract
  │   ├── build.gradle
  │   └── src/main/java/com/ftgo/<servicename>/api/
  │       ├── events/             # Domain events
  │       └── web/                # Request/response DTOs
  ├── <service-name>-impl/        # Service implementation
  │   ├── build.gradle
  │   └── src/
  │       ├── main/java/com/ftgo/<servicename>/
  │       │   ├── domain/         # Entities, aggregates, domain services
  │       │   ├── repository/     # Spring Data JPA repositories
  │       │   ├── web/            # REST controllers
  │       │   ├── config/         # Spring configuration
  │       │   └── messaging/      # Event pub/sub
  │       ├── main/resources/     # application.properties, etc.
  │       └── test/
  ├── config/                     # Environment-specific configs
  ├── docker/                     # Dockerfile
  │   └── Dockerfile
  └── k8s/                        # Kubernetes manifests
      └── deployment.yaml
```

## Naming Conventions

| Aspect              | Convention                                  | Example                          |
|---------------------|---------------------------------------------|----------------------------------|
| Service directory   | `<context>-service`                         | `payment-service`                |
| API module          | `<context>-service-api`                     | `payment-service-api`            |
| Impl module         | `<context>-service-impl`                    | `payment-service-impl`           |
| Base package        | `com.ftgo.<context>service`                 | `com.ftgo.paymentservice`        |
| Gradle path         | `:services:<service-name>:<submodule>`      | `:services:payment-service:payment-service-api` |
| Docker image        | `ftgo/<service-name>`                       | `ftgo/payment-service`           |
| K8s deployment      | `<service-name>`                            | `payment-service`                |
| Database            | `ftgo_<context>_service`                    | `ftgo_payment_service`           |
