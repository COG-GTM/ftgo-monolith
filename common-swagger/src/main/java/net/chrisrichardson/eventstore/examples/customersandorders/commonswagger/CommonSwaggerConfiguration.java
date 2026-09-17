package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared OpenAPI (Swagger) configuration imported by every FTGO service.
 *
 * <p>Replaces the springfox {@code Docket}/{@code @EnableSwagger2} setup with springdoc-openapi.
 * springdoc auto-configures the endpoints, so no enabling annotation is needed:
 * <ul>
 *   <li>OpenAPI JSON at {@code /v3/api-docs} (and {@code /v3/api-docs/ftgo} for the group below)</li>
 *   <li>Swagger UI at {@code /swagger-ui.html}, which redirects to {@code /swagger-ui/index.html}</li>
 * </ul>
 *
 * <p>The old springfox rules for unwrapping {@code ResponseEntity}, {@code CompletableFuture} and
 * {@code DeferredResult} return types are not needed: springdoc unwraps those wrappers itself,
 * and it does not add default 4xx/5xx responses either.
 */
@Configuration
public class CommonSwaggerConfiguration {

    /** Base package whose {@code @RestController}s are documented (same selector the Docket used). */
    public static final String FTGO_BASE_PACKAGE = "net.chrisrichardson.ftgo";

    /** Name of the springdoc group; its spec is served at {@code /v3/api-docs/ftgo}. */
    public static final String FTGO_GROUP = "ftgo";

    /**
     * Top-level API metadata shown in Swagger UI and in the generated spec.
     */
    @Bean
    public OpenAPI ftgoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FTGO")
                        .description("FTGO - Food To Go application REST API")
                        .version("1.0"));
    }

    /**
     * API group that documents only the FTGO controllers, equivalent to the former
     * {@code RequestHandlerSelectors.basePackage("net.chrisrichardson.ftgo")} selector.
     */
    @Bean
    public GroupedOpenApi ftgoOpenApiGroup() {
        return GroupedOpenApi.builder()
                .group(FTGO_GROUP)
                .packagesToScan(FTGO_BASE_PACKAGE)
                .build();
    }
}
