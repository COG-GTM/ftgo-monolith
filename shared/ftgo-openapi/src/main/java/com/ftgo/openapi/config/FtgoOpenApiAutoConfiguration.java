package com.ftgo.openapi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Auto-configuration for FTGO OpenAPI 3.0 documentation.
 *
 * <p>This configuration replaces the legacy {@code CommonSwaggerConfiguration} from the
 * common-swagger module (Springfox 2.8.0) with SpringDoc OpenAPI 3.0.
 *
 * <h3>Migration from Springfox to SpringDoc</h3>
 * <table>
 *   <tr><th>Springfox (Old)</th><th>SpringDoc (New)</th></tr>
 *   <tr><td>{@code @EnableSwagger2}</td><td>Auto-configured (no annotation needed)</td></tr>
 *   <tr><td>{@code Docket} bean</td><td>{@code OpenAPI} + {@code GroupedOpenApi} beans</td></tr>
 *   <tr><td>{@code @ApiOperation}</td><td>{@code @Operation}</td></tr>
 *   <tr><td>{@code @ApiParam}</td><td>{@code @Parameter}</td></tr>
 *   <tr><td>{@code @ApiModel}</td><td>{@code @Schema}</td></tr>
 *   <tr><td>{@code @ApiModelProperty}</td><td>{@code @Schema}</td></tr>
 *   <tr><td>/swagger-ui.html</td><td>/swagger-ui/index.html (or /swagger-ui.html redirect)</td></tr>
 *   <tr><td>/v2/api-docs</td><td>/v3/api-docs</td></tr>
 * </table>
 *
 * <h3>Usage</h3>
 * <p>Add this library as a dependency and configure via application.yml:
 * <pre>
 * ftgo:
 *   openapi:
 *     title: My Service API
 *     description: Description of my service
 *     version: v1
 * </pre>
 *
 * @see FtgoOpenApiProperties
 */
@Configuration
@EnableConfigurationProperties(FtgoOpenApiProperties.class)
public class FtgoOpenApiAutoConfiguration {

    private final FtgoOpenApiProperties properties;

    public FtgoOpenApiAutoConfiguration(FtgoOpenApiProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the OpenAPI 3.0 metadata bean with information from configuration properties.
     *
     * <p>This replaces the Springfox {@code Docket} bean from the legacy common-swagger module.
     * Services can override this bean by defining their own {@code OpenAPI} bean.
     *
     * @return configured OpenAPI metadata
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenApi() {
        OpenAPI openAPI = new OpenAPI()
                .info(buildApiInfo())
                .servers(Collections.singletonList(
                        new Server()
                                .url("/")
                                .description("Default Server")
                ));

        String externalDocsUrl = properties.getExternalDocsUrl();
        if (externalDocsUrl != null && !externalDocsUrl.isEmpty()) {
            openAPI.externalDocs(
                    new ExternalDocumentation()
                            .url(externalDocsUrl)
                            .description(properties.getExternalDocsDescription())
            );
        }

        return openAPI;
    }

    /**
     * Creates a grouped API configuration that scans the configured base package.
     *
     * <p>This is the SpringDoc equivalent of Springfox's
     * {@code RequestHandlerSelectors.basePackage()}.
     *
     * @return grouped API configuration for the FTGO controllers
     */
    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi ftgoApi() {
        return GroupedOpenApi.builder()
                .group("ftgo-api")
                .packagesToScan(properties.getBasePackage())
                .build();
    }

    private Info buildApiInfo() {
        Info info = new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .license(new License()
                        .name(properties.getLicenseName())
                        .url(properties.getLicenseUrl()));

        String contactName = properties.getContactName();
        if (contactName != null && !contactName.isEmpty()) {
            info.contact(new Contact()
                    .name(contactName)
                    .email(properties.getContactEmail())
                    .url(properties.getContactUrl()));
        }

        return info;
    }
}
