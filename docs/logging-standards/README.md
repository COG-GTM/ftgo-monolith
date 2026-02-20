# FTGO Logging Standards

This directory defines the logging standards, conventions, and configuration templates for all FTGO microservices. These standards build on the centralized logging library (`libs/ftgo-logging/`) and ELK infrastructure (`infrastructure/logging/`) established in EM-43.

For architecture overview and infrastructure setup, see [docs/logging/README.md](../logging/README.md).

## Contents

| Document | Description |
|----------|-------------|
| [Log Levels](log-levels.md) | Log level guidelines per environment (dev, staging, prod) |
| [JSON Format](json-format.md) | Structured log format standards and required fields |
| [MDC Standards](mdc-standards.md) | Mapped Diagnostic Context field conventions |
| [Log Aggregation](log-aggregation.md) | Aggregation patterns, Kibana queries, and alerting |

## Configuration Templates

Logback configuration templates are available under `config/logback/`:

| Template | Purpose |
|----------|---------|
| [logback-spring-dev.xml](../../config/logback/logback-spring-dev.xml) | Development environment (pretty-print console) |
| [logback-spring-staging.xml](../../config/logback/logback-spring-staging.xml) | Staging environment (JSON console + Logstash) |
| [logback-spring-prod.xml](../../config/logback/logback-spring-prod.xml) | Production environment (JSON + Logstash, conservative levels) |
| [logback-spring-test.xml](../../config/logback/logback-spring-test.xml) | Test execution (minimal, console-only) |

## Quick Start

1. Add `libs/ftgo-logging` and `libs/ftgo-tracing` to your service dependencies:

   ```groovy
   dependencies {
       implementation project(':libs:ftgo-logging')
       implementation project(':libs:ftgo-tracing')
   }
   ```

2. Copy the appropriate Logback template from `config/logback/` to your service's `src/main/resources/logback-spring.xml`.

3. Configure your service in `application.properties`:

   ```properties
   spring.application.name=your-service-name
   ftgo.logging.service-name=${spring.application.name}
   ftgo.logging.json-enabled=true
   ftgo.logging.trace-correlation-enabled=true
   ```

4. Follow the [log level guidelines](log-levels.md) and [MDC standards](mdc-standards.md) in your service code.

## Principles

1. **Structured over unstructured** - All log output must be structured JSON in staging and production.
2. **Correlation by default** - Every log entry must carry `traceId`, `spanId`, and `requestId` via MDC.
3. **Consistent field names** - Use the canonical field names defined in [JSON Format](json-format.md).
4. **Environment-appropriate verbosity** - Follow the per-environment log level matrix in [Log Levels](log-levels.md).
5. **No sensitive data** - Never log PII, credentials, tokens, or payment details.
6. **Actionable messages** - Log messages should help an on-call engineer diagnose issues without additional context.

## Integration with Existing Libraries

```
libs/ftgo-tracing/        --> Provides traceId/spanId via Micrometer + OpenTelemetry
        |
        v
libs/ftgo-logging/        --> Auto-configures JSON encoder, MDC correlation, Logstash appender
        |
        v
infrastructure/logging/   --> ELK stack (Elasticsearch, Logstash, Kibana, Filebeat)
        |
        v
docs/logging-standards/   --> This directory: standards, conventions, templates
```
