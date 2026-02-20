# Logback Configuration Templates

This directory contains environment-specific Logback configuration templates for FTGO microservices.

## Templates

| Template | Environment | Console Output | Logstash | Root Level |
|----------|-------------|---------------|----------|------------|
| `logback-spring-dev.xml` | Development | Plain-text (colored) | Disabled | `DEBUG` |
| `logback-spring-staging.xml` | Staging | JSON | Enabled | `INFO` |
| `logback-spring-prod.xml` | Production | JSON | Enabled | `INFO` |
| `logback-spring-test.xml` | Test execution | Plain-text | Disabled | `WARN` |

## Usage

1. Copy the appropriate template to your service's `src/main/resources/logback-spring.xml`:

   ```bash
   cp config/logback/logback-spring-dev.xml services/order-service/src/main/resources/logback-spring.xml
   ```

2. The templates use Spring profiles to activate the correct configuration. Set the active profile via:

   ```properties
   spring.profiles.active=dev
   ```

   Or via environment variable:

   ```bash
   SPRING_PROFILES_ACTIVE=prod
   ```

3. All templates (except test) include `logback/logback-ftgo.xml` from `libs/ftgo-logging`, which provides the `JSON_CONSOLE`, `LOGSTASH`, and `ASYNC_LOGSTASH` appenders.

## Multi-Environment Service Configuration

For services that need to support all environments in a single `logback-spring.xml`, combine the profile-specific blocks:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="logback/logback-ftgo.xml"/>

    <!-- Development -->
    <springProfile name="dev,local,default">
        <root level="DEBUG">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>

    <!-- Staging -->
    <springProfile name="staging">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
            <appender-ref ref="ASYNC_LOGSTASH"/>
        </root>
    </springProfile>

    <!-- Production -->
    <springProfile name="prod,production">
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
            <appender-ref ref="ASYNC_LOGSTASH"/>
        </root>
    </springProfile>
</configuration>
```

## Related Documentation

- [Logging Standards](../../docs/logging-standards/README.md) - Full logging standards and conventions
- [Log Level Guidelines](../../docs/logging-standards/log-levels.md) - Per-environment log level matrix
- [Logging Architecture](../../docs/logging/README.md) - ELK stack architecture and setup
