# __SERVICE_DISPLAY_NAME__

Brief description of the service.

## Bounded Context

Describe the bounded context this service belongs to.

## Package Structure

```
com.ftgo.__SERVICE_PACKAGE__
  +-- domain/        # Domain entities, value objects, aggregates
  +-- service/       # Application services and use cases
  +-- web/           # REST controllers and DTOs
  +-- config/        # Spring configuration classes
  +-- messaging/     # Event publishers and consumers
```

## Running Locally

```bash
./gradlew :services:__SERVICE_NAME__:bootRun
```

## API Port

Default: `8080` (update to the assigned port)

## Setup Instructions

1. Copy this `_template-service` directory to `services/<your-service-name>`
2. Replace all `__SERVICE_NAME__` placeholders with your service name (kebab-case)
3. Replace all `__SERVICE_PACKAGE__` placeholders with your package name (lowercase, no hyphens)
4. Replace all `__SERVICE_DISPLAY_NAME__` placeholders with a human-readable name
5. Replace all `__SERVICE_NAME_UNDERSCORE__` placeholders with underscore-separated name
6. Create source packages under `src/main/java/com/ftgo/<service>/`
7. Add the service to `settings.gradle`: `include 'services:<your-service-name>'`
8. Assign a unique port number in `application.yml`
