# ADR-0002: Migrate from Springfox to SpringDoc OpenAPI 3

## Status

Accepted

## Date

2024-03-01

## Context

The FTGO monolith uses Springfox 2.8.0 to generate Swagger 2.0 API documentation via the `common-swagger` module. This presents several issues:

1. **Springfox is deprecated and unmaintained** - The last release (3.0.0) was in July 2020. No further development is planned.
2. **Swagger 2.0 is outdated** - The industry has moved to OpenAPI 3.0/3.1, which supports richer schema definitions, security schemes, and callbacks.
3. **Incompatible with Spring Boot 3.x** - Springfox does not support Spring Boot 3.x or Jakarta EE (the `javax` to `jakarta` namespace migration).
4. **Limited annotation support** - Springfox annotations (`@Api`, `@ApiOperation`, `@ApiModel`) are less expressive than OpenAPI 3.0 annotations.
5. **No standardized API format** - The monolith lacks consistent response envelopes, error formats, and pagination standards.

### Current State

```
common-swagger/
  build.gradle              # Springfox 2.8.0 dependencies
  src/main/java/.../
    CommonSwaggerConfiguration.java  # @EnableSwagger2, Docket bean
```

Each monolith service depends on `common-swagger` and inherits the Springfox configuration. The generated documentation is Swagger 2.0 format.

## Decision

We will:

1. **Create a new `ftgo-openapi-lib` module** in `shared/` that provides SpringDoc OpenAPI 3.0 configuration for new microservices.
2. **Keep the existing `common-swagger` module unchanged** for backward compatibility with the legacy monolith during the migration period.
3. **Use SpringDoc OpenAPI 2.3.0** (`springdoc-openapi-starter-webmvc-ui`) which is fully compatible with Spring Boot 3.2.x and Jakarta EE.
4. **Define standard API models** (response envelope, error format, pagination) in the new library.
5. **Adopt URL path versioning** (`/api/v1/`) as the API versioning strategy.

### Why SpringDoc?

| Criteria | Springfox 2.8.0 | SpringDoc 2.3.0 |
|----------|-----------------|-----------------|
| Maintenance | Abandoned (2020) | Active development |
| OpenAPI Version | Swagger 2.0 | OpenAPI 3.0 / 3.1 |
| Spring Boot 3.x | Not supported | Full support |
| Jakarta EE | Not supported | Full support |
| Auto-configuration | Manual `@EnableSwagger2` | Auto-configured |
| Swagger UI | `/swagger-ui.html` | `/swagger-ui.html` (same path) |
| Annotation Standard | Custom (`@Api`, `@ApiOperation`) | Standard (`@Tag`, `@Operation`) |
| Community | Declining | Growing |

## Consequences

### Positive

- **Future-proof**: SpringDoc actively supports Spring Boot 3.x and beyond
- **Standard annotations**: Uses `io.swagger.v3.oas.annotations` which are part of the OpenAPI standard
- **Auto-configuration**: No `@EnableSwagger2` needed; works out of the box
- **Richer documentation**: OpenAPI 3.0 supports more expressive schemas, security definitions, and examples
- **Consistent APIs**: Standard response envelope and error format across all microservices
- **Version catalog integration**: SpringDoc version managed centrally in `libs.versions.toml`

### Negative

- **Two modules during migration**: Both `common-swagger` (legacy) and `ftgo-openapi-lib` (new) will coexist until the monolith is fully decomposed
- **Annotation migration required**: Existing Springfox annotations on monolith controllers need to be migrated when services are extracted (this is expected as part of each service extraction)
- **Learning curve**: Developers familiar with Springfox annotations need to learn OpenAPI 3.0 annotations

### Migration Path

| Step | Scope | Action |
|------|-------|--------|
| 1 | New microservices | Use `ftgo-openapi-lib` (this ADR) |
| 2 | Service extraction | Replace Springfox annotations with OpenAPI 3.0 during extraction |
| 3 | Monolith sunset | Remove `common-swagger` module after all services are extracted |

### Annotation Migration Reference

| Springfox (Old) | SpringDoc (New) |
|----------------|-----------------|
| `@EnableSwagger2` | Auto-configured (remove) |
| `@Api(tags = "Orders")` | `@Tag(name = "Orders")` |
| `@ApiOperation(value = "Get order")` | `@Operation(summary = "Get order")` |
| `@ApiParam(value = "Order ID")` | `@Parameter(description = "Order ID")` |
| `@ApiModel(value = "Order")` | `@Schema(description = "Order")` |
| `@ApiModelProperty(value = "ID")` | `@Schema(description = "ID")` |
| `@ApiResponse(code = 200)` | `@ApiResponse(responseCode = "200")` |
| `Docket` bean | `OpenAPI` bean |

## References

- [SpringDoc OpenAPI](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Springfox GitHub (archived)](https://github.com/springfox/springfox)
- [EM-45: Define REST API Standards and Migrate from Springfox to SpringDoc OpenAPI 3](https://github.com/COG-GTM/ftgo-monolith)
