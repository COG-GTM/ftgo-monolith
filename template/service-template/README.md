# Service Template

Use this template to create a new microservice in the FTGO platform.

## Getting Started

1. **Copy this template** into the `services/` directory:
   ```bash
   cp -r template/service-template services/<your-service-name>
   ```

2. **Replace placeholders** throughout all files:
   - Replace `<service-name>` with your service name (e.g., `payment-service`)
   - Replace `<ServiceName>` with the PascalCase name (e.g., `PaymentService`)

3. **Create the package structure** under `src/main/java/`:
   ```
   com/ftgo/<servicename>/
   ├── domain/          # Domain entities, repositories, services
   ├── web/             # REST controllers, request/response DTOs
   ├── main/            # Spring Boot configuration classes
   └── messaging/       # Event publishers and consumers (if needed)
   ```

4. **Register the module** in the root `settings.gradle`:
   ```groovy
   include "services:<your-service-name>"
   include "services:<your-service-name>-api"
   ```

5. **Create an API module** (if exposing public DTOs):
   ```bash
   mkdir -p services/<your-service-name>-api/src/main/java
   ```

6. **Update dependencies** in `build.gradle` to reference only the shared libraries and service APIs you need.

## Directory Structure

```
<your-service-name>/
├── build.gradle           # Gradle build configuration
├── config/
│   └── application.yml    # Spring Boot application config
├── docker/
│   └── Dockerfile         # Container image definition
├── k8s/
│   └── deployment.yml     # Kubernetes deployment manifest
├── src/
│   ├── main/
│   │   ├── java/          # Java source code
│   │   └── resources/     # Application resources
│   └── test/
│       ├── java/          # Test source code
│       └── resources/     # Test resources
└── README.md              # Service-specific documentation (create this)
```

## Naming Conventions

- **Package**: `com.ftgo.<servicename>.<layer>` (e.g., `com.ftgo.paymentservice.domain`)
- **Gradle module**: `services:<service-name>` (e.g., `services:payment-service`)
- **Docker image**: `ftgo/<service-name>` (e.g., `ftgo/payment-service`)
- **K8s resources**: `<service-name>` (e.g., `payment-service`)

## Checklist

- [ ] Copied template to `services/<your-service-name>`
- [ ] Replaced all `<service-name>` placeholders
- [ ] Created Java package structure
- [ ] Registered module in `settings.gradle`
- [ ] Updated `build.gradle` dependencies
- [ ] Created API module (if needed)
- [ ] Added database migration scripts (if needed)
- [ ] Verified build: `./gradlew :services:<your-service-name>:build`
