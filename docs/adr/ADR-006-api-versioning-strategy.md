# ADR-006: API Versioning Strategy

## Status

Accepted

## Date

2026-03-17

## Context

As FTGO migrates from a monolith to microservices, we need a clear API versioning strategy to:

- Allow independent service evolution without breaking consumers
- Provide a clear contract between services
- Support gradual migration of API consumers
- Maintain backward compatibility during transitions

### Options Considered

1. **URL Path Versioning** (`/api/v1/orders`)
2. **Header Versioning** (`Accept: application/vnd.ftgo.v1+json`)
3. **Query Parameter Versioning** (`/orders?version=1`)

## Decision

We adopt **URL Path Versioning** with the format `/api/v{major}/{resource}`.

### Rationale

- **Visibility**: Version is immediately visible in URLs, logs, and monitoring
- **Simplicity**: Easy to implement with Spring MVC path prefixes
- **Cacheability**: Different versions are different URLs, enabling proper HTTP caching
- **Debugging**: Version is visible in browser, curl, and network tools
- **Routing**: API gateways and load balancers can route based on URL path
- **Industry Standard**: Used by Google, Stripe, GitHub, and many major APIs

### Implementation

The `ApiVersioningConfiguration` class in `ftgo-openapi-lib` automatically prefixes all `@RestController` endpoints with `/api/v1` using Spring MVC's `PathMatchConfigurer`.

### Version Deprecation Policy

1. Deprecated versions announced **6 months** before removal
2. Maximum **2** concurrent API versions
3. Deprecated responses include `Sunset` and `Deprecation` headers
4. Deprecated endpoints marked with `@Deprecated` in OpenAPI specs

## Consequences

### Positive

- Clear, visible versioning in all API interactions
- Simple routing and gateway configuration
- Easy to test different versions independently

### Negative

- URLs become longer with version prefix
- Service code may need to maintain multiple controller versions
- URL-based routing slightly more complex in service mesh

### Mitigation

- Keep version prefix short (`/api/v1`)
- Use controller inheritance for shared logic between versions
- Document version lifecycle clearly
