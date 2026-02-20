# ftgo-resilience

Shared resilience library providing health checks, service discovery, and resilience patterns (circuit breaker, retry, bulkhead) for FTGO microservices.

## Quick Start

Add to your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':libs:ftgo-resilience')
}
```

The library auto-configures via Spring Boot's `AutoConfiguration` mechanism.

## Documentation

See [docs/resilience/README.md](../../docs/resilience/README.md) for full documentation.
