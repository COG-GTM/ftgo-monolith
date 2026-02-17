package net.chrisrichardson.ftgo.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(FtgoOpenApiProperties.class)
public class FtgoOpenApiConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI ftgoOpenAPI(FtgoOpenApiProperties properties) {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(properties.getContactEmail()))
                        .license(new License()
                                .name(properties.getLicenseName())
                                .url(properties.getLicenseUrl())))
                .servers(List.of(
                        new Server().url(properties.getServerUrl()).description("Current environment")));

        if (properties.isSecurityEnabled()) {
            openAPI.components(new Components()
                            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("JWT authentication token")))
                    .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }

        return openAPI;
    }

    @Bean
    @ConditionalOnMissingBean(GroupedOpenApi.class)
    public GroupedOpenApi publicApi(FtgoOpenApiProperties properties) {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan(properties.getBasePackage())
                .pathsToMatch("/api/**")
                .build();
    }
}
