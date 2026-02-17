# FTGO Template Service

This is a template for creating new FTGO microservices. Copy this directory and customize it for your bounded context.

## Quick Start

1. Copy this template:
   ```bash
   cp -r services/_template services/ftgo-<name>-service
   ```

2. Rename packages:
   - Replace `com.ftgo.template` with `com.ftgo.<name>service`
   - Rename `TemplateServiceApplication.java` to `<Name>ServiceApplication.java`

3. Update configuration:
   - Edit `build.gradle` to add service-specific dependencies
   - Update `application.yml` with correct service name and database
   - Update Docker and K8s files with correct service name

4. Register in root `settings.gradle`:
   ```groovy
   include "services:ftgo-<name>-service"
   ```

## Project Structure

```
src/
  main/
    java/com/ftgo/<name>service/
      domain/          - Entities, aggregates, value objects, repositories
      application/     - Application services, use cases
      web/             - REST controllers, DTOs
      config/          - Spring configuration
      events/          - Domain events
      messaging/       - Async message handlers
    resources/
      application.yml  - Default config
  test/
    java/              - Mirrors main structure
    resources/
      application-test.yml
docker/
  Dockerfile           - Multi-stage container build
  docker-compose.yml   - Local dev stack
k8s/
  deployment.yaml      - K8s deployment
  service.yaml         - K8s service
  configmap.yaml       - K8s config
config/
  application.yml      - Externalized config
  application-local.yml
  application-prod.yml
```

## Building

```bash
./gradlew :services:ftgo-<name>-service:build
./gradlew :services:ftgo-<name>-service:test
```

## Running Locally

```bash
docker-compose -f services/ftgo-<name>-service/docker/docker-compose.yml up
```

## API Documentation

Once running, Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```
