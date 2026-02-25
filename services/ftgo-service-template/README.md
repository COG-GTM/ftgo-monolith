# FTGO Service Template

This is the template/archetype for creating new FTGO microservices. Use this as a starting point when adding a new bounded context to the platform.

## How to Use This Template

1. **Copy the directory:**
   ```bash
   cp -r services/ftgo-service-template services/ftgo-<your-service>-service
   ```

2. **Rename packages:**
   - Replace `com.ftgo.template` with `com.ftgo.<yourservice>` in all Java files
   - Update `src/main/java/com/ftgo/template/` directory to `src/main/java/com/ftgo/<yourservice>/`
   - Update `src/test/java/com/ftgo/template/` directory to `src/test/java/com/ftgo/<yourservice>/`

3. **Update build.gradle:**
   - Update the module comment at the top
   - Add/remove dependencies specific to your bounded context
   - Uncomment and update the API project dependency

4. **Update configuration:**
   - Rename `config/application.properties` values (service name, database name)
   - Update `docker/Dockerfile` labels
   - Update `k8s/deployment.yaml` with correct service name

5. **Register in settings.gradle:**
   ```groovy
   include "services:ftgo-<your-service>-service"
   ```

## Directory Structure

```
ftgo-<service>-service/
  src/
    main/
      java/com/ftgo/<service>/
        api/          # REST API request/response DTOs
        config/       # Spring configuration classes
        domain/       # Domain entities, value objects, services
        repository/   # JPA repositories
        web/          # REST controllers
      resources/
        application.properties
    test/
      java/com/ftgo/<service>/
        domain/       # Domain unit tests
        web/          # Controller tests
        repository/   # Repository tests
      resources/
    integration-test/
      java/com/ftgo/<service>/
      resources/
  config/             # External configuration files
  docker/             # Dockerfile and docker-compose overrides
  k8s/                # Kubernetes manifests
  build.gradle        # Module build configuration
```

## Package Naming Convention

All packages follow: `com.ftgo.<bounded-context>.<layer>`

| Layer        | Package                        | Purpose                              |
|-------------|--------------------------------|--------------------------------------|
| `api`       | `com.ftgo.<service>.api`       | REST DTOs, request/response objects  |
| `config`    | `com.ftgo.<service>.config`    | Spring @Configuration classes        |
| `domain`    | `com.ftgo.<service>.domain`    | Entities, value objects, services    |
| `repository`| `com.ftgo.<service>.repository`| JPA repositories                     |
| `web`       | `com.ftgo.<service>.web`       | REST controllers                     |
